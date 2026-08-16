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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

// Foreground service: menarik /api/notify/feed tiap N detik, memunculkan notifikasi
// per event baru, + menyimpan ke riwayat (dibaca tab Notifikasi). TANPA FCM.
class NotifierService : Service() {
    private val running = AtomicBoolean(false)
    @Volatile private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopWork(); return START_NOT_STICKY
        }
        startInForeground("Menyinkronkan menu…")
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
        val token = prefs.token
        if (token.isEmpty()) { setStatus(prefs, "Belum login"); return }
        val cursor = prefs.cursor
        val sb = StringBuilder(prefs.baseUrl).append("/api/notify/feed?token=").append(token)
        if (cursor.isNotEmpty()) sb.append("&since=").append(cursor)

        val conn = (URL(sb.toString()).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000; readTimeout = 20000; requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = conn.responseCode
            if (code == 401) { setStatus(prefs, "Sesi tak sah (401) — login ulang"); return }
            if (code != 200) { setStatus(prefs, "Server $code · " + nowHm()); return }
            val j = JSONObject(conn.inputStream.bufferedReader().readText())
            prefs.cursor = j.optString("cursor", cursor)
            val arr = j.optJSONArray("events")
            var shown = 0
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val e = arr.getJSONObject(i)
                    val type = e.optString("type")
                    val title = e.optString("title", "Notifikasi")
                    val body = e.optString("body", "")
                    val ts = e.optLong("ts", System.currentTimeMillis() / 1000)
                    notifyEvent(type, title, body, e.optString("key", System.nanoTime().toString()))
                    prefs.addHistory(type, title, body, ts * 1000)
                    shown++
                }
            }
            setStatus(prefs, if (shown > 0) "$shown promo baru · " + nowHm() else "Menu tersinkron · " + nowHm())
        } finally {
            conn.disconnect()
        }
    }

    private fun setStatus(prefs: Prefs, text: String) {
        prefs.lastStatus = text
        NotificationManagerCompat.from(this).notify(FG_ID, ongoingNotif(text))
    }

    private fun contentPi(req: Int): PendingIntent = PendingIntent.getActivity(
        this, req, Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun ongoingNotif(text: String): android.app.Notification =
        NotificationCompat.Builder(this, App.CH_ONGOING)
            .setContentTitle("Menu Resto")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_notif)
            .setOngoing(true).setSilent(true)
            .setContentIntent(contentPi(0))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun notifyEvent(type: String, title: String, body: String, key: String) {
        val n = NotificationCompat.Builder(this, App.CH_ALERT)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_stat_notif)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentPi(1))
            .build()
        NotificationManagerCompat.from(this).notify(key.hashCode(), n)
    }

    private fun nowHm(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    companion object {
        const val ACTION_STOP = "id.stcautotrade.notif.STOP"
        private const val FG_ID = 1001

        fun start(ctx: android.content.Context) {
            androidx.core.content.ContextCompat.startForegroundService(ctx, Intent(ctx, NotifierService::class.java))
        }
        fun stop(ctx: android.content.Context) {
            ctx.startService(Intent(ctx, NotifierService::class.java).setAction(ACTION_STOP))
        }
    }
}
