package id.stcautotrade.notif

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

// Membuat channel notifikasi sekali saat app dibuat:
//  - ongoing : notifikasi tetap "sedang memantau" (importance rendah, senyap)
//  - alerts  : notifikasi event (deposit/penarikan/aktivasi) — bunyi + getar
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)

        val ongoing = NotificationChannel(CH_ONGOING, "Pemantauan", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Notifikasi tetap saat memantau di latar belakang"
            setShowBadge(false)
        }
        val alerts = NotificationChannel(CH_ALERT, "Notifikasi", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Deposit, penarikan, dan permintaan aktivasi"
            enableVibration(true)
            enableLights(true)
        }
        nm.createNotificationChannel(ongoing)
        nm.createNotificationChannel(alerts)
    }

    companion object {
        const val CH_ONGOING = "ongoing"
        const val CH_ALERT = "alerts"
    }
}
