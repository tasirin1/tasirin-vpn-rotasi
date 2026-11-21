package id.tasirin.vpnrotasi

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * Penyimpanan daftar server: satu berkas JSON di folder internal aplikasi.
 * Sengaja sederhana (tanpa database) — daftar server pengguna kecil.
 */
class ServerStore(context: Context) {

    private val file = File(context.filesDir, "servers.json")

    fun load(): MutableList<ServerEntry> {
        if (!file.exists()) return mutableListOf()
        return try {
            val arr = JSONArray(file.readText())
            MutableList(arr.length()) { i -> ServerEntry.fromJson(arr.getJSONObject(i)) }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(servers: List<ServerEntry>) {
        val arr = JSONArray()
        servers.forEach { arr.put(it.toJson()) }
        file.writeText(arr.toString())
    }
}
