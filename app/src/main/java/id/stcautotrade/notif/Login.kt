package id.stcautotrade.notif

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// "Login" DISAMARKAN jadi landing aplikasi pesan-antar makanan "Cikiluks Chicken".
// Kotak cari = gerbang: teks yang diketik = password admin.
private val HERO = listOf(
    "Mau makan apa hari ini?", "Lagi pengen makan apa?",
    "Lapar? Cari yang enak yuk!", "Waktunya makan enak 😋",
)
private data class Cat(val e: String, val n: String)
private val CATS = listOf(
    Cat("🍚", "Nasi"), Cat("🍜", "Mie"), Cat("🍗", "Ayam"), Cat("🥤", "Minuman"),
    Cat("🍢", "Sate"), Cat("🥗", "Sehat"), Cat("🍰", "Manis"), Cat("🌶️", "Pedas"),
)
private data class Promo(val e: String, val n: String, val disc: String, val bg: Color)
private val PROMOS = listOf(
    Promo("🍔", "Burger Juara", "-40%", Color(0xFFFDE8D8)),
    Promo("🍕", "Pizza Nikmat", "-30%", Color(0xFFE7F0DC)),
    Promo("🍗", "Ayam Krispi", "-50%", Color(0xFFFCE1DC)),
)
private data class Dish(val e: String, val n: String, val d: String, val r: String, val price: String, val km: String)
private val DISHES = listOf(
    Dish("🍜", "Mie Kuah Pedas", "Hangat & gurih", "4.9", "Rp18.000", "1.2 km"),
    Dish("🍛", "Nasi Kari Ayam", "Rempah kaya", "4.8", "Rp25.000", "0.8 km"),
    Dish("🥗", "Salad Segar", "Sehat & ringan", "4.7", "Rp22.000", "2.1 km"),
    Dish("🍢", "Sate Ayam Madura", "Bumbu kacang legit", "4.9", "Rp20.000", "1.5 km"),
    Dish("🍚", "Nasi Goreng Spesial", "Klasik favorit", "4.8", "Rp15.000", "0.5 km"),
    Dish("🍗", "Ayam Geprek", "Pedas nampol", "4.9", "Rp16.000", "1.0 km"),
)

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    var answer by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var miss by remember { mutableStateOf(false) }
    val hero = remember { HERO.random() }
    val askNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun submit() {
        if (loading || answer.isBlank()) return
        loading = true; miss = false
        scope.launch {
            val token = Api.login(prefs.baseUrl, answer.trim())
            loading = false
            if (token != null) {
                prefs.token = token
                prefs.password = answer.trim()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                NotifierService.start(ctx)
                onLoggedIn()
            } else {
                miss = true; answer = ""
            }
        }
    }

    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {

        // ── Top bar: merek + lokasi + avatar ────────────────────────────────
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) { Text("🍽️", fontSize = 19.sp) }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Cikiluks Chicken", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Jakarta Selatan", style = MaterialTheme.typography.bodySmall, color = muted)
                }
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { Text("😋", fontSize = 18.sp) }
            }
        }

        // ── Salam + kotak cari (GERBANG) ────────────────────────────────────
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(hero, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(3.dp))
            Text("Ketik selera atau nama masakan, kami carikan yang pas.", style = MaterialTheme.typography.bodyMedium, color = muted)
            Spacer(Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp)),
            ) {
                Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔎", fontSize = 17.sp, modifier = Modifier.padding(start = 8.dp, end = 4.dp))
                    BasicTextField(
                        value = answer,
                        onValueChange = { answer = it; miss = false },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { submit() }),
                        modifier = Modifier.weight(1f).padding(vertical = 10.dp, horizontal = 6.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (answer.isEmpty()) Text("Ketik selera kamu…", color = muted, fontSize = 16.sp)
                                inner()
                            }
                        },
                    )
                    Button(onClick = { submit() }, enabled = !loading && answer.isNotBlank(), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
                        if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Cari", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (miss) {
                Spacer(Modifier.height(9.dp))
                Text("Belum nemu yang pas — coba kata lain 🍽️", style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }

        // ── Banner promo ────────────────────────────────────────────────────
        Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFBD5B3A), Color(0xFFE58A6D))))) {
                    Row(Modifier.padding(18.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.22f)) {
                                Text("PROMO SPESIAL", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Diskon 50%", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            Text("Untuk order pertamamu hari ini!", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.92f))
                        }
                        Text("🍔", fontSize = 52.sp)
                    }
                }
            }
        }

        // ── Kategori ────────────────────────────────────────────────────────
        SectionHeader("Kategori")
        Column(Modifier.padding(horizontal = 12.dp)) {
            CATS.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { c -> CategoryChip(c) { answer = c.n; miss = false } }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        // ── Lagi diskon (horizontal) ────────────────────────────────────────
        SectionHeader("Lagi diskon 🔥", "Lihat semua")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PROMOS.forEach { PromoCard(it) }
        }

        // ── Rekomendasi (grid 2 kolom) ──────────────────────────────────────
        SectionHeader("Rekomendasi buat kamu")
        Column(Modifier.padding(horizontal = 16.dp)) {
            DISHES.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { d -> Box(Modifier.weight(1f)) { DishCard(d) } }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        Text(
            "© Cikiluks Chicken · ayam kriuk juara, pedasnya nagih 🍗",
            style = MaterialTheme.typography.labelSmall, color = muted,
            modifier = Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CategoryChip(c: Cat, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { Text(c.e, fontSize = 25.sp) }
        }
        Spacer(Modifier.height(6.dp))
        Text(c.n, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PromoCard(p: Promo) {
    Surface(shape = MaterialTheme.shapes.large, color = p.bg, modifier = Modifier.width(180.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(p.e, fontSize = 34.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(p.n, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1C1C1E))
                Spacer(Modifier.height(3.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                    Text(p.disc, style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun DishCard(d: Dish) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(Modifier.fillMaxWidth().height(88.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Text(d.e, fontSize = 44.sp)
            }
            Column(Modifier.padding(12.dp)) {
                Text(d.n, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(d.d, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = AppColors.amber, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(d.r, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("  ·  ${d.km}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Text(d.price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
