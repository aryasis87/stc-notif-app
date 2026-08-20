package id.stcautotrade.notif

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Data samaran resto ────────────────────────────────────────────────────────
private data class MenuDish(val emoji: String, val name: String, val desc: String, val price: String, val rating: String)
private val FAVORITES = listOf(
    MenuDish("🍔", "Beef Burger Spesial", "Daging sapi panggang, keju cheddar, saus signature", "Rp 32.000", "4.9"),
    MenuDish("🍜", "Mie Ayam Bakso", "Mie kenyal, ayam kecap, bakso urat, pangsit renyah", "Rp 24.000", "4.8"),
    MenuDish("🍗", "Ayam Geprek Sambal", "Ayam crispy, sambal bawang level 1–5, nasi hangat", "Rp 22.000", "4.9"),
    MenuDish("🍕", "Pizza Beef Pepperoni", "Mozzarella, pepperoni, saus tomat rumahan", "Rp 55.000", "4.7"),
    MenuDish("🍛", "Nasi Goreng Kampung", "Nasi goreng terasi, telur, kerupuk, acar segar", "Rp 20.000", "4.8"),
    MenuDish("🥤", "Es Teh Manis Jumbo", "Teh segar diseduh, gula aren, es batu", "Rp 8.000", "4.9"),
)
private val CATEGORIES = listOf("🍔" to "Burger", "🍜" to "Mie", "🍗" to "Ayam", "🍕" to "Pizza", "🥤" to "Minuman", "🍰" to "Dessert")

// ── Komponen bersama ─────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
    )
}

@Composable
private fun accentBrush() = Brush.linearGradient(
    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)),
)

// ── BERANDA — landing resto ──────────────────────────────────────────────────
@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        // Hero
        Box(
            Modifier.fillMaxWidth().padding(16.dp).clip(MaterialTheme.shapes.large)
                .background(accentBrush()).padding(20.dp),
        ) {
            Column {
                Text("Dapur Hari Ini", style = MaterialTheme.typography.displaySmall, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Masakan rumahan, diantar hangat 🍲", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.92f))
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("⭐ 4.9")
                    Spacer(Modifier.width(8.dp))
                    Pill("1.2rb+ ulasan")
                    Spacer(Modifier.width(8.dp))
                    Pill("Buka 09.00–22.00")
                }
            }
        }

        // Search palsu (dekoratif)
        Surface(
            color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(10.dp))
                Text("Cari menu favoritmu…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Kategori
        SectionLabel("Kategori")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            CATEGORIES.forEach { (emoji, name) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(18.dp),
                    ) { Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 28.sp) } }
                    Spacer(Modifier.height(6.dp))
                    Text(name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Promo banner
        Box(
            Modifier.fillMaxWidth().padding(16.dp).clip(MaterialTheme.shapes.medium)
                .background(Brush.linearGradient(listOf(AppColors.amber, AppColors.red)))
                .padding(18.dp),
        ) {
            Column {
                Text("Promo Spesial Hari Ini 🔥", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(3.dp))
                Text("Diskon 30% untuk menu pilihan · gratis ongkir min. Rp 50.000", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
            }
        }

        // Menu favorit
        SectionLabel("Menu Favorit")
        FAVORITES.forEach { d -> MenuDishCard(d) }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Pill(text: String) {
    Surface(color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(20.dp)) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
    }
}

@Composable
private fun MenuDishCard(d: MenuDish) {
    Surface(
        color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = MaterialTheme.shapes.small) {
                Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) { Text(d.emoji, fontSize = 30.sp) }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(d.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(d.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(d.price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("⭐ ${d.rating}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) {
                Text("Pesan", color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
    }
}

// ── NOTIFIKASI — promo & pesanan ─────────────────────────────────────────────
@Composable
fun NotificationsScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var notifs by remember { mutableStateOf(prefs.history()) }
    var running by remember { mutableStateOf(prefs.running) }

    LaunchedEffect(Unit) {
        while (true) {
            notifs = prefs.history(); running = prefs.running
            delay(2500)
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Promo & Pesanan", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(3.dp))
                Text(if (running) "Notifikasi promo aktif" else "Notifikasi dijeda", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {
                if (running) NotifierService.stop(ctx) else NotifierService.start(ctx)
                running = !running
            }) {
                Icon(if (running) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle, "Notifikasi", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
        }
        if (notifs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍽️", fontSize = 44.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada promo", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Promo & update pesanan akan muncul di sini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val (emoji, tint) = when (it.type) {
        "deposit" -> "🍔" to AppColors.green
        "penarikan" -> "🏷️" to AppColors.red
        "aktivasi" -> "🍽️" to AppColors.blue
        else -> "🔔" to MaterialTheme.colorScheme.primary
    }
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = tint.copy(alpha = 0.14f), shape = MaterialTheme.shapes.small) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 21.sp) }
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

// ── DAPUR (landing → tombol → WebView) ───────────────────────────────────────
@Composable
fun DashboardScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var opened by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(opened) {
        if (opened) {
            val cookie = Api.authCookie(prefs.baseUrl, prefs.password)
            if (cookie != null) {
                CookieManager.getInstance().apply {
                    setAcceptCookie(true); setCookie(prefs.baseUrl, "$cookie; Path=/"); flush()
                }
            }
            ready = true
        }
    }

    if (!opened) {
        // Landing dapur + tombol burger
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(accentBrush()).padding(22.dp),
            ) {
                Column {
                    Text("Dapur Digital 👨‍🍳", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text("Kelola menu, pesanan, & laporan penjualan dapurmu di satu tempat.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("🍔", fontSize = 72.sp)
            Spacer(Modifier.height(10.dp))
            Text("Panel Dapur", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text("Ketuk tombol di bawah untuk membuka panel lengkap.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Spacer(Modifier.height(26.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.clickable { opened = true },
            ) {
                Row(Modifier.padding(horizontal = 26.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🍔", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("Buka Dapur", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(30.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MiniStat("🧾", "Pesanan")
                MiniStat("📦", "Stok")
                MiniStat("📊", "Laporan")
            }
        }
    } else {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Panel Dapur", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
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
}

@Composable
private fun MiniStat(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 26.sp) }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── SETELAN — samaran resto ──────────────────────────────────────────────────
@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var running by remember { mutableStateOf(prefs.running) }

    LaunchedEffect(Unit) { while (true) { running = prefs.running; delay(2000) } }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        Text("Setelan Dapur", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp))

        SubLabel("Promo & Pesanan")
        GroupCard {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sinkron promo & pesanan", style = MaterialTheme.typography.bodyLarge)
                    Text(if (running) "Aktif — notifikasi jalan" else "Dijeda", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        SubLabel("Notifikasi HP")
        GroupCard {
            TapRow("Tes notifikasi promo", Icons.Filled.NotificationsActive) { sendTestNotif(ctx) }
            HorizontalDivider(Modifier.padding(start = 52.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            TapRow("Izinkan jalan di latar", Icons.Filled.BatteryChargingFull, last = true) {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
                } catch (e: Exception) {
                    try { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) {}
                }
            }
        }

        SubLabel("Akun")
        GroupCard {
            TapRow("Keluar", Icons.Filled.Logout, destructive = true, last = true) { onLogout() }
        }

        Text(
            "Dapur Hari Ini v2.5",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SubLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 32.dp, top = 18.dp, bottom = 7.dp),
    )
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) { Column(content = content) }
}

@Composable
private fun InfoRow(label: String, value: String, last: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
    if (!last) HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
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
