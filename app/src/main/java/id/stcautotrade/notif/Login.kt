package id.stcautotrade.notif

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// Halaman "login" DISAMARKAN sebagai landing rekomendasi makanan — meniru
// admin.stcautotrade.id ("Dapur Hari Ini"). Kotak "cari selera" = gerbang:
// yang diketik = password admin. Hanya yang tahu password bisa masuk.
private val HERO = listOf(
    "Mau makan apa hari ini?",
    "Lagi pengen makan apa?",
    "Bingung mau masak apa?",
    "Selera kamu apa hari ini?",
    "Cari inspirasi menu?",
)
private val CATS = listOf("Sarapan", "Makan Siang", "Cemilan", "Sehat", "Pedas", "Manis", "Hangat")
private data class Dish(val e: String, val n: String, val d: String, val r: String)
private val DISHES = listOf(
    Dish("🍜", "Mie Kuah Pedas", "Hangat, gurih, pas buat hujan.", "4.9"),
    Dish("🍛", "Nasi Kari Ayam", "Rempah kaya, bikin nagih.", "4.8"),
    Dish("🥗", "Salad Segar", "Ringan dan menyehatkan.", "4.7"),
    Dish("🍢", "Sate Manis", "Bumbu kacang legit.", "4.9"),
)

@OptIn(ExperimentalLayoutApi::class)
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

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        // Merek
        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { Text("🍳", fontSize = 20.sp) }
            }
            Spacer(Modifier.width(11.dp))
            Text("Dapur Hari Ini", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(30.dp))

        // Hero (tengah)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🌿  INSPIRASI MENU HARIAN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(14.dp))
            Text(hero, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Ketik apa yang kamu inginkan — pedas, manis, hangat, atau nama masakan — dan biar kami carikan yang pas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            // Kotak cari (gerbang password)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
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
                                if (answer.isEmpty()) Text("Ketik selera kamu…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                                inner()
                            }
                        },
                    )
                    Button(
                        onClick = { submit() },
                        enabled = !loading && answer.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else Text("Temukan", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (miss) {
                Spacer(Modifier.height(10.dp))
                Text("Belum nemu yang pas — coba kata lain 🍽️", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(22.dp))
            // Kategori
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                CATS.forEach { c -> Chip(c) { answer = c; miss = false } }
            }
        }

        Spacer(Modifier.height(34.dp))

        // Populer
        Text("Lagi populer minggu ini", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text("Dipilih dari selera banyak orang", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        DISHES.forEach { d ->
            DishCard(d)
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Chip(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(999.dp)).clickable { onClick() },
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun DishCard(d: Dish) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(d.e, fontSize = 38.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(d.n, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(d.d, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("★ ${d.r}  ·  populer", style = MaterialTheme.typography.labelSmall, color = AppColors.amber, fontWeight = FontWeight.Bold)
            }
        }
    }
}
