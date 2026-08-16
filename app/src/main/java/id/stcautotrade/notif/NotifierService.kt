package id.stcautotrade.notif

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

// Foreground service: menarik /api/notify/feed tiap N detik dan memunculkan
// notifikasi untuk tiap event baru. TANPA FCM/Google — murni HTTPS ke server
// sendiri, jadi kebal blokir push. START_STICKY + BootReceiver menjaga tetap hidup.
class NotifierService : Service() {
    private val running = AtomicBoolean(false)
    @Volatile private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopWork()
            return START_NOT_STICKY
        }
        startInForeground("Memantau notifikasi…")
        if (running.compareAndSet(false, true)) {
            Prefs(this).running = true
            worker = Thread { loop() }.also { it.start() }
        }
        return START_STICKY
    }

    private fun startInForeground(text: String) {
        val notif = ongoingNotif(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FG_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(FG_ID, notif)
        }
    }

    private fun stopWork() {
        running.set(false)
        Prefs(this).running = false
        worker?.interrupt()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun loop() {
        val prefs = Prefs(this)
        while (running.get()) {
            try {
                poll(prefs)
            } catch (e: InterruptedException) {
                break
            } catch (e: Exception) {
                setStatus(prefs, "Tunggu koneksi… " + nowHm())
            }
            try {
                Thread.sleep((prefs.intervalSec.coerceIn(10, 300) * 1000).toLong())
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    private fun poll(prefs: Prefs) {
        val base = prefs.baseUrl.trim().trimEnd('/')
        val token = prefs.token.trim()
        if (base.isEmpty() || token.isEmpty()) {
            setStatus(prefs, "URL / token belum diisi")
            return
        }
        val cursor = prefs.cursor
        val sb = StringBuilder(base).append("/api/notify/feed?token=")
            .append(URLEncoder.encode(token, "UTF-8"))
        if (cursor.isNotEmpty()) sb.append("&since=").append(cursor)

        val conn = (URL(sb.toString()).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 20000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = conn.responseCode
            if (code == 401) { setStatus(prefs, "Token salah (401)"); return }
            if (code != 200) { setStatus(prefs, "Server $code · " + nowHm()); return }
            val body = conn.inputStream.bufferedReader().readText()
            val j = JSONObject(body)
            prefs.cursor = j.optString("cursor", cursor)
            val arr = j.optJSONArray("events")
            var shown = 0
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val e = arr.getJSONObject(i)
                    notifyEvent(
                        e.optString("type"),
                        e.optString("title", "Notifikasi"),
                        e.optString("body", ""),
                        e.optString("key", System.nanoTime().toString())
                    )
                    shown++
                }
            }
            setStatus(prefs, if (shown > 0) "Aktif · $shown notif baru · " + nowHm() else "Aktif · dicek " + nowHm())
        } finally {
            conn.disconnect()
        }
    }

    private fun setStatus(prefs: Prefs, text: String) {
        prefs.lastStatus = text
        // perbarui teks pada notifikasi tetap
        NotificationManagerCompat.from(this).notify(FG_ID, ongoingNotif(text))
    }

    private fun ongoingNotif(text: String): android.app.Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, LockActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, App.CH_ONGOING)
            .setContentTitle("Notifikasi aktif")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_notif)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun notifyEvent(type: String, title: String, body: String, key: String) {
        val pi = PendingIntent.getActivity(
            this, 1, Intent(this, LockActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(this, App.CH_ALERT)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_stat_notif)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pi)
            .build()
        // id unik & stabil per event agar tak dobel/timpa
        NotificationManagerCompat.from(this).notify(key.hashCode(), n)
    }

    private fun nowHm(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    companion object {
        const val ACTION_STOP = "id.stcautotrade.notif.STOP"
        private const val FG_ID = 1001
    }
}
