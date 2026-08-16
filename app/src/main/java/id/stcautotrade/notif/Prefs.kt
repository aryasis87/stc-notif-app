package id.stcautotrade.notif

import android.content.Context

// Penyimpanan sederhana (SharedPreferences): URL server, token, cursor, interval,
// status jalan/tidak, dan status terakhir untuk ditampilkan di layar.
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("notif", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = sp.getString("baseUrl", DEFAULT_URL) ?: DEFAULT_URL
        set(v) = sp.edit().putString("baseUrl", v).apply()

    var token: String
        get() = sp.getString("token", "") ?: ""
        set(v) = sp.edit().putString("token", v).apply()

    var cursor: String
        get() = sp.getString("cursor", "") ?: ""
        set(v) = sp.edit().putString("cursor", v).apply()

    var intervalSec: Int
        get() = sp.getInt("interval", 20)
        set(v) = sp.edit().putInt("interval", v).apply()

    var running: Boolean
        get() = sp.getBoolean("running", false)
        set(v) = sp.edit().putBoolean("running", v).apply()

    var lastStatus: String
        get() = sp.getString("status", "Belum dimulai") ?: "Belum dimulai"
        set(v) = sp.edit().putString("status", v).apply()

    companion object {
        const val DEFAULT_URL = "https://admin.stcautotrade.id"
    }
}
