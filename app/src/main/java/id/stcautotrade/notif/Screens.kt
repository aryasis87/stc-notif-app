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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

// ── Komponen bersama (iOS grouped) ───────────────────────────────────────────
@Composable
fun LargeHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.displaySmall)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (action != null) action()
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 32.dp, top = 18.dp, bottom = 7.dp),
    )
}

@Composable
fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) { Column(content = content) }
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
    if (!last) HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun RowScope.StatTile(label: String, value: String, valueColor: Color) {
    Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = valueColor)
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatRow(a: @Composable RowScope.() -> Unit, b: @Composable RowScope.() -> Unit, last: Boolean = false) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        a()
        VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
        b()
    }
    if (!last) HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
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

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        LargeHeader("Beranda", if (updated.isEmpty()) "Ringkasan STC & KOALA" else "Diperbarui $updated") {
            IconButton(onClick = { load() }) { Icon(Icons.Filled.Refresh, "Muat ulang", tint = MaterialTheme.colorScheme.primary) }
        }
        if (loading && data == null) {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (data == null) {
            SectionLabel("Kesalahan")
            GroupCard { InfoRow("Gagal memuat data", "coba lagi", MaterialTheme.colorScheme.error, last = true) }
        } else {
            data!!.forEach { b ->
                SectionLabel(b.label)
                if (b.error != null) {
                    GroupCard { InfoRow("Kesalahan", b.error, MaterialTheme.colorScheme.error, last = true) }
                } else {
                    GroupCard {
                        StatRow(
                            { StatTile("Total user", n(b.total), MaterialTheme.colorScheme.onSurface) },
                            { StatTile("Aktif", n(b.active), AppColors.green) },
                        )
                        StatRow(
                            { StatTile("Login 24 jam", n(b.recent24h), MaterialTheme.colorScheme.onSurface) },
                            { StatTile("User baru 24 jam", n(b.recentAdded24h), MaterialTheme.colorScheme.onSurface) },
                        )
                        StatRow(
                            { StatTile("Sesi aktif", "${n(b.activeSessions)}/${n(b.totalSessions)}", MaterialTheme.colorScheme.onSurface) },
                            { StatTile("Mode jalan", n(b.running), MaterialTheme.colorScheme.primary) },
                            last = true,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(26.dp))
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

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        LargeHeader("Notifikasi", if (running) "Menyinkronkan · $status" else "Berhenti") {
            IconButton(onClick = {
                if (running) NotifierService.stop(ctx) else NotifierService.start(ctx)
                running = !running
            }) {
                Icon(if (running) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle, "Pantau", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
        }
        if (notifs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(50.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada promo", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Promo menu terbaru akan muncul di sini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 20.dp)) {
                items(notifs) { it -> NotifRow(it) }
            }
        }
    }
}

@Composable
private fun NotifRow(it: HistoryItem) {
    val (icon, tint) = when (it.type) {
        "deposit" -> Icons.Filled.Fastfood to AppColors.green
        "penarikan" -> Icons.Filled.LocalOffer to AppColors.red
        "aktivasi" -> Icons.Filled.RestaurantMenu to AppColors.blue
        else -> Icons.Filled.Restaurant to MaterialTheme.colorScheme.primary
    }
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = tint.copy(alpha = 0.14f), shape = MaterialTheme.shapes.small) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)) }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(it.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(it.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(fmtTime(it.ts), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun fmtTime(ts: Long): String = SimpleDateFormat("dd MMM · HH:mm", Locale("id")).format(Date(ts))

// ── DASHBOARD (WebView auto-login) ───────────────────────────────────────────
@Composable
fun DashboardScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var ready by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) {
        val cookie = Api.authCookie(prefs.baseUrl, prefs.password)
        if (cookie != null) {
            CookieManager.getInstance().apply {
                setAcceptCookie(true); setCookie(prefs.baseUrl, "$cookie; Path=/"); flush()
            }
        }
        ready = true
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Dashboard", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = { webView?.reload() }) { Icon(Icons.Filled.Refresh, "Muat ulang", tint = MaterialTheme.colorScheme.primary) }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
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

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        LargeHeader("Setelan")

        SectionLabel("Pemantauan")
        GroupCard {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pantau di latar", style = MaterialTheme.typography.bodyLarge)
                    Text(if (running) "Aktif — notifikasi jalan" else "Berhenti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = running,
                    onCheckedChange = {
                        if (it) NotifierService.start(ctx) else NotifierService.stop(ctx)
                        running = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppColors.green,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }
            HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            InfoRow("Interval cek", "${prefs.intervalSec} detik", last = true)
        }

        SectionLabel("Notifikasi HP")
        GroupCard {
            TapRow("Kirim notifikasi tes", Icons.Filled.NotificationsActive) { sendTestNotif(ctx) }
            HorizontalDivider(Modifier.padding(start = 52.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            TapRow("Abaikan optimasi baterai", Icons.Filled.BatteryChargingFull, last = true) {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
                } catch (e: Exception) {
                    try { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) {}
                }
            }
        }

        SectionLabel("Akun")
        GroupCard {
            TapRow("Keluar", Icons.Filled.Logout, destructive = true, last = true) { onLogout() }
        }

        Text(
            "Menu Resto v2.4",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TapRow(label: String, icon: ImageVector, destructive: Boolean = false, last: Boolean = false, onClick: () -> Unit) {
    val iconTint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val labelColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = labelColor, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
    if (!last) HorizontalDivider(Modifier.padding(start = 52.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
}

private fun sendTestNotif(ctx: android.content.Context) {
    val nb = androidx.core.app.NotificationCompat.Builder(ctx, App.CH_ALERT)
        .setContentTitle("Nasi goreng spesial")
        .setContentText("Rp25.000 — promo tes, notifikasi aktif! 🍽️")
        .setSmallIcon(R.drawable.ic_stat_notif)
        .setAutoCancel(true)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
        .build()
    androidx.core.app.NotificationManagerCompat.from(ctx).notify(9999, nb)
}
