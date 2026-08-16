package id.stcautotrade.notif

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { STCNotifTheme { Surface(color = MaterialTheme.colorScheme.background) { AppRoot() } } }
    }
}

@Composable
fun AppRoot() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var loggedIn by remember { mutableStateOf(prefs.loggedIn) }

    if (!loggedIn) {
        LoginScreen(onLoggedIn = { loggedIn = true })
    } else {
        MainScaffold(onLogout = {
            NotifierService.stop(ctx)
            prefs.logout()
            loggedIn = false
        })
    }
}

// ── Login (Apple-like) ───────────────────────────────────────────────────────
@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val scope = rememberCoroutineScope()
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val askNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun submit() {
        if (loading || pass.isEmpty()) return
        loading = true; error = null
        scope.launch {
            val token = Api.login(prefs.baseUrl, pass)
            loading = false
            if (token != null) {
                prefs.token = token
                prefs.password = pass
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                NotifierService.start(ctx)
                onLoggedIn()
            } else {
                error = "Password salah atau server tak terjangkau."
                pass = ""
            }
        }
    }

    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 420.dp)) {
            Box(
                Modifier.size(76.dp).padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primary) {
                    Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("STC Notif", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "Masuk untuk memantau STC & KOALA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it; error = null },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                isError = error != null,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { submit() },
                enabled = !loading && pass.isNotEmpty(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Masuk", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Tanpa isi URL/token — cukup password.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Scaffold + bottom nav ────────────────────────────────────────────────────
private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Beranda", Icons.Filled.Home),
    NOTIF("Notifikasi", Icons.Filled.Notifications),
    DASH("Dashboard", Icons.Filled.GridView),
    SETTINGS("Setelan", Icons.Filled.Settings),
}

@Composable
fun MainScaffold(onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.HOME) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, t.label) },
                        label = { Text(t.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                }
            }
        },
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {
            when (tab) {
                Tab.HOME -> HomeScreen()
                Tab.NOTIF -> NotificationsScreen()
                Tab.DASH -> DashboardScreen()
                Tab.SETTINGS -> SettingsScreen(onLogout = onLogout)
            }
        }
    }
}
