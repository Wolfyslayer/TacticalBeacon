package com.tacticalbeacon.ui.compass

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tacticalbeacon.data.model.*
import com.tacticalbeacon.location.LocationManager
import com.tacticalbeacon.navigation.NavigationViewModel
import com.tacticalbeacon.ui.theme.TacticalColors
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val azimuth by viewModel.azimuth.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isGpsAccuracyPoor by viewModel.isGpsAccuracyPoor.collectAsStateWithLifecycle()

    // Start compass sensor
    LaunchedEffect(Unit) {
        viewModel.compassManager.start()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.compassManager.stop() }
    }

    val targetPin = navigationState.targetPin
    val distance = navigationState.distanceMeters
    val targetBearing = navigationState.bearingDegrees

    // Animated compass rotation
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "compass_azimuth"
    )

    // Bearing arrow rotation relative to compass
    val relativeBearing = if (targetPin != null) {
        ((targetBearing - animatedAzimuth + 360f) % 360f)
    } else 0f

    val proximityLevel = if (targetPin != null) getProximityLevel(distance) else ProximityLevel.FAR
    val proximityColor = proximityLevelColor(proximityLevel)

    Scaffold(
        containerColor = TacticalColors.MatteBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "COMPASS",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalColors.OliveGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = TacticalColors.OliveGreen
                        )
                    }
                },
                actions = {
                    if (targetPin != null) {
                        TextButton(
                            onClick = { viewModel.stopNavigation() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = TacticalColors.AlertRed
                            )
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("STOP", fontWeight = FontWeight.Bold)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // GPS accuracy warning
            if (isGpsAccuracyPoor) {
                Surface(
                    color = TacticalColors.AlertAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, TacticalColors.AlertAmber.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = TacticalColors.AlertAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "GPS ACCURACY POOR — ±${locationState.accuracy.toInt()}m",
                            style = MaterialTheme.typography.labelMedium,
                            color = TacticalColors.AlertAmber
                        )
                    }
                }
            }

            // Target info
            if (targetPin != null) {
                Surface(
                    color = TacticalColors.CardSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    com.tacticalbeacon.ui.map.pinColorValue(targetPin.color).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                com.tacticalbeacon.ui.map.pinIconVector(targetPin.icon),
                                null,
                                tint = com.tacticalbeacon.ui.map.pinColorValue(targetPin.color),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "NAVIGATING TO",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalColors.OliveGreen
                            )
                            Text(
                                targetPin.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = TacticalColors.HighContrastWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Proximity level badge
                        Surface(
                            color = proximityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                proximityLevel.label.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = proximityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ─── Main Compass ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                CompassRose(
                    azimuth = animatedAzimuth,
                    targetBearing = if (targetPin != null) relativeBearing else null,
                    proximityColor = if (targetPin != null) proximityColor else TacticalColors.OliveGreen,
                    modifier = Modifier.fillMaxSize(0.95f)
                )
            }

            // ─── Stats Row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Heading
                StatCard(
                    label = "HEADING",
                    value = "${animatedAzimuth.toInt()}°",
                    subvalue = headingLabel(animatedAzimuth),
                    modifier = Modifier.weight(1f)
                )

                if (targetPin != null) {
                    // Distance
                    StatCard(
                        label = "DISTANCE",
                        value = LocationManager.formatDistance(distance, settings.useMetric),
                        subvalue = null,
                        valueColor = proximityColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Bearing to target
                    StatCard(
                        label = "BEARING",
                        value = "${targetBearing.toInt()}°",
                        subvalue = headingLabel(targetBearing),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // GPS accuracy
                    StatCard(
                        label = "GPS ACCURACY",
                        value = if (locationState.isValid) "±${locationState.accuracy.toInt()}m" else "NO FIX",
                        subvalue = null,
                        valueColor = if (isGpsAccuracyPoor) TacticalColors.AlertAmber else TacticalColors.AlertGreen,
                        modifier = Modifier.weight(1f)
                    )

                    // Coordinates
                    StatCard(
                        label = "POSITION",
                        value = if (locationState.isValid)
                            "${String.format("%.4f", locationState.latitude)}"
                        else "---",
                        subvalue = if (locationState.isValid)
                            "${String.format("%.4f", locationState.longitude)}"
                        else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // No navigation prompt
            if (targetPin == null) {
                Surface(
                    color = TacticalColors.CardSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = TacticalColors.DisabledText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Select a pin on the map to begin navigation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TacticalColors.DisabledText
                        )
                    }
                }
            }
        }
    }
}

// ─── Compass Rose Canvas ──────────────────────────────────────────────────────

@Composable
fun CompassRose(
    azimuth: Float,
    targetBearing: Float?,
    proximityColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val oliveGreen = TacticalColors.OliveGreen
    val oliveGreenDark = TacticalColors.OliveGreenDark
    val highContrastWhite = TacticalColors.HighContrastWhite
    val secondaryText = TacticalColors.SecondaryText
    val alertRed = TacticalColors.AlertRed
    val matteBlack = TacticalColors.MatteBlack

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.9f

        // ── Outer ring ──
        drawCircle(
            color = TacticalColors.ElevatedSurface,
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // ── Tick marks ──
        for (i in 0 until 360 step 5) {
            val angle = Math.toRadians((i - azimuth).toDouble())
            val isMajor = i % 45 == 0
            val isMedium = i % 15 == 0
            val tickLen = when {
                isMajor -> radius * 0.12f
                isMedium -> radius * 0.07f
                else -> radius * 0.04f
            }
            val startR = radius - tickLen
            val endR = radius

            val startX = center.x + startR * sin(angle).toFloat()
            val startY = center.y - startR * cos(angle).toFloat()
            val endX = center.x + endR * sin(angle).toFloat()
            val endY = center.y - endR * cos(angle).toFloat()

            drawLine(
                color = if (isMajor) oliveGreen else secondaryText.copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
            )
        }

        // ── Cardinal labels ──
        val cardinals = listOf("N", "E", "S", "W")
        val cardinalAngles = listOf(0f, 90f, 180f, 270f)
        cardinals.forEachIndexed { idx, label ->
            val angle = Math.toRadians((cardinalAngles[idx] - azimuth).toDouble())
            val labelR = radius * 0.75f
            val x = center.x + labelR * sin(angle).toFloat()
            val y = center.y - labelR * cos(angle).toFloat()

            val isNorth = label == "N"
            val style = TextStyle(
                fontSize = if (isNorth) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNorth) alertRed else highContrastWhite,
                fontFamily = FontFamily.Monospace
            )
            val measured = textMeasurer.measure(label, style)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(x - measured.size.width / 2f, y - measured.size.height / 2f),
                style = style
            )
        }

        // ── Intercardinal labels ──
        val intercardinals = listOf("NE", "SE", "SW", "NW")
        val intercardinalAngles = listOf(45f, 135f, 225f, 315f)
        intercardinals.forEachIndexed { idx, label ->
            val angle = Math.toRadians((intercardinalAngles[idx] - azimuth).toDouble())
            val labelR = radius * 0.75f
            val x = center.x + labelR * sin(angle).toFloat()
            val y = center.y - labelR * cos(angle).toFloat()

            val style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryText,
                fontFamily = FontFamily.Monospace
            )
            val measured = textMeasurer.measure(label, style)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(x - measured.size.width / 2f, y - measured.size.height / 2f),
                style = style
            )
        }

        // ── Target bearing arrow ──
        if (targetBearing != null) {
            val arrowAngle = Math.toRadians(targetBearing.toDouble())
            val arrowLength = radius * 0.55f
            val arrowWidth = 18.dp.toPx()

            val tipX = center.x + arrowLength * sin(arrowAngle).toFloat()
            val tipY = center.y - arrowLength * cos(arrowAngle).toFloat()

            val baseAngle1 = arrowAngle + Math.PI * 0.85
            val baseAngle2 = arrowAngle - Math.PI * 0.85
            val baseR = arrowWidth

            val base1X = center.x + baseR * sin(baseAngle1).toFloat()
            val base1Y = center.y - baseR * cos(baseAngle1).toFloat()
            val base2X = center.x + baseR * sin(baseAngle2).toFloat()
            val base2Y = center.y - baseR * cos(baseAngle2).toFloat()

            val arrowPath = Path().apply {
                moveTo(tipX, tipY)
                lineTo(base1X, base1Y)
                lineTo(center.x, center.y)
                lineTo(base2X, base2Y)
                close()
            }

            drawPath(arrowPath, color = proximityColor, alpha = 0.9f)
            drawPath(
                arrowPath,
                color = proximityColor,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // ── Center dot ──
        drawCircle(
            color = oliveGreen,
            radius = 8.dp.toPx(),
            center = center
        )
        drawCircle(
            color = matteBlack,
            radius = 4.dp.toPx(),
            center = center
        )

        // ── Fixed north indicator (top of screen) ──
        val northIndicatorY = center.y - radius - 16.dp.toPx()
        drawLine(
            color = alertRed,
            start = Offset(center.x, center.y - radius + 4.dp.toPx()),
            end = Offset(center.x, northIndicatorY + 8.dp.toPx()),
            strokeWidth = 3.dp.toPx()
        )
        // Triangle tip
        val triPath = Path().apply {
            moveTo(center.x, northIndicatorY)
            lineTo(center.x - 6.dp.toPx(), northIndicatorY + 10.dp.toPx())
            lineTo(center.x + 6.dp.toPx(), northIndicatorY + 10.dp.toPx())
            close()
        }
        drawPath(triPath, color = alertRed)
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    label: String,
    value: String,
    subvalue: String?,
    valueColor: Color = TacticalColors.HighContrastWhite,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TacticalColors.CardSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TacticalColors.OliveGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (subvalue != null) {
                Text(
                    subvalue,
                    style = MaterialTheme.typography.labelSmall,
                    color = TacticalColors.SecondaryText,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

fun headingLabel(degrees: Float): String {
    val d = ((degrees % 360) + 360) % 360
    return when {
        d < 22.5 || d >= 337.5 -> "N"
        d < 67.5 -> "NE"
        d < 112.5 -> "E"
        d < 157.5 -> "SE"
        d < 202.5 -> "S"
        d < 247.5 -> "SW"
        d < 292.5 -> "W"
        else -> "NW"
    }
}

fun proximityLevelColor(level: ProximityLevel) = when (level) {
    ProximityLevel.FAR -> TacticalColors.ProxFar
    ProximityLevel.NEAR -> TacticalColors.ProxNear
    ProximityLevel.CLOSE -> TacticalColors.ProxClose
    ProximityLevel.VERY_CLOSE -> TacticalColors.ProxVeryClose
    ProximityLevel.IMMEDIATE -> TacticalColors.ProxImmediate
    ProximityLevel.CRITICAL -> TacticalColors.ProxCritical
    ProximityLevel.ARRIVED -> TacticalColors.ProxArrived
    else -> TacticalColors.ProxFar
}
