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


// ── Scaffold + tab bar iOS ───────────────────────────────────────────────────
private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Beranda", Icons.Filled.Restaurant),
    NOTIF("Promo", Icons.Filled.LocalOffer),
    DASH("Dapur", Icons.Filled.Storefront),
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
