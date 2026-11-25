package id.tasirin.vpnrotasi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

/**
 * Layar utama: daftar server, sambung/putus, dan rotasi otomatis.
 *
 * Rotasi tahap ini berjalan selama aplikasi terbuka (pengatur waktu di
 * layar). Rotasi yang tetap jalan saat aplikasi tertutup butuh layanan
 * latar — tahap berikutnya, lihat AGENTS.md.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var store: ServerStore
    private lateinit var connector: VpnConnector

    private lateinit var tvStatus: TextView
    private lateinit var lvServers: ListView
    private lateinit var etInterval: EditText
    private lateinit var swRotation: Switch

    private val servers = mutableListOf<ServerEntry>()
    private val deadIndices = mutableSetOf<Int>()
    private val latenciesMs = mutableMapOf<Int, Long>()

    private var selectedIndex = -1
    private var currentIndex = -1

    private val handler = Handler(Looper.getMainLooper())
    private val probeExecutor = Executors.newSingleThreadExecutor()

    private val rotationTick = object : Runnable {
        override fun run() {
            rotateOnce()
            val minutes = etInterval.text.toString().toLongOrNull()?.coerceAtLeast(1) ?: 10L
            handler.postDelayed(this, minutes * 60_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = ServerStore(this)
        connector = VpnConnector(this)

        tvStatus = findViewById(R.id.tvStatus)
        lvServers = findViewById(R.id.lvServers)
        etInterval = findViewById(R.id.etInterval)
        swRotation = findViewById(R.id.swRotation)

        servers.addAll(store.load())
        refreshList()

        lvServers.setOnItemClickListener { _, _, position, _ ->
            selectedIndex = position
            setStatus("Dipilih: ${servers[position].name}. Ketuk Hubungkan untuk menyambung.")
        }
        lvServers.setOnItemLongClickListener { _, _, position, _ ->
            confirmDelete(position)
            true
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener { showAddDialog() }
        findViewById<Button>(R.id.btnConnect).setOnClickListener { connectSelected() }
        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            // Tombol Putus = putus darurat juga: hentikan rotasi sekalian.
            swRotation.isChecked = false
            val result = connector.disconnect()
            currentIndex = -1
            setStatus(result.message)
        }
        swRotation.setOnCheckedChangeListener { _, checked ->
            handler.removeCallbacks(rotationTick)
            if (checked) {
                if (servers.size < 2) {
                    swRotation.isChecked = false
                    Toast.makeText(this, "Rotasi butuh minimal 2 server.", Toast.LENGTH_SHORT).show()
                } else {
                    setStatus("Rotasi otomatis menyala.")
                    handler.post(rotationTick)
                }
            } else {
                setStatus("Rotasi otomatis mati.")
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(rotationTick)
        probeExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun setStatus(text: String) {
        tvStatus.text = text
    }

    private fun refreshList() {
        val labels = servers.map { s ->
            val mark = when (servers.indexOf(s)) {
                currentIndex -> "● "
                else -> ""
            }
            "$mark${s.name} — ${s.host} (${s.country})"
        }
        lvServers.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
    }

    private fun showAddDialog() {
        val name = EditText(this).apply { hint = "Nama (bebas, mis. SG 1)" }
        val host = EditText(this).apply { hint = "Alamat server (IP/hostname)" }
        val country = EditText(this).apply { hint = "Negara (mis. SG)" }
        val username = EditText(this).apply { hint = "User ID" }
        val password = EditText(this).apply { hint = "Password" }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(name); addView(host); addView(country); addView(username); addView(password)
        }
        // Formulir dibungkus ScrollView supaya semua kolom tetap bisa
        // dijangkau saat keyboard menutupi sebagian layar.
        val scroll = android.widget.ScrollView(this).apply { addView(box) }
        AlertDialog.Builder(this)
            .setTitle("Tambah Server")
            .setView(scroll)
            .setPositiveButton("Simpan") { _, _ ->
                val entry = ServerEntry(
                    id = System.currentTimeMillis(),
                    name = name.text.toString().ifBlank { host.text.toString() },
                    host = host.text.toString().trim(),
                    country = country.text.toString().trim().ifBlank { "-" },
                    username = username.text.toString(),
                    password = password.text.toString(),
                )
                if (entry.host.isBlank()) {
                    Toast.makeText(this, "Alamat server wajib diisi.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servers.add(entry)
                store.save(servers)
                refreshList()
                setStatus("Server ${entry.name} ditambahkan.")
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDelete(position: Int) {
        val entry = servers[position]
        AlertDialog.Builder(this)
            .setTitle("Hapus server?")
            .setMessage("${entry.name} — ${entry.host}")
            .setPositiveButton("Hapus") { _, _ ->
                servers.removeAt(position)
                store.save(servers)
                deadIndices.clear()
                latenciesMs.clear()
                if (currentIndex == position) currentIndex = -1
                selectedIndex = -1
                refreshList()
                setStatus("Server ${entry.name} dihapus.")
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun connectSelected() {
        val index = when {
            selectedIndex in servers.indices -> selectedIndex
            servers.isNotEmpty() -> 0
            else -> -1
        }
        if (index < 0) {
            Toast.makeText(this, "Tambah server dulu.", Toast.LENGTH_SHORT).show()
            return
        }
        connectTo(index)
    }

    private fun connectTo(index: Int) {
        val entry = servers[index]
        setStatus("Menyambung ke ${entry.name}…")
        probeExecutor.execute {
            val result = connector.connect(entry)
            runOnUiThread {
                if (result.ok) {
                    currentIndex = index
                    deadIndices.remove(index)
                } else {
                    // Gagal sambung: tandai mati supaya dilewati putaran ini.
                    deadIndices.add(index)
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                setStatus(result.message)
                refreshList()
            }
        }
    }

    /** Satu giliran rotasi: tes kecepatan diam-diam, lalu pindah mengikuti lingkaran. */
    private fun rotateOnce() {
        if (servers.size < 2) return
        probeExecutor.execute {
            servers.indices.forEach { i ->
                if (i in deadIndices) return@forEach
                val ms = SpeedProbe.measureMs(servers[i].host)
                if (ms == null) deadIndices.add(i) else latenciesMs[i] = ms
            }
            val next = RotationPlanner.nextIndex(servers, currentIndex, deadIndices, latenciesMs)
            runOnUiThread {
                if (next < 0) {
                    setStatus("Rotasi: tidak ada server hidup yang bisa dipilih.")
                    swRotation.isChecked = false
                } else {
                    connectTo(next)
                }
            }
        }
    }
}
