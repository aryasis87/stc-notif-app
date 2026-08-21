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

// ═════════════════════════════════════════════════════════════════════════════
//  CIKILUKS CHICKEN — samaran aplikasi pesan-antar ayam goreng.
//  Warna merek: oranye-merah hangat (amber → red). Konten dibuat realistik.
// ═════════════════════════════════════════════════════════════════════════════

private const val BRAND = "Cikiluks Chicken"

@Composable
private fun brandBrush() = Brush.linearGradient(listOf(AppColors.amber, AppColors.red))

// ── Data samaran ─────────────────────────────────────────────────────────────
private data class HPromo(val emoji: String, val title: String, val sub: String, val code: String, val a: Color, val b: Color)
private val HPROMOS = listOf(
    HPromo("🔥", "Diskon 50%", "Semua paket ayam, hari ini saja", "CIKILUKS50", AppColors.red, AppColors.amber),
    HPromo("🚚", "Gratis Ongkir", "Min. belanja Rp 40.000", "ONGKIRGRATIS", AppColors.blue, Color(0xFF5AC8FA)),
    HPromo("💸", "Cashback 20%", "Bayar pakai e-wallet", "WALLET20", AppColors.green, Color(0xFF30D158)),
    HPromo("🎁", "Beli 2 Gratis 1", "Ayam crispy pilihan", "BELI2GRATIS1", Color(0xFF8E44AD), Color(0xFFBB6BD9)),
)

private data class HCat(val emoji: String, val name: String)
private val CATEGORIES = listOf(
    HCat("🍗", "Ayam Goreng"), HCat("🌶️", "Geprek"), HCat("🍟", "Crispy"),
    HCat("🍱", "Paket"), HCat("🍚", "Nasi"), HCat("🥫", "Sambal"),
    HCat("🥤", "Minuman"), HCat("🍰", "Dessert"),
)

private data class Flash(val emoji: String, val name: String, val old: String, val price: String, val sold: Int, val stock: Int)
private val FLASH = listOf(
    Flash("🍗", "Paket Ayam 3 Pcs", "Rp 45.000", "Rp 29.900", 182, 40),
    Flash("🌶️", "Geprek Keju Mozzarella", "Rp 32.000", "Rp 22.900", 264, 25),
    Flash("🍔", "Chicken Burger Jumbo", "Rp 38.000", "Rp 25.900", 97, 60),
    Flash("🍟", "Crispy Wings 6 Pcs", "Rp 40.000", "Rp 27.900", 143, 33),
)

private data class Bundle(val emoji: String, val name: String, val items: String, val old: String, val price: String)
private val BUNDLES = listOf(
    Bundle("👨‍👩‍👧", "Paket Keluarga", "8 ayam + 4 nasi + 4 es teh", "Rp 145.000", "Rp 99.000"),
    Bundle("🧑‍🤝‍🧑", "Paket Berdua", "4 ayam + 2 nasi + 2 minuman", "Rp 78.000", "Rp 55.000"),
    Bundle("🍱", "Paket Hemat Solo", "2 ayam + nasi + es teh", "Rp 38.000", "Rp 27.000"),
)

private data class HDish(
    val emoji: String, val name: String, val desc: String, val price: String,
    val rating: String, val sold: String, val tag: String, val spicy: Int = 0,
)
private val MENU = listOf(
    HDish("🍗", "Ayam Goreng Original", "Ayam kampung ungkep bumbu rahasia, digoreng garing keemasan", "Rp 12.000", "4.9", "5rb+ terjual", "TERLARIS"),
    HDish("🌶️", "Ayam Geprek Sambal Bawang", "Ayam crispy digeprek sambal bawang, level 1–5, nasi hangat", "Rp 18.000", "4.9", "8rb+ terjual", "PEDAS", spicy = 3),
    HDish("🧀", "Geprek Keju Mozzarella", "Ayam geprek disiram lelehan mozzarella & saus creamy", "Rp 22.900", "4.8", "3rb+ terjual", "FAVORIT", spicy = 2),
    HDish("🍟", "Crispy Wings 6 Pcs", "Sayap ayam balur tepung renyah, saus madu / pedas manis", "Rp 27.900", "4.8", "2rb+ terjual", "RENYAH"),
    HDish("🍔", "Chicken Burger Jumbo", "Patty ayam crispy tebal, keju, selada, saus signature", "Rp 25.900", "4.7", "1rb+ terjual", "BARU"),
    HDish("🍚", "Nasi + Ayam + Sambal", "Nasi pulen, ayam pilihan, sambal cikiluks, lalapan segar", "Rp 20.000", "4.9", "6rb+ terjual", "HEMAT"),
    HDish("🥤", "Es Teh Manis Jumbo", "Teh segar diseduh, gula aren, gelas jumbo 650ml", "Rp 6.000", "4.9", "9rb+ terjual", "SEGER"),
)

private data class Review(val name: String, val stars: Int, val text: String, val ago: String)
private val REVIEWS = listOf(
    Review("Rina S.", 5, "Ayamnya kriuk banget, sambalnya nampol! Langganan tiap minggu 😍", "2 jam lalu"),
    Review("Budi P.", 5, "Porsi banyak, harga bersahabat. Datang masih anget. Recommended!", "5 jam lalu"),
    Review("Dewi A.", 4, "Enak, cuma nunggu agak lama pas jam makan siang. Rasanya juara.", "kemarin"),
)

// Kategori "Dapur": SATU yang `real` (paket spesial) membuka panel; sisanya decoy.
private data class KTile(val emoji: String, val label: String, val real: Boolean = false)
private val KITCHEN_TILES = listOf(
    KTile("🍗", "Ayam Goreng"),
    KTile("🌶️", "Geprek"),
    KTile("🍱", "Paket Spesial", real = true),
    KTile("🍟", "Crispy"),
    KTile("🍚", "Nasi Box"),
    KTile("🥤", "Minuman"),
    KTile("🥫", "Sambal"),
    KTile("🍰", "Dessert"),
    KTile("🧾", "Pesanan"),
)

// ── Komponen bersama ─────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp))
}

@Composable
private fun SectionHeaderRow(title: String, action: String? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() },
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
    }
}

@Composable
private fun Stars(n: Int, size: Int = 13) {
    Row { repeat(5) { i ->
        Icon(if (i < n) Icons.Filled.Star else Icons.Filled.StarBorder, null,
            tint = AppColors.amber, modifier = Modifier.size(size.dp))
    } }
}

// ── BERANDA ──────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen() {
    var cat by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 28.dp)) {

        // Top bar merek + lokasi + keranjang
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(brandBrush()), contentAlignment = Alignment.Center) {
                Text("🍗", fontSize = 20.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(BRAND, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, null, tint = AppColors.red, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Antar ke: Rumah", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                }
            }
            BadgeIcon(Icons.Filled.NotificationsNone, "3")
            Spacer(Modifier.width(4.dp))
            BadgeIcon(Icons.Filled.ShoppingCart, "2")
        }

        // Search
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Cari ayam geprek, paket, minuman…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        // Strip kepercayaan (rating · pesanan · antar)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            TrustCell("⭐ 4.9", "1.240 ulasan", Modifier.weight(1f))
            TrustCell("🛵 20 mnt", "estimasi antar", Modifier.weight(1f))
            TrustCell("🔥 12rb+", "pesanan/bln", Modifier.weight(1f))
        }

        // HPromo carousel
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 4.dp)) {
            HPROMOS.forEach { p -> HPromoCard(p); Spacer(Modifier.width(12.dp)) }
        }

        // Flash sale + hitung mundur
        FlashSaleHeader()
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 4.dp)) {
            FLASH.forEach { f -> FlashCard(f); Spacer(Modifier.width(12.dp)) }
        }

        // Kategori
        SectionHeaderRow("Kategori")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            CATEGORIES.forEachIndexed { i, c ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 14.dp).clickable { cat = i }) {
                    Surface(color = if (cat == i) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        border = if (cat == i) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null) {
                        Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) { Text(c.emoji, fontSize = 26.sp) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(c.name, style = MaterialTheme.typography.labelSmall,
                        color = if (cat == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Paket hemat (bundles)
        SectionHeaderRow("Paket Hemat 🍱", "Lihat semua")
        BUNDLES.forEach { b -> BundleCard(b) }

        // Best seller
        SectionHeaderRow("Best Seller 👑", "Lihat semua")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 4.dp)) {
            FLASH.forEach { f -> FlashCard(f, mini = true); Spacer(Modifier.width(12.dp)) }
        }

        // Menu lengkap
        SectionHeaderRow("Menu Cikiluks", "Semua")
        MENU.forEach { d -> HDishCard(d) }

        // Ulasan pelanggan
        SectionHeaderRow("Ulasan Pelanggan ⭐")
        REVIEWS.forEach { r -> ReviewCard(r) }

        // Info toko
        Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                InfoLine(Icons.Filled.Schedule, "Buka setiap hari 10.00 – 22.00 WIB")
                Spacer(Modifier.height(9.dp))
                InfoLine(Icons.Filled.Place, "Jl. Merdeka No. 12, dekat alun-alun kota")
                Spacer(Modifier.height(9.dp))
                InfoLine(Icons.Filled.Verified, "100% ayam segar, digoreng saat dipesan")
            }
        }
    }
}

@Composable
private fun BadgeIcon(icon: ImageVector, count: String) {
    Box {
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
        }
        Surface(color = AppColors.red, shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp)) {
            Text(count, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
        }
    }
}

@Composable
private fun TrustCell(big: String, small: String, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small, modifier = modifier.padding(horizontal = 3.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(big, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(small, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HPromoCard(p: HPromo) {
    Box(Modifier.width(285.dp).height(122.dp).clip(MaterialTheme.shapes.large).background(Brush.linearGradient(listOf(p.a, p.b))).padding(18.dp)) {
        Column(Modifier.fillMaxSize()) {
            Text("${p.emoji}  ${p.title}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(p.sub, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(8.dp)) {
                    Text("KODE: ${p.code}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                Surface(color = Color.White, shape = RoundedCornerShape(20.dp)) {
                    Text("Klaim", color = p.a, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun FlashSaleHeader() {
    var secs by remember { mutableStateOf(2 * 3600 + 45 * 60 + 12) }
    LaunchedEffect(Unit) { while (true) { delay(1000); if (secs > 0) secs-- } }
    val h = secs / 3600; val m = (secs % 3600) / 60; val s = secs % 60
    fun pad(x: Int) = x.toString().padStart(2, '0')
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 22.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.FlashOn, null, tint = AppColors.red, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(5.dp))
        Text("Flash Sale", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        Text("Berakhir ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf(pad(h), pad(m), pad(s)).forEachIndexed { i, part ->
            if (i > 0) Text(":", fontWeight = FontWeight.Bold, color = AppColors.red)
            Surface(color = AppColors.red, shape = RoundedCornerShape(6.dp)) {
                Text(part, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun FlashCard(f: Flash, mini: Boolean = false) {
    val w = if (mini) 140.dp else 160.dp
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(w)) {
        Column {
            Box(Modifier.fillMaxWidth().height(if (mini) 84.dp else 96.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Text(f.emoji, fontSize = if (mini) 40.sp else 48.sp)
                Surface(color = AppColors.red, shape = RoundedCornerShape(bottomEnd = 10.dp), modifier = Modifier.align(Alignment.TopStart)) {
                    Text("HEMAT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(f.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(f.old, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                    Spacer(Modifier.width(5.dp))
                    Text(f.price, style = MaterialTheme.typography.titleSmall, color = AppColors.red, fontWeight = FontWeight.Bold)
                }
                if (!mini) {
                    Spacer(Modifier.height(6.dp))
                    // Bar stok terjual
                    val frac = (f.sold.toFloat() / (f.sold + f.stock)).coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
                        Box(Modifier.fillMaxWidth(frac).height(6.dp).clip(RoundedCornerShape(3.dp)).background(brandBrush()))
                    }
                    Spacer(Modifier.height(3.dp))
                    Text("Terjual ${f.sold} · sisa ${f.stock}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BundleCard(b: Bundle) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(60.dp).clip(MaterialTheme.shapes.small).background(brandBrush()), contentAlignment = Alignment.Center) { Text(b.emoji, fontSize = 30.sp) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(b.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(b.items, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(b.old, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                    Spacer(Modifier.width(6.dp))
                    Text(b.price, style = MaterialTheme.typography.titleMedium, color = AppColors.red, fontWeight = FontWeight.Bold)
                }
            }
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) {
                Text("Pesan", color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun HDishCard(d: HDish) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Row(Modifier.padding(12.dp)) {
            Box {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = MaterialTheme.shapes.small) {
                    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) { Text(d.emoji, fontSize = 36.sp) }
                }
                Surface(color = tagColor(d.tag), shape = RoundedCornerShape(bottomEnd = 8.dp), modifier = Modifier.align(Alignment.TopStart)) {
                    Text(d.tag, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(d.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(d.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${d.rating}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text("· ${d.sold}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (d.spicy > 0) { Spacer(Modifier.width(8.dp)); Text("🌶️".repeat(d.spicy), fontSize = 10.sp) }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(d.price, style = MaterialTheme.typography.titleMedium, color = AppColors.red, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Tambah", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun tagColor(tag: String): Color = when (tag) {
    "TERLARIS", "FAVORIT" -> AppColors.red
    "PEDAS" -> Color(0xFFE74C3C)
    "BARU" -> AppColors.blue
    "HEMAT", "SEGER" -> AppColors.green
    else -> AppColors.amber
}

@Composable
private fun ReviewCard(r: Review) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text(r.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Stars(r.stars, size = 12)
                }
                Text(r.ago, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(r.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── PROMO (Notifikasi asli, disamarkan sebagai inbox promo) ───────────────────
private data class Voucher(val emoji: String, val title: String, val sub: String, val exp: String, val c: Color)
private val VOUCHERS = listOf(
    Voucher("🎟️", "Diskon Rp 15.000", "Min. Rp 50.000", "s/d 30 Sep", AppColors.red),
    Voucher("🚚", "Gratis Ongkir 3x", "Tanpa min. belanja", "s/d 25 Sep", AppColors.blue),
    Voucher("💰", "Cashback 25%", "Bayar e-wallet", "s/d 28 Sep", AppColors.green),
)

@Composable
fun NotificationsScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var notifs by remember { mutableStateOf(prefs.history()) }
    var running by remember { mutableStateOf(prefs.running) }
    var filter by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { while (true) { notifs = prefs.history(); running = prefs.running; delay(2500) } }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("HPromo & Pesanan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(if (running) "Notifikasi promo aktif" else "Notifikasi dijeda", style = MaterialTheme.typography.bodySmall,
                    color = if (running) AppColors.green else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {
                if (running) NotifierService.stop(ctx) else NotifierService.start(ctx); running = !running
            }) {
                Icon(if (running) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle, "Notifikasi", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
        }

        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 24.dp)) {
            // Voucher saya (dekoratif)
            item {
                Text("🎟️ Voucher Saya", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 20.dp, top = 6.dp, bottom = 8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 4.dp, bottom = 6.dp)) {
                    VOUCHERS.forEach { v -> VoucherCard(v); Spacer(Modifier.width(12.dp)) }
                }
            }
            // Filter
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 16.dp, top = 10.dp, bottom = 4.dp)) {
                    listOf("Semua", "HPromo", "Pesanan", "Info").forEachIndexed { i, f ->
                        Chip(f, filter == i) { filter = i }; Spacer(Modifier.width(8.dp))
                    }
                }
                Text("📥 Kotak Masuk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp))
            }
            if (notifs.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍗", fontSize = 46.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Belum ada notifikasi", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("HPromo & update pesanan akan muncul di sini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(notifs) { it -> Box(Modifier.padding(horizontal = 16.dp)) { NotifRow(it) } }
            }
        }
    }
}

@Composable
private fun VoucherCard(v: Voucher) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(220.dp)) {
        Row(Modifier.height(78.dp)) {
            Box(Modifier.width(60.dp).fillMaxHeight().background(v.c.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Text(v.emoji, fontSize = 28.sp) }
            Column(Modifier.padding(10.dp).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(v.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = v.c)
                Text(v.sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text("Berlaku ${v.exp}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NotifRow(it: HistoryItem) {
    val (emoji, tint) = when (it.type) {
        "deposit" -> "🍗" to AppColors.green
        "penarikan" -> "🏷️" to AppColors.red
        "aktivasi" -> "🎁" to AppColors.blue
        else -> "🔔" to MaterialTheme.colorScheme.primary
    }
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = tint.copy(alpha = 0.14f), shape = MaterialTheme.shapes.small) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 21.sp) }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(it.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                CookieManager.getInstance().apply { setAcceptCookie(true); setCookie(prefs.baseUrl, "$cookie; Path=/"); flush() }
            }
            ready = true
        }
    }

    if (!opened) {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Hero dapur
            Box(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(brandBrush()).padding(20.dp)) {
                Column {
                    Text("Dapur $BRAND", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Semua digoreng fresh saat dipesan 🍗", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.95f))
                    Spacer(Modifier.height(12.dp))
                    Row {
                        DapurStat("128", "porsi hari ini", Modifier.weight(1f))
                        DapurStat("4.9★", "rating dapur", Modifier.weight(1f))
                        DapurStat("~12'", "waktu masak", Modifier.weight(1f))
                    }
                }
            }

            // Status pesanan berjalan (dekoratif)
            Spacer(Modifier.height(14.dp))
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.amber.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) { Text("👨‍🍳", fontSize = 22.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pesanan #CK-2481 sedang dimasak", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Estimasi siap 8 menit lagi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionLabel("Kategori Dapur")
            KITCHEN_TILES.chunked(3).forEach { rowTiles ->
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowTiles.forEach { t ->
                        KitchenTile(t, Modifier.weight(1f)) {
                            if (t.real) opened = true
                            else android.widget.Toast.makeText(ctx, "Menu ${t.label} segera hadir 🍗", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    repeat(3 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            // Menu andalan dapur
            SectionLabel("Andalan Chef 👨‍🍳")
            listOf(
                Triple("🌶️", "Geprek Level 5", "Buat yang doyan super pedas"),
                Triple("🍗", "Ayam Bakar Madu", "Manis gurih, dipanggang bara"),
            ).forEach { (e, t, s) ->
                Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) { Text(e, fontSize = 26.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(s, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Ketuk kategori untuk lihat menu & pesan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(20.dp))
        }
    } else {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Panel Dapur", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { webView?.reload() }) { Icon(Icons.Filled.Refresh, "Muat ulang", tint = MaterialTheme.colorScheme.primary) }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Box(Modifier.fillMaxSize()) {
                if (ready) {
                    AndroidView(modifier = Modifier.fillMaxSize(), factory = { c ->
                        WebView(c).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            loadUrl(prefs.baseUrl + "/")
                            webView = this
                        }
                    })
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
            }
        }
    }
}

@Composable
private fun DapurStat(big: String, small: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(end = 4.dp)) {
        Text(big, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Text(small, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun KitchenTile(t: KTile, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = modifier.clickable { onClick() }) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(t.emoji, fontSize = 30.sp)
            Spacer(Modifier.height(8.dp))
            Text(t.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── SETELAN — profil aplikasi makanan ────────────────────────────────────────
@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var running by remember { mutableStateOf(prefs.running) }

    LaunchedEffect(Unit) { while (true) { running = prefs.running; delay(2000) } }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 28.dp)) {
        Text("Akun Saya", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 10.dp))

        // Kartu profil
        Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(27.dp)).background(brandBrush()), contentAlignment = Alignment.Center) { Text("🍗", fontSize = 26.sp) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pelanggan Setia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = AppColors.amber.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp)) {
                                Text("👑 GOLD", color = AppColors.amber, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("member sejak 2024", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                    ProfileStat("48", "Pesanan", Modifier.weight(1f))
                    VDiv()
                    ProfileStat("1.250", "Poin", Modifier.weight(1f))
                    VDiv()
                    ProfileStat("3", "Voucher", Modifier.weight(1f))
                }
            }
        }

        SubLabel("Pesanan & Belanja")
        GroupCard {
            TapRow("Riwayat pesanan", Icons.Filled.ReceiptLong) {}
            HDiv(); TapRow("Alamat tersimpan", Icons.Filled.Place) {}
            HDiv(); TapRow("Metode pembayaran", Icons.Filled.CreditCard) {}
            HDiv(); TapRow("Favorit saya", Icons.Filled.Favorite, last = true) {}
        }

        SubLabel("HPromo & Notifikasi")
        GroupCard {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sinkron promo & pesanan", style = MaterialTheme.typography.bodyLarge)
                    Text(if (running) "Aktif — notifikasi jalan" else "Dijeda", style = MaterialTheme.typography.bodySmall,
                        color = if (running) AppColors.green else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = running, onCheckedChange = {
                    if (it) NotifierService.start(ctx) else NotifierService.stop(ctx); running = it
                }, colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, checkedTrackColor = AppColors.green, checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = Color.White, uncheckedTrackColor = MaterialTheme.colorScheme.outline, uncheckedBorderColor = Color.Transparent,
                ))
            }
            HDiv(); InfoRow("Interval cek", "${prefs.intervalSec} detik", last = true)
        }

        SubLabel("Notifikasi HP")
        GroupCard {
            TapRow("Tes notifikasi promo", Icons.Filled.NotificationsActive) { sendTestNotif(ctx) }
            HDiv(52)
            TapRow("Izinkan jalan di latar", Icons.Filled.BatteryChargingFull, last = true) {
                try { ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))) }
                catch (e: Exception) { try { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) {} }
            }
        }

        SubLabel("Bantuan")
        GroupCard {
            TapRow("Pusat bantuan (FAQ)", Icons.Filled.HelpOutline) {}
            HDiv(52); TapRow("Hubungi kami", Icons.Filled.ChatBubbleOutline) {}
            HDiv(52); TapRow("Tentang $BRAND", Icons.Filled.Info, last = true) {}
        }

        SubLabel("Akun")
        GroupCard { TapRow("Keluar", Icons.Filled.Logout, destructive = true, last = true) { onLogout() } }

        Text("$BRAND v2.8 · dibuat dengan 🧡 di Indonesia", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(20.dp), )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileStat(big: String, small: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(big, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(small, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VDiv() { Box(Modifier.width(0.5.dp).height(30.dp).background(MaterialTheme.colorScheme.outline)) }

@Composable
private fun HDiv(startPad: Int = 16) { HorizontalDivider(Modifier.padding(start = startPad.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline) }

@Composable
private fun SubLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 32.dp, top = 18.dp, bottom = 7.dp))
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Column(content = content) }
}

@Composable
private fun InfoRow(label: String, value: String, last: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
    if (!last) HDiv()
}

@Composable
private fun TapRow(label: String, icon: ImageVector, destructive: Boolean = false, last: Boolean = false, onClick: () -> Unit) {
    val iconTint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val labelColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = labelColor, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
    if (!last) HDiv(52)
}

private fun sendTestNotif(ctx: android.content.Context) {
    val nb = androidx.core.app.NotificationCompat.Builder(ctx, App.CH_ALERT)
        .setContentTitle("Ayam Geprek Spesial 🌶️")
        .setContentText("Diskon 50% khusus hari ini — pesan sekarang di $BRAND!")
        .setSmallIcon(R.drawable.ic_stat_notif)
        .setAutoCancel(true)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
        .build()
    androidx.core.app.NotificationManagerCompat.from(ctx).notify(9999, nb)
}
