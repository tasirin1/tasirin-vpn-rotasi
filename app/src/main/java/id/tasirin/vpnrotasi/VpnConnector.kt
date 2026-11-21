package id.tasirin.vpnrotasi

import android.content.Context
import android.net.Ikev2VpnProfile
import android.net.VpnManager
import android.os.Build

/**
 * Penghubung ke mesin VPN bawaan Android (IKEv2 lewat VpnManager, API 30+).
 *
 * Aplikasi ini TIDAK menulis protokol VPN sendiri — enkripsi dan jabat
 * tangan IKEv2 dikerjakan sistem Android. Kode orisinal aplikasi ini yang
 * mengatur daftar server, rotasi, dan keputusannya.
 */
class VpnConnector(private val context: Context) {

    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    /** Hasil sambung: berhasil atau pesan gagal yang bisa ditampilkan. */
    data class Result(val ok: Boolean, val message: String)

    fun connect(entry: ServerEntry): Result {
        if (!isSupported()) {
            return Result(false, "Butuh Android 11 (API 30) atau lebih baru untuk menyambung IKEv2.")
        }
        return try {
            val manager = context.getSystemService(VpnManager::class.java)
                ?: return Result(false, "Layanan VPN sistem tidak tersedia.")
            val profile = Ikev2VpnProfile.Builder(entry.host, entry.username)
                .setUsernamePasswordAuthentication(entry.username, entry.password)
                .setMetered(false)
                .build()
            manager.provisionVpnProfile(profile)
            manager.startProvisionedVpnProfile()
            Result(true, "Tersambung ke ${entry.name}.")
        } catch (e: SecurityException) {
            // Sandi ditolak server atau izin VPN ditolak sistem sama-sama
            // mampir ke sini pada banyak perangkat; pesan dibuat jujur umum.
            Result(false, "Sistem menolak sambungan (izin VPN atau sandi ditolak): ${e.message}")
        } catch (e: Exception) {
            Result(false, "Gagal menyambung: ${e.message}")
        }
    }

    fun disconnect(): Result {
        if (!isSupported()) return Result(false, "Tidak didukung di Android ini.")
        return try {
            val manager = context.getSystemService(VpnManager::class.java)
                ?: return Result(false, "Layanan VPN sistem tidak tersedia.")
            manager.stopProvisionedVpnProfile()
            Result(true, "VPN diputus.")
        } catch (e: Exception) {
            Result(false, "Gagal memutus: ${e.message}")
        }
    }
}
