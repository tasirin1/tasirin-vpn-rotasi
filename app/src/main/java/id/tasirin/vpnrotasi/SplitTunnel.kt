package id.tasirin.vpnrotasi

import android.content.Context

/** Mode split tunneling: aplikasi mana saja yang lalu lintasnya lewat VPN. */
enum class SplitMode { MATI, HANYA, KECUALIKAN }

/**
 * Logika murni split tunneling (fitur pilihan owner nomor 6).
 *
 * resolve() mengubah mode + daftar paket terpilih menjadi pasangan
 * (allowed, disallowed) untuk Ikev2VpnProfile.Builder:
 * - MATI: keduanya kosong -> semua aplikasi lewat VPN (perilaku bawaan).
 * - HANYA: allowed = terpilih -> hanya aplikasi itu yang lewat VPN.
 * - KECUALIKAN: disallowed = terpilih -> semua lewat VPN kecuali itu.
 */
object SplitTunnel {

    fun resolve(mode: SplitMode, selected: Set<String>): Pair<List<String>, List<String>> =
        when (mode) {
            SplitMode.MATI -> Pair(emptyList(), emptyList())
            SplitMode.HANYA -> Pair(selected.sorted(), emptyList())
            SplitMode.KECUALIKAN -> Pair(emptyList(), selected.sorted())
        }

    /**
     * Mode HANYA tanpa aplikasi terpilih tidak sah: daftar allowed yang
     * kosong di Android artinya SEMUA aplikasi — kebalikan dari maksud
     * pengguna. Pemanggil wajib menolak menyambung dalam keadaan ini.
     */
    fun valid(mode: SplitMode, selected: Set<String>): Boolean =
        mode != SplitMode.HANYA || selected.isNotEmpty()
}

/** Penyimpanan pengaturan split tunneling (mode + paket terpilih). */
class SplitStore(context: Context) {

    private val prefs = context.getSharedPreferences("split_tunnel", Context.MODE_PRIVATE)

    fun loadMode(): SplitMode {
        val raw = prefs.getString("mode", null)
        return SplitMode.values().firstOrNull { it.name == raw } ?: SplitMode.MATI
    }

    fun loadSelected(): Set<String> =
        prefs.getStringSet("packages", emptySet())?.toSet() ?: emptySet()

    fun save(mode: SplitMode, selected: Set<String>) {
        prefs.edit()
            .putString("mode", mode.name)
            .putStringSet("packages", selected)
            .apply()
    }
}
