package id.tasirin.vpnrotasi

import org.json.JSONObject

/**
 * Satu server VPN yang diketik sendiri oleh pengguna di aplikasi.
 * Data ini hanya hidup di penyimpanan internal aplikasi — tidak pernah
 * masuk repo, tidak pernah dikirim ke mana pun selain server tujuan
 * saat pengguna menyambungkan VPN.
 */
data class ServerEntry(
    val id: Long,
    val name: String,
    val host: String,
    val country: String,
    val username: String,
    val password: String,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("host", host)
        put("country", country)
        put("username", username)
        put("password", password)
    }

    companion object {
        fun fromJson(obj: JSONObject): ServerEntry = ServerEntry(
            id = obj.optLong("id", 0L),
            name = obj.optString("name", ""),
            host = obj.optString("host", ""),
            country = obj.optString("country", ""),
            username = obj.optString("username", ""),
            password = obj.optString("password", ""),
        )
    }
}
