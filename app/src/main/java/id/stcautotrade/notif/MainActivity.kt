package id.stcautotrade.notif

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    private lateinit var etUrl: EditText
    private lateinit var etToken: EditText
    private lateinit var etInterval: EditText
    private lateinit var tvStatus: TextView

    private val askNotif = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startNotifier() else toast("Izin notifikasi ditolak — tak bisa memunculkan notif.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)

        etUrl = findViewById(R.id.etUrl)
        etToken = findViewById(R.id.etToken)
        etInterval = findViewById(R.id.etInterval)
        tvStatus = findViewById(R.id.tvStatus)

        etUrl.setText(prefs.baseUrl)
        etToken.setText(prefs.token)
        etInterval.setText(prefs.intervalSec.toString())

        findViewById<Button>(R.id.btnStart).setOnClickListener { saveThenStart() }
        findViewById<Button>(R.id.btnStop).setOnClickListener { stopNotifier() }
        findViewById<Button>(R.id.btnTest).setOnClickListener { testNotif() }
        findViewById<Button>(R.id.btnBattery).setOnClickListener { requestIgnoreBattery() }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun saveThenStart() {
        prefs.baseUrl = etUrl.text.toString().trim()
        prefs.token = etToken.text.toString().trim()
        prefs.intervalSec = (etInterval.text.toString().toIntOrNull() ?: 20).coerceIn(10, 300)
        if (prefs.token.isEmpty()) { toast("Isi token dulu."); return }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startNotifier()
        }
    }

    private fun startNotifier() {
        ContextCompat.startForegroundService(this, Intent(this, NotifierService::class.java))
        toast("Pemantauan dimulai.")
        tvStatus.postDelayed({ refreshStatus() }, 1200)
    }

    private fun stopNotifier() {
        startService(Intent(this, NotifierService::class.java).setAction(NotifierService.ACTION_STOP))
        prefs.running = false
        toast("Pemantauan dihentikan.")
        tvStatus.postDelayed({ refreshStatus() }, 600)
    }

    private fun testNotif() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        val n = NotificationCompat.Builder(this, App.CH_ALERT)
            .setContentTitle("🔔 Tes notifikasi")
            .setContentText("Kalau ini muncul, notifikasi di perangkat ini berfungsi.")
            .setSmallIcon(R.drawable.ic_stat_notif)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        NotificationManagerCompat.from(this).notify(9999, n)
    }

    private fun requestIgnoreBattery() {
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
            )
        } catch (e: Exception) {
            try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
            catch (e2: Exception) { toast("Buka Setelan > Baterai secara manual.") }
        }
    }

    private fun refreshStatus() {
        val state = if (prefs.running) "● BERJALAN" else "○ berhenti"
        tvStatus.text = "$state\n${prefs.lastStatus}"
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
