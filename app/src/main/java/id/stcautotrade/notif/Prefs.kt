package id.stcautotrade.notif

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Penyimpanan lokal (app-private). URL server tetap (bukan rahasia). Token feed &
// password disimpan setelah login (password dipakai auto-login WebView dashboard).
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("notif", Context.MODE_PRIVATE)

    val baseUrl: String get() = "https://admin.stcautotrade.id"

    var token: String
        get() = sp.getString("token", "") ?: ""
        set(v) = sp.edit().putString("token", v).apply()

    // password admin — dipakai auto-login WebView (app sudah tergerbang password).
    var password: String
        get() = sp.getString("password", "") ?: ""
        set(v) = sp.edit().putString("password", v).apply()

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

    val loggedIn: Boolean get() = token.isNotEmpty()

    fun logout() {
        sp.edit().remove("token").remove("password").remove("cursor").apply()
    }

    // ── Riwayat notifikasi (untuk tab Notifikasi) ────────────────────────────
    fun addHistory(type: String, title: String, body: String, ts: Long) {
        val arr = JSONArray(sp.getString("history", "[]"))
        val item = JSONObject().put("type", type).put("title", title).put("body", body).put("ts", ts)
        // sisipkan di depan (terbaru dulu), cap 100
        val next = JSONArray()
        next.put(item)
        for (i in 0 until minOf(arr.length(), 99)) next.put(arr.getJSONObject(i))
        sp.edit().putString("history", next.toString()).apply()
    }

    fun history(): List<HistoryItem> {
        val arr = JSONArray(sp.getString("history", "[]"))
        val out = ArrayList<HistoryItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(HistoryItem(o.optString("type"), o.optString("title"), o.optString("body"), o.optLong("ts")))
        }
        return out
    }

    fun clearHistory() = sp.edit().remove("history").apply()
}

data class HistoryItem(val type: String, val title: String, val body: String, val ts: Long)
