package id.tasirin.vpnrotasi

import java.net.InetAddress

/**
 * Tes kecepatan tahap awal: waktu resolusi alamat server.
 *
 * Ini sinyal kasar (server yang alamatnya tidak terjangkau akan gagal di
 * sini dan dianggap mati untuk putaran ini). Pengukuran latensi UDP IKEv2
 * yang akurat adalah tahap berikutnya — lihat AGENTS.md.
 *
 * @return lama resolusi dalam ms, atau null bila alamat tak terjangkau.
 */
object SpeedProbe {

    fun measureMs(host: String): Long? = try {
        val start = System.nanoTime()
        InetAddress.getByName(host)
        (System.nanoTime() - start) / 1_000_000
    } catch (e: Exception) {
        null
    }
}
