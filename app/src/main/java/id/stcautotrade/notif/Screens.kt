package id.stcautotrade.notif

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val idNum = NumberFormat.getInstance(Locale("id"))
private fun n(x: Int) = idNum.format(x)

// ── Komponen bersama (iOS grouped style) ─────────────────────────────────────
@Composable
fun LargeHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.displaySmall)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (action != null) action()
    }
}

@Composable
fun GroupCard(header: String? = null, content: @Composable ColumnScope.() -> Unit) {
    if (header != null) {
        Text(
            header.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 32.dp, top = 14.dp, bottom = 6.dp),
        )
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(content = content)
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface, last: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
    if (!last) HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outline)
}

// ── BERANDA ──────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<List<BotSummary>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var updated by remember { mutableStateOf("") }

    fun load() {
        loading = true
        scope.launch {
            data = Api.summary(prefs.baseUrl, prefs.token)
            loading = false
            updated = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LargeHeader("Beranda", if (updated.isEmpty()) "Ringkasan STC & KOALA" else "Diperbarui $updated") {
            IconButton(onClick = { load() }) { Icon(Icons.Filled.Refresh, "Muat ulang") }
        }
        if (loading && data == null) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (data == null) {
            GroupCard { InfoRow("Gagal memuat", "coba lagi", MaterialTheme.colorScheme.error, last = true) }
        } else {
            data!!.forEach { b ->
                GroupCard(header = b.label) {
                    if (b.error != null) {
                        InfoRow("Kesalahan", b.error, MaterialTheme.colorScheme.error, last = true)
                    } else {
                        InfoRow("Total user", n(b.total))
                        InfoRow("Aktif", n(b.active), AppColors.green)
                        InfoRow("Login 24 jam", n(b.recent24h))
                        InfoRow("User baru 24 jam", n(b.recentAdded24h))
                        InfoRow("Sesi aktif", "${n(b.activeSessions)} / ${n(b.totalSessions)}")
                        InfoRow("Mode jalan", n(b.running), MaterialTheme.colorScheme.primary, last = true)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ── NOTIFIKASI ───────────────────────────────────────────────────────────────
@Composable
fun NotificationsScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var notifs by remember { mutableStateOf(prefs.history()) }
    var running by remember { mutableStateOf(prefs.running) }
    var status by remember { mutableStateOf(prefs.lastStatus) }

    LaunchedEffect(Unit) {
        while (true) {
            notifs = prefs.history(); running = prefs.running; status = prefs.lastStatus
            delay(2500)
        }
    }

    Column(Modifier.fillMaxSize()) {
        LargeHeader("Notifikasi", if (running) "Memantau · $status" else "Berhenti")
        Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            AssistChip(
                onClick = {
                    if (running) NotifierService.stop(ctx) else NotifierService.start(ctx)
                    running = !running
                },
                label = { Text(if (running) "Hentikan" else "Mulai pantau") },
                leadingIcon = { Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null) },
            )
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = { prefs.clearHistory(); notifs = emptyList() },
                label = { Text("Bersihkan") },
                leadingIcon = { Icon(Icons.Filled.DeleteOutline, null) },
            )
        }
        if (notifs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(46.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Belum ada notifikasi", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(notifs) { it -> NotifRow(it) }
            }
        }
    }
}

@Composable
private fun NotifRow(it: HistoryItem) {
    val (icon, tint) = when (it.type) {
        "deposit" -> Icons.Filled.SouthWest to AppColors.green
        "penarikan" -> Icons.Filled.NorthEast to AppColors.red
        "aktivasi" -> Icons.Filled.VerifiedUser to AppColors.blue
        else -> Icons.Filled.Notifications to MaterialTheme.colorScheme.primary
    }
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = tint.copy(alpha = 0.14f), shape = MaterialTheme.shapes.small) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(it.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(it.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text(fmtTime(it.ts), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun fmtTime(ts: Long): String =
    SimpleDateFormat("dd MMM · HH:mm", Locale("id")).format(Date(ts))

// ── DASHBOARD (WebView auto-login) ───────────────────────────────────────────
@Composable
fun DashboardScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    var ready by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) {
        val cookie = Api.authCookie(prefs.baseUrl, prefs.password)
        if (cookie != null) {
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setCookie(prefs.baseUrl, "$cookie; Path=/")
                flush()
            }
        }
        ready = true
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { webView?.reload() }) { Icon(Icons.Filled.Refresh, "Muat ulang") }
        }
        Box(Modifier.fillMaxSize()) {
            if (ready) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { c ->
                        WebView(c).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            loadUrl(prefs.baseUrl + "/")
                            webView = this
                        }
                    },
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }
}

// ── SETELAN ──────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var running by remember { mutableStateOf(prefs.running) }

    LaunchedEffect(Unit) { while (true) { running = prefs.running; delay(2000) } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LargeHeader("Setelan")

        GroupCard(header = "Pemantauan") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pantau di latar", style = MaterialTheme.typography.bodyLarge)
                    Text(if (running) "Aktif" else "Berhenti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = running, onCheckedChange = {
                    if (it) NotifierService.start(ctx) else NotifierService.stop(ctx)
                    running = it
                })
            }
            HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outline)
            InfoRow("Interval cek", "${prefs.intervalSec} detik", last = true)
        }

        GroupCard(header = "Notifikasi HP") {
            TapRow("Kirim notif tes", Icons.Filled.NotificationsActive) { sendTestNotif(ctx) }
            HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outline)
            TapRow("Abaikan optimasi baterai", Icons.Filled.BatteryChargingFull, last = true) {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
                } catch (e: Exception) {
                    try { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) {}
                }
            }
        }

        GroupCard(header = "Akun") {
            TapRow("Keluar", Icons.Filled.Logout, tint = MaterialTheme.colorScheme.error, last = true) { onLogout() }
        }

        Text(
            "STC Notif v2.0 · admin.stcautotrade.id",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        )
    }
}

@Composable
private fun TapRow(label: String, icon: ImageVector, tint: Color = MaterialTheme.colorScheme.primary, last: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = tint, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (!last) HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outline)
}

private fun sendTestNotif(ctx: android.content.Context) {
    val n = androidx.core.app.NotificationCompat.Builder(ctx, App.CH_ALERT)
        .setContentTitle("🔔 Tes notifikasi")
        .setContentText("Kalau ini muncul, notifikasi di perangkat ini berfungsi.")
        .setSmallIcon(R.drawable.ic_stat_notif)
        .setAutoCancel(true)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
        .build()
    androidx.core.app.NotificationManagerCompat.from(ctx).notify(9999, n)
}
