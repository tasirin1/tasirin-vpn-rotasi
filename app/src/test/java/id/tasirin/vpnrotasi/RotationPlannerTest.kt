package id.tasirin.vpnrotasi

import org.junit.Assert.assertEquals
import org.junit.Test

class RotationPlannerTest {

    private fun server(name: String, country: String = "SG") =
        ServerEntry(id = name.hashCode().toLong(), name = name, host = "$name.example", country = country, username = "u", password = "p")

    private val three = listOf(server("a"), server("b"), server("c"))

    @Test
    fun `daftar kosong menghasilkan -1`() {
        assertEquals(-1, RotationPlanner.nextIndex(emptyList(), -1, emptySet(), emptyMap()))
    }

    @Test
    fun `dari awal lingkaran pindah ke server berikutnya`() {
        // Tanpa data kecepatan: urutan lingkaran yang menang.
        assertEquals(1, RotationPlanner.nextIndex(three, 0, emptySet(), emptyMap()))
    }

    @Test
    fun `lingkaran membungkus dari terakhir ke pertama`() {
        assertEquals(0, RotationPlanner.nextIndex(three, 2, emptySet(), emptyMap()))
    }

    @Test
    fun `tidak pernah diam di server yang sama bila ada pilihan lain`() {
        // "a" paling cepat sekalipun, dari posisi a tetap harus pindah.
        val speeds = mapOf(0 to 1L, 1 to 900L, 2 to 800L)
        assertEquals(2, RotationPlanner.nextIndex(three, 0, emptySet(), speeds))
    }

    @Test
    fun `tercepat di antara calon sesudah posisi sekarang yang menang`() {
        val speeds = mapOf(0 to 500L, 1 to 50L, 2 to 900L)
        assertEquals(1, RotationPlanner.nextIndex(three, 2, emptySet(), speeds))
    }

    @Test
    fun `server mati dilewati`() {
        assertEquals(2, RotationPlanner.nextIndex(three, 0, setOf(1), emptyMap()))
    }

    @Test
    fun `semua mati menghasilkan -1`() {
        assertEquals(-1, RotationPlanner.nextIndex(three, 0, setOf(0, 1, 2), emptyMap()))
    }

    @Test
    fun `saringan negara membatasi lingkaran`() {
        val mixed = listOf(server("a", "SG"), server("b", "JP"), server("c", "SG"))
        assertEquals(2, RotationPlanner.nextIndex(mixed, 0, emptySet(), emptyMap(), countryFilter = "SG"))
    }

    @Test
    fun `satu server saja tetap kepilih`() {
        val one = listOf(server("a"))
        assertEquals(0, RotationPlanner.nextIndex(one, 0, emptySet(), emptyMap()))
    }
}
