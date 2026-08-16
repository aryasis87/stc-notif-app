package id.stcautotrade.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

// Hidupkan lagi service setelah HP reboot, kalau sebelumnya memang sedang aktif.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && Prefs(context).running) {
            ContextCompat.startForegroundService(context, Intent(context, NotifierService::class.java))
        }
    }
}
