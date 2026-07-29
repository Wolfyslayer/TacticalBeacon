package com.tacticalbeacon

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.*
import com.tacticalbeacon.data.repository.SettingsRepository
import com.tacticalbeacon.location.LocationForegroundService
import com.tacticalbeacon.ui.compass.CompassScreen
import com.tacticalbeacon.ui.map.MapScreen
import com.tacticalbeacon.ui.pins.PinListScreen
import com.tacticalbeacon.ui.settings.SettingsScreen
import com.tacticalbeacon.ui.theme.TacticalBeaconTheme
import com.tacticalbeacon.ui.theme.TacticalColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TacticalBeaconTheme {
                val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                    initialValue = com.tacticalbeacon.data.repository.AppSettings()
                )

                // Keep screen awake if setting is enabled
                LaunchedEffect(settings.keepScreenAwake) {
                    if (settings.keepScreenAwake) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                PermissionGate {
                    AppNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Service continues running in background via foreground service
    }
}

// ─── Navigation Graph ─────────────────────────────────────────────────────────

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "map"
    ) {
        composable("map") {
            MapScreen(
                onNavigateToCompass = { navController.navigate("compass") },
                onNavigateToPinList = { navController.navigate("pins") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("compass") {
            CompassScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("pins") {
            PinListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCompass = {
                    navController.navigate("compass") {
                        popUpTo("map")
                    }
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// ─── Permission Gate ──────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionGate(content: @Composable () -> Unit) {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    when {
        locationPermissions.allPermissionsGranted -> content()
        locationPermissions.shouldShowRationale -> {
            PermissionRationaleScreen(
                onRequestPermissions = { locationPermissions.launchMultiplePermissionRequest() }
            )
        }
        else -> {
            LaunchedEffect(Unit) {
                locationPermissions.launchMultiplePermissionRequest()
            }
            PermissionWaitingScreen()
        }
    }
}

@Composable
fun PermissionRationaleScreen(onRequestPermissions: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalColors.MatteBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                null,
                tint = TacticalColors.OliveGreen,
                modifier = Modifier.size(72.dp)
            )
            Text(
                "LOCATION ACCESS REQUIRED",
                style = MaterialTheme.typography.titleLarge,
                color = TacticalColors.OliveGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            Surface(
                color = TacticalColors.CardSurface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionBullet(
                        icon = Icons.Default.GpsFixed,
                        text = "Precise GPS location for accurate navigation"
                    )
                    PermissionBullet(
                        icon = Icons.Default.Navigation,
                        text = "Continuous tracking to measure distance to pins"
                    )
                    PermissionBullet(
                        icon = Icons.Default.Notifications,
                        text = "Background location for proximity alerts"
                    )
                }
            }
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalColors.OliveGreen
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("GRANT PERMISSIONS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PermissionWaitingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalColors.MatteBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = TacticalColors.OliveGreen)
            Text(
                "AWAITING PERMISSIONS",
                style = MaterialTheme.typography.titleMedium,
                color = TacticalColors.OliveGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PermissionBullet(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = TacticalColors.OliveGreen, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TacticalColors.SecondaryText)
    }
}
