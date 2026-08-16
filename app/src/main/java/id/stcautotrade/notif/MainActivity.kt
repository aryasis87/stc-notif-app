package id.stcautotrade.notif

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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

// ── Login ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
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

    Box(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 440.dp)) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primary, shadowElevation = 10.dp) {
                Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(42.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("STC Notif", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "Masuk untuk memantau STC & KOALA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(30.dp))
            TextField(
                value = pass,
                onValueChange = { pass = it; error = null },
                placeholder = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { submit() }),
                isError = error != null,
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            )
            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { submit() },
                enabled = !loading && pass.isNotEmpty(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("Masuk", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Cukup password — tanpa isi URL atau token.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Scaffold + tab bar iOS ───────────────────────────────────────────────────
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
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    Tab.entries.forEach { tt ->
                        NavigationBarItem(
                            selected = tab == tt,
                            onClick = { tab = tt },
                            icon = { Icon(tt.icon, tt.label, modifier = Modifier.size(24.dp)) },
                            label = { Text(tt.label, style = MaterialTheme.typography.labelSmall) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(bottom = inner.calculateBottomPadding())) {
            when (tab) {
                Tab.HOME -> HomeScreen()
                Tab.NOTIF -> NotificationsScreen()
                Tab.DASH -> DashboardScreen()
                Tab.SETTINGS -> SettingsScreen(onLogout = onLogout)
            }
        }
    }
}
