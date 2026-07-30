package com.tacticalbeacon.ui.pins

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.PinStatus
import com.tacticalbeacon.navigation.NavigationViewModel
import com.tacticalbeacon.ui.map.PinDetailSheet
import com.tacticalbeacon.ui.map.pinColorValue
import com.tacticalbeacon.ui.map.pinIconVector
import com.tacticalbeacon.ui.theme.TacticalColors
import com.tacticalbeacon.utils.FileUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinListScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCompass: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pins by viewModel.pins.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPin by remember { mutableStateOf<Pin?>(null) }
    var showPinDetail by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                if (content != null) {
                    val fileName = FileUtils.getFileName(context, it) ?: ""
                    val result = when {
                        fileName.endsWith(".gpx", ignoreCase = true) ->
                            viewModel.pinRepository.importFromGpx(content)
                        else ->
                            viewModel.pinRepository.importFromJson(content)
                    }
                    result.fold(
                        onSuccess = { count ->
                            snackbarHostState.showSnackbar("Imported $count pins successfully")
                        },
                        onFailure = { e ->
                            snackbarHostState.showSnackbar("Import failed: ${e.message}")
                        }
                    )
                }
            }
        }
    }

    // Export launcher
    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val json = viewModel.pinRepository.exportToJson(pins)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(json.toByteArray())
                }
                snackbarHostState.showSnackbar("Exported ${pins.size} pins as JSON")
            }
        }
    }

    val exportGpxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/gpx+xml")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val gpx = viewModel.pinRepository.exportToGpx(pins)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(gpx.toByteArray())
                }
                snackbarHostState.showSnackbar("Exported ${pins.size} pins as GPX")
            }
        }
    }

    // Filtered pins
    val filteredPins = remember(pins, searchQuery) {
        if (searchQuery.isBlank()) pins
        else pins.filter { pin ->
            pin.name.contains(searchQuery, ignoreCase = true) ||
            pin.notes.contains(searchQuery, ignoreCase = true) ||
            pin.icon.label.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = TacticalColors.MatteBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PINS (${pins.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalColors.OliveGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TacticalColors.OliveGreen)
                    }
                },
                actions = {
                    // Import
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Default.FileDownload, "Import", tint = TacticalColors.SecondaryText)
                    }
                    // Export menu
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.FileUpload, "Export", tint = TacticalColors.SecondaryText)
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false },
                            containerColor = TacticalColors.CardSurface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as JSON", color = TacticalColors.HighContrastWhite) },
                                leadingIcon = {
                                    Icon(Icons.Default.Code, null, tint = TacticalColors.OliveGreen)
                                },
                                onClick = {
                                    showExportMenu = false
                                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                    exportJsonLauncher.launch("tactical_beacon_$timestamp.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as GPX", color = TacticalColors.HighContrastWhite) },
                                leadingIcon = {
                                    Icon(Icons.Default.Map, null, tint = TacticalColors.OliveGreen)
                                },
                                onClick = {
                                    showExportMenu = false
                                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                    exportGpxLauncher.launch("tactical_beacon_$timestamp.gpx")
                                }
                            )
                        }
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
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search pins...", color = TacticalColors.DisabledText)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = TacticalColors.OliveGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = TacticalColors.DisabledText)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = com.tacticalbeacon.ui.map.tacticalTextFieldColors(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            if (filteredPins.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            null,
                            tint = TacticalColors.DisabledText,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            if (searchQuery.isBlank()) "NO PINS SAVED" else "NO PINS MATCH",
                            style = MaterialTheme.typography.titleMedium,
                            color = TacticalColors.DisabledText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (searchQuery.isBlank()) "Tap the map to drop a pin"
                            else "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TacticalColors.DisabledText.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPins, key = { it.id }) { pin ->
                        PinListItem(
                            pin = pin,
                            isNavigating = navigationState.targetPin?.id == pin.id,
                            useMetric = settings.useMetric,
                            currentLocation = locationState,
                            onClick = {
                                selectedPin = pin
                                showPinDetail = true
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showPinDetail && selectedPin != null) {
        PinDetailSheet(
            pin = selectedPin!!,
            isNavigating = navigationState.targetPin?.id == selectedPin!!.id,
            currentLocation = locationState,
            useMetric = settings.useMetric,
            onNavigate = { pin ->
                viewModel.startNavigation(pin)
                showPinDetail = false
                onNavigateToCompass()
            },
            onEdit = { pin -> selectedPin = pin },
            onSave = { pin ->
                viewModel.updatePin(pin)
                selectedPin = pin
            },
            onDelete = { pin ->
                viewModel.deletePin(pin)
                showPinDetail = false
                selectedPin = null
            },
            onDismiss = {
                showPinDetail = false
                selectedPin = null
            }
        )
    }
}

@Composable
fun PinListItem(
    pin: Pin,
    isNavigating: Boolean,
    useMetric: Boolean,
    currentLocation: com.tacticalbeacon.data.model.LocationState,
    onClick: () -> Unit
) {
    val distance = if (currentLocation.isValid) {
        com.tacticalbeacon.location.LocationManager.distanceBetween(
            currentLocation.latitude, currentLocation.longitude,
            pin.latitude, pin.longitude
        )
    } else null

    Surface(
        onClick = onClick,
        color = if (isNavigating) TacticalColors.OliveGreenContainer else TacticalColors.CardSurface,
        shape = RoundedCornerShape(10.dp),
        border = if (isNavigating) BorderStroke(1.dp, TacticalColors.OliveGreen) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(pinColorValue(pin.color).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    pinIconVector(pin.icon),
                    null,
                    tint = pinColorValue(pin.color),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        pin.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TacticalColors.HighContrastWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isNavigating) {
                        Surface(
                            color = TacticalColors.OliveGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalColors.OliveGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    pin.icon.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TacticalColors.DisabledText
                )
                if (pin.notes.isNotBlank()) {
                    Text(
                        pin.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TacticalColors.SecondaryText,
                        maxLines = 1
                    )
                }
            }

            // Category and status badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = TacticalColors.ElevatedSurface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        pin.category.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalColors.OliveGreen
                    )
                }
                Surface(
                    color = TacticalColors.ElevatedSurface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        pin.status.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (pin.status) {
                            PinStatus.ACTIVE -> TacticalColors.AlertGreen
                            PinStatus.INACTIVE -> TacticalColors.SecondaryText
                            PinStatus.ARCHIVED -> TacticalColors.DisabledText
                            PinStatus.COMPLETED -> TacticalColors.OliveGreen
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (distance != null) {
                    Text(
                        com.tacticalbeacon.location.LocationManager.formatDistance(distance, useMetric),
                        style = MaterialTheme.typography.labelMedium,
                        color = TacticalColors.OliveGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${String.format("%.4f", pin.latitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalColors.DisabledText,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "${String.format("%.4f", pin.longitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalColors.DisabledText,
                    fontFamily = FontFamily.Monospace
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TacticalColors.DisabledText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
