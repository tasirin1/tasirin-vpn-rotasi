package id.tasirin.vpnrotasi

/**
 * Otak rotasi server — logika murni, tanpa Android, bisa diuji JVM.
 *
 * Aturan owner (2026-11-21):
 * - Pola LINGKARAN: giliran selalu bergerak maju dari server aktif sekarang,
 *   sesudah yang terakhir balik ke pertama. Server yang sama tidak boleh
 *   kepilih dua kali berurutan selama masih ada pilihan lain yang hidup.
 * - Di antara calon sesudah posisi sekarang (mengikuti lingkaran), yang
 *   dipilih adalah yang TERCEPAT menurut hasil tes kecepatan terakhir.
 *   Calon yang belum pernah dites dianggap paling lambat.
 * - Server mati (pernah gagal sambung) dilewati.
 * - Lingkaran bisa dibatasi satu negara.
 */
object RotationPlanner {

    /**
     * @return indeks server berikutnya, atau -1 bila tidak ada calon hidup.
     */
    fun nextIndex(
        servers: List<ServerEntry>,
        currentIndex: Int,
        deadIndices: Set<Int>,
        latenciesMs: Map<Int, Long>,
        countryFilter: String? = null,
    ): Int {
        if (servers.isEmpty()) return -1

        // Calon: semua indeks yang lolos saringan negara dan tidak mati,
        // diurutkan mengikuti lingkaran mulai sesudah posisi sekarang.
        val pool = servers.indices.filter { i ->
            i !in deadIndices &&
                (countryFilter == null || servers[i].country.equals(countryFilter, ignoreCase = true))
        }
        if (pool.isEmpty()) return -1

        val start = if (currentIndex in servers.indices) currentIndex else -1
        val circular = pool.sortedBy { i ->
            val d = (i - start + servers.size) % servers.size
            if (d == 0) servers.size else d // posisi sekarang sendiri paling akhir
        }

        // Bila masih ada calon selain posisi sekarang, posisi sekarang gugur:
        // rotasi wajib bergerak, bukan diam di server yang sama.
        val movable = if (circular.size > 1) circular.filter { it != start } else circular
        if (movable.isEmpty()) return -1

        // Tercepat menang; yang belum dites = paling lambat; seri = urutan lingkaran.
        return movable.minWithOrNull(
            compareBy<Int> { latenciesMs[it] ?: Long.MAX_VALUE }
                .thenBy { circular.indexOf(it) }
        ) ?: -1
    }
}
