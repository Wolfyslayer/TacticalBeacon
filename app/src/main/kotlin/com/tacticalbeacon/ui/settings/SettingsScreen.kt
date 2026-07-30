package com.tacticalbeacon.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tacticalbeacon.data.repository.AppSettings
import com.tacticalbeacon.navigation.NavigationViewModel
import com.tacticalbeacon.ui.theme.TacticalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var localSettings by remember(settings) { mutableStateOf(settings) }
    var showClearBreadcrumbsConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TacticalColors.MatteBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalColors.OliveGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.updateSettings(localSettings)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TacticalColors.OliveGreen)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateSettings(localSettings)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = TacticalColors.OliveGreen)
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TacticalColors.DarkSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ─── Map & Display ─────────────────────────────────────────────
            SettingsSection(title = "MAP & DISPLAY") {
                SettingsToggle(
                    icon = Icons.Default.GridOn,
                    title = "Grid Overlay",
                    subtitle = "Show coordinate grid on map",
                    checked = localSettings.showGrid,
                    onCheckedChange = { localSettings = localSettings.copy(showGrid = it) }
                )
                SettingsDivider()
                SettingsToggle(
                    icon = Icons.Default.Timeline,
                    title = "Breadcrumb Trail",
                    subtitle = "Show path history on map",
                    checked = localSettings.showBreadcrumbs,
                    onCheckedChange = { localSettings = localSettings.copy(showBreadcrumbs = it) }
                )
                SettingsDivider()
                SettingsToggle(
                    icon = Icons.Default.Explore,
                    title = "Compass Overlay",
                    subtitle = "Show compass rose on map",
                    checked = localSettings.showCompassOverlay,
                    onCheckedChange = { localSettings = localSettings.copy(showCompassOverlay = it) }
                )
                SettingsDivider()
                SettingsToggle(
                    icon = Icons.Default.Circle,
                    title = "Range Rings",
                    subtitle = "Show distance rings around user",
                    checked = localSettings.showRangeRings,
                    onCheckedChange = { localSettings = localSettings.copy(showRangeRings = it) }
                )
                SettingsDivider()
                SettingsToggle(
                    icon = Icons.Default.Straighten,
                    title = "Measurement Overlay",
                    subtitle = "Show measurement tools on map",
                    checked = localSettings.showMeasurementOverlay,
                    onCheckedChange = { localSettings = localSettings.copy(showMeasurementOverlay = it) }
                )
                SettingsDivider()
                SettingsToggle(
                    icon = Icons.Default.DarkMode,
                    title = "Red Light Mode",
                    subtitle = "Preserve night vision with red tint",
                    checked = localSettings.redLightMode,
                    onCheckedChange = { localSettings = localSettings.copy(redLightMode = it) }
                )
            }

            // ─── Coordinates ────────────────────────────────────────────
            SettingsSection(title = "COORDINATES") {
                val coordOptions = listOf(
                    "DECIMAL_DEGREES" to "Decimal Degrees",
                    "MGRS" to "MGRS",
                    "UTM" to "UTM"
                )
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = TacticalColors.OliveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Coordinate Format",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TacticalColors.HighContrastWhite
                            )
                            Text(
                                coordOptions.find { it.first == localSettings.coordinateFormat }?.second
                                    ?: "Decimal Degrees",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalColors.SecondaryText
                            )
                        }
                    }
                    Slider(
                        value = coordOptions.indexOfFirst { it.first == localSettings.coordinateFormat }.toFloat(),
                        onValueChange = { v ->
                            val idx = v.toInt().coerceIn(0, coordOptions.size - 1)
                            localSettings = localSettings.copy(coordinateFormat = coordOptions[idx].first)
                        },
                        valueRange = 0f..(coordOptions.size - 1).toFloat(),
                        steps = coordOptions.size - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = TacticalColors.OliveGreen,
                            activeTrackColor = TacticalColors.OliveGreen,
                            inactiveTrackColor = TacticalColors.ElevatedSurface
                        )
                    )
                }
            }

            // ─── GPS ──────────────────────────────────────────────────────────
            SettingsSection(title = "GPS & BATTERY") {
                SettingsToggle(
                    icon = Icons.Default.BatteryAlert,
                    title = "Battery Saver Mode",
                    subtitle = "Reduced GPS accuracy, longer battery life",
                    checked = localSettings.batterySaverMode,
                    onCheckedChange = { localSettings = localSettings.copy(batterySaverMode = it) }
                )
                SettingsDivider()

                // GPS update rate
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            null,
                            tint = TacticalColors.OliveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "GPS Update Rate",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TacticalColors.HighContrastWhite
                            )
                            Text(
                                "${localSettings.gpsUpdateIntervalMs}ms (${1000L / localSettings.gpsUpdateIntervalMs} Hz)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalColors.SecondaryText
                            )
                        }
                    }
                    Slider(
                        value = when (localSettings.gpsUpdateIntervalMs) {
                            500L -> 0f
                            1000L -> 1f
                            2000L -> 2f
                            5000L -> 3f
                            else -> 1f
                        },
                        onValueChange = { v ->
                            localSettings = localSettings.copy(
                                gpsUpdateIntervalMs = when (v.toInt()) {
                                    0 -> 500L
                                    1 -> 1000L
                                    2 -> 2000L
                                    3 -> 5000L
                                    else -> 1000L
                                }
                            )
                        },
                        valueRange = 0f..3f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = TacticalColors.OliveGreen,
                            activeTrackColor = TacticalColors.OliveGreen,
                            inactiveTrackColor = TacticalColors.ElevatedSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("500ms", "1s", "2s", "5s").forEach { label ->
                            Text(label, style = MaterialTheme.typography.labelSmall, color = TacticalColors.DisabledText)
                        }
                    }
                }
            }

            // ─── Alerts ───────────────────────────────────────────────────────
            SettingsSection(title = "PROXIMITY ALERTS") {
                // Alert volume
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            null,
                            tint = TacticalColors.OliveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Alert Volume",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TacticalColors.HighContrastWhite
                            )
                            Text(
                                "${(localSettings.alertVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalColors.SecondaryText
                            )
                        }
                    }
                    Slider(
                        value = localSettings.alertVolume,
                        onValueChange = { localSettings = localSettings.copy(alertVolume = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = TacticalColors.OliveGreen,
                            activeTrackColor = TacticalColors.OliveGreen,
                            inactiveTrackColor = TacticalColors.ElevatedSurface
                        )
                    )
                }

                SettingsDivider()

                // Vibration strength
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Vibration,
                            null,
                            tint = TacticalColors.OliveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Vibration Strength",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TacticalColors.HighContrastWhite
                            )
                            Text(
                                "${localSettings.vibrationStrength}/5",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalColors.SecondaryText
                            )
                        }
                    }
                    Slider(
                        value = localSettings.vibrationStrength.toFloat(),
                        onValueChange = { localSettings = localSettings.copy(vibrationStrength = it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = TacticalColors.OliveGreen,
                            activeTrackColor = TacticalColors.OliveGreen,
                            inactiveTrackColor = TacticalColors.ElevatedSurface
                        )
                    )
                }

                SettingsDivider()

                // Proximity distances
                Text(
                    "PROXIMITY THRESHOLDS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TacticalColors.OliveGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ProximityThresholdRow(
                    label = "Threshold 1",
                    value = localSettings.customDist1,
                    interval = "10s alert",
                    onValueChange = { localSettings = localSettings.copy(customDist1 = it) }
                )
                ProximityThresholdRow(
                    label = "Threshold 2",
                    value = localSettings.customDist2,
                    interval = "5s alert",
                    onValueChange = { localSettings = localSettings.copy(customDist2 = it) }
                )
                ProximityThresholdRow(
                    label = "Threshold 3",
                    value = localSettings.customDist3,
                    interval = "2s alert",
                    onValueChange = { localSettings = localSettings.copy(customDist3 = it) }
                )
                ProximityThresholdRow(
                    label = "Threshold 4",
                    value = localSettings.customDist4,
                    interval = "1s alert",
                    onValueChange = { localSettings = localSettings.copy(customDist4 = it) }
                )
                ProximityThresholdRow(
                    label = "Threshold 5",
                    value = localSettings.customDist5,
                    interval = "0.5s alert",
                    onValueChange = { localSettings = localSettings.copy(customDist5 = it) }
                )
                ProximityThresholdRow(
                    label = "Threshold 6",
                    value = localSettings.customDist6,
                    interval = "Rapid",
                    onValueChange = { localSettings = localSettings.copy(customDist6 = it) }
                )
                ProximityThresholdRow(
                    label = "Threshold 7",
                    value = localSettings.customDist7,
                    interval = "Continuous",
                    onValueChange = { localSettings = localSettings.copy(customDist7 = it) }
                )
            }

            // ─── Data Management ──────────────────────────────────────────────
            SettingsSection(title = "DATA MANAGEMENT") {
                Surface(
                    onClick = { showClearBreadcrumbsConfirm = true },
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            null,
                            tint = TacticalColors.AlertRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Clear Breadcrumb Trail",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TacticalColors.AlertRed
                            )
                            Text(
                                "Remove all path history",
                                style = MaterialTheme.typography.bodySmall,
                                color = TacticalColors.SecondaryText
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = TacticalColors.DisabledText
                        )
                    }
                }
            }

            // ─── About ────────────────────────────────────────────────────────
            SettingsSection(title = "ABOUT") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version", color = TacticalColors.SecondaryText)
                    Text("1.0.0", color = TacticalColors.HighContrastWhite, fontWeight = FontWeight.SemiBold)
                }
                SettingsDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Map Engine", color = TacticalColors.SecondaryText)
                    Text("OSMDroid 6.1", color = TacticalColors.HighContrastWhite)
                }
                SettingsDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min Android", color = TacticalColors.SecondaryText)
                    Text("Android 12 (API 31)", color = TacticalColors.HighContrastWhite)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearBreadcrumbsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearBreadcrumbsConfirm = false },
            containerColor = TacticalColors.CardSurface,
            title = {
                Text("CLEAR TRAIL", color = TacticalColors.AlertRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete all breadcrumb trail data.",
                    color = TacticalColors.SecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearBreadcrumbs()
                        showClearBreadcrumbsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalColors.AlertRed)
                ) {
                    Text("CLEAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearBreadcrumbsConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TacticalColors.SecondaryText)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

// ─── Reusable Settings Components ────────────────────────────────────────────

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = TacticalColors.OliveGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            color = TacticalColors.CardSurface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TacticalColors.OliveGreen, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TacticalColors.HighContrastWhite)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TacticalColors.SecondaryText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TacticalColors.HighContrastWhite,
                checkedTrackColor = TacticalColors.OliveGreen,
                uncheckedThumbColor = TacticalColors.DisabledText,
                uncheckedTrackColor = TacticalColors.ElevatedSurface
            )
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        color = TacticalColors.ElevatedSurface,
        thickness = 0.5.dp
    )
}

@Composable
fun ProximityThresholdRow(
    label: String,
    value: Int,
    interval: String,
    onValueChange: (Int) -> Unit
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TacticalColors.HighContrastWhite)
            Text(interval, style = MaterialTheme.typography.labelSmall, color = TacticalColors.DisabledText)
        }
        OutlinedTextField(
            value = textValue,
            onValueChange = { v ->
                textValue = v
                v.toIntOrNull()?.let { onValueChange(it) }
            },
            suffix = { Text("m", color = TacticalColors.SecondaryText) },
            modifier = Modifier.width(90.dp),
            singleLine = true,
            colors = com.tacticalbeacon.ui.map.tacticalTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TacticalColors.HighContrastWhite,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        )
    }
}
