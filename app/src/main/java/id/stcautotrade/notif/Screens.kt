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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Data samaran resto ────────────────────────────────────────────────────────
private data class HPromo(val emoji: String, val title: String, val sub: String, val a: Color, val b: Color)
private val HOME_PROMOS = listOf(
    HPromo("🔥", "Diskon 50%", "Menu pilihan hari ini", AppColors.red, AppColors.amber),
    HPromo("🚚", "Gratis Ongkir", "Min. belanja Rp 50rb", AppColors.blue, Color(0xFF5AC8FA)),
    HPromo("💸", "Cashback 20%", "Bayar via e-wallet", AppColors.green, Color(0xFF30D158)),
)
private data class HFeat(val emoji: String, val name: String, val old: String, val price: String, val rating: String)
private val HOME_FEATURED = listOf(
    HFeat("🍔", "Beef Burger", "Rp 45.000", "Rp 32.000", "4.9"),
    HFeat("🍗", "Ayam Geprek", "Rp 30.000", "Rp 22.000", "4.8"),
    HFeat("🍕", "Pizza Beef", "Rp 75.000", "Rp 55.000", "4.7"),
    HFeat("🍜", "Mie Bakso", "Rp 30.000", "Rp 24.000", "4.8"),
)

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

// Kategori "Dapur": SATU yang `real` (burger) membuka panel; sisanya decoy.
private data class KTile(val emoji: String, val label: String, val real: Boolean = false)
private val KITCHEN_TILES = listOf(
    KTile("🍜", "Mie & Bakso"),
    KTile("🍔", "Burger", real = true),
    KTile("🍗", "Ayam"),
    KTile("🍕", "Pizza"),
    KTile("🍛", "Nasi Box"),
    KTile("🥤", "Minuman"),
    KTile("🍰", "Dessert"),
    KTile("🥗", "Menu Sehat"),
    KTile("🧾", "Pesanan"),
)

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

// ── BERANDA — home food-delivery ─────────────────────────────────────────────
@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        // Top bar: sapaan + lokasi antar + lonceng
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Halo, selamat datang 👋", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Antar ke: Rumah", style = MaterialTheme.typography.titleMedium)
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(21.dp))
                }
            }
        }

        // Search
        Surface(
            color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Cari menu favoritmu…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        // Promo carousel
        Spacer(Modifier.height(10.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 4.dp)) {
            HOME_PROMOS.forEach { p -> PromoCard(p); Spacer(Modifier.width(12.dp)) }
        }

        // Kategori
        SectionHeaderRow("Kategori")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            CATEGORIES.forEach { (emoji, name) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 14.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), shape = RoundedCornerShape(20.dp)) {
                        Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 28.sp) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Lagi diskon (featured horizontal)
        SectionHeaderRow("Lagi Diskon 🔥", "Lihat semua")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 4.dp)) {
            HOME_FEATURED.forEach { f -> FeaturedCard(f); Spacer(Modifier.width(12.dp)) }
        }

        // Menu favorit (vertikal)
        SectionHeaderRow("Menu Favorit", "Lihat semua")
        FAVORITES.forEach { d -> MenuDishCard(d) }
    }
}

@Composable
private fun SectionHeaderRow(title: String, action: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PromoCard(p: HPromo) {
    Box(
        Modifier.width(280.dp).height(118.dp).clip(MaterialTheme.shapes.large)
            .background(Brush.linearGradient(listOf(p.a, p.b))).padding(18.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Text("${p.emoji}  ${p.title}", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(3.dp))
            Text(p.sub, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
            Spacer(Modifier.weight(1f))
            Surface(color = Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(20.dp)) {
                Text("Klaim sekarang", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
            }
        }
    }
}

@Composable
private fun FeaturedCard(f: HFeat) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(152.dp)) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(92.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(f.emoji, fontSize = 46.sp)
                Surface(color = AppColors.red, shape = RoundedCornerShape(bottomEnd = 10.dp), modifier = Modifier.align(Alignment.TopStart)) {
                    Text("HEMAT", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                }
            }
            Column(Modifier.padding(11.dp)) {
                Text(f.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text("⭐ ${f.rating}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(f.old, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                    Spacer(Modifier.width(6.dp))
                    Text(f.price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
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
        // Landing dapur: grid kategori seragam. Tombol ASLI (burger) berbaur —
        // decoy lain hanya menampilkan toast wajar, agar penyamaran lebih dalam.
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            Box(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(accentBrush()).padding(20.dp),
            ) {
                Column {
                    Text("Dapur Hari Ini", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text("Pilih kategori menu untuk mulai memesan 🍽️", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
                }
            }

            SectionLabel("Kategori Menu")
            KITCHEN_TILES.chunked(3).forEach { rowTiles ->
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowTiles.forEach { t ->
                        KitchenTile(t, Modifier.weight(1f)) {
                            if (t.real) opened = true
                            else android.widget.Toast.makeText(ctx, "Menu ${t.label} segera hadir 🍽️", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    repeat(3 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Ketuk kategori untuk lihat menu & pesan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(20.dp))
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
private fun KitchenTile(t: KTile, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.clickable { onClick() },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(t.emoji, fontSize = 30.sp)
            Spacer(Modifier.height(8.dp))
            Text(t.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
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
            "Dapur Hari Ini v2.6",
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
