package id.stcautotrade.notif

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class BotSummary(
    val bot: String, val label: String,
    val total: Int, val active: Int, val inactive: Int,
    val recent24h: Int, val recentAdded24h: Int,
    val totalSessions: Int, val activeSessions: Int, val running: Int,
    val error: String? = null,
)

object Api {
    private fun open(urlStr: String, method: String): HttpURLConnection =
        (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000; readTimeout = 20000; requestMethod = method
            setRequestProperty("Accept", "application/json")
        }

    // Login: tukar password → token feed. Return token atau null (gagal).
    suspend fun login(base: String, password: String): String? = withContext(Dispatchers.IO) {
        val c = open("$base/api/notify/login", "POST")
        try {
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(JSONObject().put("password", password).toString().toByteArray()) }
            if (c.responseCode != 200) return@withContext null
            val j = JSONObject(c.inputStream.bufferedReader().readText())
            j.optString("token").ifEmpty { null }
        } catch (e: Exception) {
            null
        } finally {
            c.disconnect()
        }
    }

    // Ringkasan stats semua bot.
    suspend fun summary(base: String, token: String): List<BotSummary>? = withContext(Dispatchers.IO) {
        val c = open("$base/api/notify/summary?token=$token", "GET")
        try {
            if (c.responseCode != 200) return@withContext null
            val j = JSONObject(c.inputStream.bufferedReader().readText())
            val arr = j.optJSONArray("bots") ?: return@withContext emptyList()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                BotSummary(
                    bot = o.optString("bot"), label = o.optString("label"),
                    total = o.optInt("total"), active = o.optInt("active"), inactive = o.optInt("inactive"),
                    recent24h = o.optInt("recent24h"), recentAdded24h = o.optInt("recentAdded24h"),
                    totalSessions = o.optInt("totalSessions"), activeSessions = o.optInt("activeSessions"),
                    running = o.optInt("running"), error = if (o.has("error")) o.optString("error") else null,
                )
            }
        } catch (e: Exception) {
            null
        } finally {
            c.disconnect()
        }
    }

    // Ambil cookie sesi admin (stc_admin=JWT) via /api/auth/login utk auto-login WebView.
    suspend fun authCookie(base: String, password: String): String? = withContext(Dispatchers.IO) {
        val c = open("$base/api/auth/login", "POST")
        try {
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(JSONObject().put("password", password).toString().toByteArray()) }
            if (c.responseCode !in 200..299) return@withContext null
            val cookies = c.headerFields["Set-Cookie"] ?: c.headerFields["set-cookie"] ?: return@withContext null
            val jwt = cookies.firstOrNull { it.startsWith("stc_admin=") } ?: return@withContext null
            jwt.substringBefore(";") // "stc_admin=<value>"
        } catch (e: Exception) {
            null
        } finally {
            c.disconnect()
        }
    }
}
