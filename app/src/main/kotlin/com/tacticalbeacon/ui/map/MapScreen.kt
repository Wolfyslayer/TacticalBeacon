package com.tacticalbeacon.ui.map

import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.tacticalbeacon.R
import androidx.core.content.ContextCompat
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tacticalbeacon.data.model.*
import com.tacticalbeacon.location.LocationManager
import com.tacticalbeacon.navigation.NavigationViewModel
import com.tacticalbeacon.ui.theme.TacticalColors
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.roundToInt

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    drawable.setBounds(
        0,
        0,
        canvas.width,
        canvas.height
    )

    drawable.draw(canvas)

    return bitmap
}
private val SATELLITE_SOURCE = object : OnlineTileSourceBase(
    "Esri World Imagery",
    0,
    19,
    256,
    ".jpg",
    arrayOf(
        "https://server.arcgisonline.com/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/" +
                org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex) + "/" +
                org.osmdroid.util.MapTileIndex.getY(pMapTileIndex) + "/" +
                org.osmdroid.util.MapTileIndex.getX(pMapTileIndex) +
                ".jpg"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    onNavigateToCompass: () -> Unit,
    onNavigateToPinList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
     val context = LocalContext.current
    val pins by viewModel.pins.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isGpsAccuracyPoor by viewModel.isGpsAccuracyPoor.collectAsStateWithLifecycle()
    val breadcrumbs by viewModel.breadcrumbs.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pendingPinLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedPin by remember { mutableStateOf<Pin?>(null) }
    var showPinDetailSheet by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isCenteredOnUser by remember { mutableStateOf(true) }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
        Configuration.getInstance().tileDownloadThreads = 8
Configuration.getInstance().tileFileSystemThreads = 8
Configuration.getInstance().animationSpeedDefault = 800
    userAgentValue = "TacticalBeacon/1.0"

    osmdroidBasePath = context.getExternalFilesDir(null)
        ?: context.filesDir

    osmdroidTileCache = context.getExternalFilesDir("osmdroid")
        ?: context.filesDir
        }
    }

    // Center map on user location when it first becomes available
    LaunchedEffect(locationState.isValid, isCenteredOnUser) {
        if (locationState.isValid && isCenteredOnUser) {
            mapView?.controller?.animateTo(
                GeoPoint(locationState.latitude, locationState.longitude)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(TacticalColors.MatteBlack)) {

        // ─── OSMDroid Map View ────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(
    if (settings.defaultMapType == MapType.SATELLITE.name)
        SATELLITE_SOURCE
    else
        TileSourceFactory.MAPNIK
)
                    setMultiTouchControls(true)
                    controller.setZoom(
    if (settings.lastZoom >= 8.0)
        settings.lastZoom
    else
        16.0
)
                    setUseDataConnection(true)

isTilesScaledToDpi = true

setFlingEnabled(true)

isHorizontalMapRepetitionEnabled = false
isVerticalMapRepetitionEnabled = false

                    if (settings.lastLatitude != 0.0) {
                        controller.setCenter(GeoPoint(settings.lastLatitude, settings.lastLongitude))
                    }

                    // Map tap listener for dropping pins
                    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let {
                                pendingPinLocation = it
                                showPinDialog = true
                            }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })
                    overlays.add(mapEventsOverlay)

                    // My location overlay
val myLocationOverlay = MyLocationNewOverlay(
    GpsMyLocationProvider(ctx),
    this
)

myLocationOverlay.enableMyLocation()
myLocationOverlay.enableFollowLocation()

myLocationOverlay.setPersonIcon(
    drawableToBitmap(
        ContextCompat.getDrawable(
            ctx,
            R.drawable.ic_user_location
        )!!
    )
)

overlays.add(myLocationOverlay)

mapView = this
                }
            },
            update = { mv ->
                // Update pin markers
                mv.overlays.removeAll { it is Marker }

                // Draw breadcrumbs as polyline
                if (settings.showBreadcrumbs && breadcrumbs.size > 1) {
                    val existingPolyline = mv.overlays.filterIsInstance<Polyline>().firstOrNull()
                    existingPolyline?.let { mv.overlays.remove(it) }

                    val polyline = Polyline().apply {
                        setPoints(breadcrumbs.map { GeoPoint(it.latitude, it.longitude) })
                        outlinePaint.color = TacticalColors.OliveGreen.copy(alpha = 0.6f).toArgb()
                        outlinePaint.strokeWidth = 4f
                        outlinePaint.style = Paint.Style.STROKE
                    }
                    mv.overlays.add(0, polyline)
                }

                // Draw navigation line to target
                navigationState.targetPin?.let { target ->
                    if (locationState.isValid) {
                        val existingNavLine = mv.overlays.filterIsInstance<Polyline>()
                            .firstOrNull { it.id == "nav_line" }
                        existingNavLine?.let { mv.overlays.remove(it) }

                        val navLine = Polyline().apply {
                            id = "nav_line"
                            setPoints(listOf(
                                GeoPoint(locationState.latitude, locationState.longitude),
                                GeoPoint(target.latitude, target.longitude)
                            ))
                            outlinePaint.color = TacticalColors.AlertAmber.toArgb()
                            outlinePaint.strokeWidth = 3f
                            outlinePaint.pathEffect = android.graphics.DashPathEffect(
                                floatArrayOf(20f, 10f), 0f
                            )
                        }
                        mv.overlays.add(navLine)
                    }
                }

                // Draw pin markers
                for (pin in pins) {
                    val marker = Marker(mv).apply {

    position = GeoPoint(
        pin.latitude,
        pin.longitude
    )

    title = pin.name
    snippet = pin.notes.ifBlank { null }

    icon = ContextCompat.getDrawable(
    context,
    R.drawable.ic_tactical_pin
)

    setAnchor(
    Marker.ANCHOR_CENTER,
    Marker.ANCHOR_BOTTOM
)

    if (navigationState.targetPin?.id == pin.id) {
        alpha = 1.0f
    }

    setOnMarkerClickListener { _, _ ->
        selectedPin = pin
        showPinDetailSheet = true
        true
    }
                    
                    }
                    mv.overlays.add(marker)
                }

                // Grid overlay
                if (settings.showGrid) {
                    val existingGrid = mv.overlays.filterIsInstance<GridOverlay>().firstOrNull()
                    if (existingGrid == null) {
                        mv.overlays.add(GridOverlay())
                    }
                } else {
                    mv.overlays.removeAll { it is GridOverlay }
                }

                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // ─── Top Bar ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            // GPS accuracy warning
            AnimatedVisibility(
                visible = isGpsAccuracyPoor,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TacticalColors.AlertAmber.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = TacticalColors.MatteBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "POOR GPS ACCURACY (±${locationState.accuracy.roundToInt()}m)",
                            color = TacticalColors.MatteBlack,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // App bar
            Surface(
                color = TacticalColors.DarkSurface.copy(alpha = 0.92f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App title
                    Text(
                        "TACTICAL BEACON",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalColors.OliveGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )

                    // Coordinates display
                    if (locationState.isValid) {
                        Text(
                            "${String.format("%.5f", locationState.latitude)}\n${String.format("%.5f", locationState.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TacticalColors.SecondaryText,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = TacticalColors.SecondaryText)
                    }
                }
            }
        }

        // ─── Navigation Status Bar ────────────────────────────────────────────
        AnimatedVisibility(
            visible = navigationState.isNavigating,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            navigationState.targetPin?.let { pin ->
                NavigationStatusBar(
                    pin = pin,
                    distance = navigationState.distanceMeters,
                    bearing = navigationState.bearingDegrees,
                    useMetric = settings.useMetric,
                    onCompassClick = onNavigateToCompass,
                    onStopNavigation = { viewModel.stopNavigation() }
                )
            }
        }

        // ─── Right-side FABs ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Center on user
            FloatingActionButton(
                onClick = {
                    isCenteredOnUser = true
                    if (locationState.isValid) {
                        mapView?.controller?.animateTo(
                            GeoPoint(locationState.latitude, locationState.longitude)
                        )
                    }
                },
                containerColor = TacticalColors.CardSurface,
                contentColor = TacticalColors.OliveGreen,
                modifier = Modifier.size(48.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, "Center on me", modifier = Modifier.size(22.dp))
            }

            // Drop pin at current location
            FloatingActionButton(
                onClick = {
                    if (locationState.isValid) {
                        pendingPinLocation = GeoPoint(locationState.latitude, locationState.longitude)
                        showPinDialog = true
                    }
                },
                containerColor = TacticalColors.OliveGreen,
                contentColor = TacticalColors.HighContrastWhite,
                modifier = Modifier.size(56.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.AddLocation, "Drop pin here", modifier = Modifier.size(26.dp))
            }

            // Pin list
            FloatingActionButton(
                onClick = onNavigateToPinList,
                containerColor = TacticalColors.CardSurface,
                contentColor = TacticalColors.OliveGreen,
                modifier = Modifier.size(48.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.List, "Pin list", modifier = Modifier.size(22.dp))
            }
        }

        // ─── Bottom-left: Map type selector ───────────────────────────────────
        var showMapTypeMenu by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = if (navigationState.isNavigating) 120.dp else 24.dp)
        ) {
            Surface(
                onClick = { showMapTypeMenu = true },
                shape = RoundedCornerShape(8.dp),
                color = TacticalColors.CardSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        tint = TacticalColors.OliveGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        settings.defaultMapType,
                        style = MaterialTheme.typography.labelMedium,
                        color = TacticalColors.SecondaryText
                    )
                }
            }

            DropdownMenu(
                expanded = showMapTypeMenu,
                onDismissRequest = { showMapTypeMenu = false },
                containerColor = TacticalColors.CardSurface
            ) {
                MapType.entries.forEach { mapType ->
                    DropdownMenuItem(
                        text = {
                            Text(mapType.label, color = TacticalColors.HighContrastWhite)
                        },
                        onClick = {
                            viewModel.updateSettings(settings.copy(defaultMapType = mapType.name))
                            mapView?.setTileSource(
                                when (mapType) {
                                    MapType.SATELLITE -> SATELLITE_SOURCE
                                    MapType.TOPO -> TileSourceFactory.USGS_TOPO
                                    else -> TileSourceFactory.MAPNIK
                                }
                            )
                            showMapTypeMenu = false
                        }
                    )
                }
            }
        }
    }

    // ─── Pin Creation Dialog ──────────────────────────────────────────────────
    if (showPinDialog && pendingPinLocation != null) {
        PinEditDialog(
            initialPin = Pin(
                name = "Pin ${pins.size + 1}",
                latitude = pendingPinLocation!!.latitude,
                longitude = pendingPinLocation!!.longitude
            ),
            onSave = { pin ->
                viewModel.savePin(pin)
                showPinDialog = false
                pendingPinLocation = null
            },
            onDismiss = {
                showPinDialog = false
                pendingPinLocation = null
            }
        )
    }

    // ─── Pin Detail Bottom Sheet ──────────────────────────────────────────────
    if (showPinDetailSheet && selectedPin != null) {
        PinDetailSheet(
            pin = selectedPin!!,
            isNavigating = navigationState.targetPin?.id == selectedPin!!.id,
            currentLocation = locationState,
            useMetric = settings.useMetric,
            onNavigate = { pin ->
                viewModel.startNavigation(pin)
                showPinDetailSheet = false
                onNavigateToCompass()
            },
            onEdit = { pin ->
                selectedPin = pin
            },
            onSave = { pin ->
                viewModel.updatePin(pin)
                selectedPin = pin
            },
            onDelete = { pin ->
                viewModel.deletePin(pin)
                showPinDetailSheet = false
                selectedPin = null
            },
            onDismiss = {
                showPinDetailSheet = false
                selectedPin = null
            }
        )
    }
}

// ─── Navigation Status Bar ────────────────────────────────────────────────────

@Composable
fun NavigationStatusBar(
    pin: Pin,
    distance: Double,
    bearing: Float,
    useMetric: Boolean,
    onCompassClick: () -> Unit,
    onStopNavigation: () -> Unit
) {
    val proximityLevel = getProximityLevel(distance)
    val proximityColor = when (proximityLevel) {
        ProximityLevel.FAR -> TacticalColors.ProxFar
        ProximityLevel.NEAR -> TacticalColors.ProxNear
        ProximityLevel.CLOSE -> TacticalColors.ProxClose
        ProximityLevel.VERY_CLOSE -> TacticalColors.ProxVeryClose
        ProximityLevel.IMMEDIATE -> TacticalColors.ProxImmediate
        ProximityLevel.CRITICAL -> TacticalColors.ProxCritical
        ProximityLevel.ARRIVED -> TacticalColors.ProxArrived
        else -> TacticalColors.ProxFar
    }

    Surface(
        color = TacticalColors.DarkSurface.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Proximity indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(proximityColor, CircleShape)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pin.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = TacticalColors.OliveGreen,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        LocationManager.formatDistance(distance, useMetric),
                        style = MaterialTheme.typography.titleMedium,
                        color = proximityColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "${bearing.roundToInt()}°",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalColors.SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Compass button
            IconButton(
                onClick = onCompassClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(TacticalColors.OliveGreenContainer, CircleShape)
            ) {
                Icon(
                    Icons.Default.Explore,
                    "Open compass",
                    tint = TacticalColors.OliveGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(8.dp))

            // Stop navigation
            IconButton(
                onClick = onStopNavigation,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4A0000), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    "Stop navigation",
                    tint = TacticalColors.AlertRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
