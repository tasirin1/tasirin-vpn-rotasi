package id.tasirin.vpnrotasi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Uji logika murni SplitTunnel.
class SplitTunnelTest {

    @Test
    fun matiTidakMembatasiApaPun() {
        val (allowed, disallowed) = SplitTunnel.resolve(SplitMode.MATI, setOf("a.b", "c.d"))
        assertEquals(emptyList<String>(), allowed)
        assertEquals(emptyList<String>(), disallowed)
    }

    @Test
    fun hanyaJadiDaftarAllowedTerurut() {
        val (allowed, disallowed) = SplitTunnel.resolve(SplitMode.HANYA, setOf("b.c", "a.b"))
        assertEquals(listOf("a.b", "b.c"), allowed)
        assertEquals(emptyList<String>(), disallowed)
    }

    @Test
    fun kecualiJadiDaftarDisallowedTerurut() {
        val (allowed, disallowed) = SplitTunnel.resolve(SplitMode.KECUALIKAN, setOf("b.c", "a.b"))
        assertEquals(emptyList<String>(), allowed)
        assertEquals(listOf("a.b", "b.c"), disallowed)
    }

    @Test
    fun hanyaTanpaPilihanTidakSah() {
        assertFalse(SplitTunnel.valid(SplitMode.HANYA, emptySet()))
        assertTrue(SplitTunnel.valid(SplitMode.HANYA, setOf("a.b")))
        assertTrue(SplitTunnel.valid(SplitMode.MATI, emptySet()))
        assertTrue(SplitTunnel.valid(SplitMode.KECUALIKAN, emptySet()))
    }
}
