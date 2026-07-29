package com.tacticalbeacon.ui.map

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tacticalbeacon.data.model.Pin
import com.tacticalbeacon.data.model.LocationState
import com.tacticalbeacon.location.LocationManager
import com.tacticalbeacon.ui.theme.TacticalColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinDetailSheet(
    pin: Pin,
    isNavigating: Boolean,
    currentLocation: LocationState,
    useMetric: Boolean,
    onNavigate: (Pin) -> Unit,
    onEdit: (Pin) -> Unit,
    onSave: (Pin) -> Unit,
    onDelete: (Pin) -> Unit,
    onDismiss: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TacticalColors.CardSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TacticalColors.DisabledText)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pin header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(pinColorValue(pin.color).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = pinIconVector(pin.icon),
                        contentDescription = null,
                        tint = pinColorValue(pin.color),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        pin.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = TacticalColors.HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        pin.icon.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TacticalColors.OliveGreen
                    )
                }

                if (isNavigating) {
                    Surface(
                        color = TacticalColors.OliveGreenContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TacticalColors.OliveGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = TacticalColors.ElevatedSurface)

            // Coordinates
            Surface(
                color = TacticalColors.ElevatedSurface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CoordItem("LATITUDE", String.format("%.6f°", pin.latitude))
                    CoordItem("LONGITUDE", String.format("%.6f°", pin.longitude))
                    if (pin.altitude != 0.0) {
                        CoordItem("ALT", String.format("%.0fm", pin.altitude))
                    }
                }
            }

            // Distance from current location
            if (currentLocation.isValid) {
                val distance = LocationManager.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    pin.latitude, pin.longitude
                )
                val bearing = LocationManager.bearingTo(
                    currentLocation.latitude, currentLocation.longitude,
                    pin.latitude, pin.longitude
                )
                Surface(
                    color = TacticalColors.ElevatedSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CoordItem(
                            "DISTANCE",
                            LocationManager.formatDistance(distance, useMetric)
                        )
                        CoordItem(
                            "BEARING",
                            "${bearing.toInt()}°"
                        )
                    }
                }
            }

            // Notes
            if (pin.notes.isNotBlank()) {
                Surface(
                    color = TacticalColors.ElevatedSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(
                            "NOTES",
                            style = MaterialTheme.typography.labelSmall,
                            color = TacticalColors.OliveGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            pin.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TacticalColors.HighContrastWhite
                        )
                    }
                }
            }

            // Created date
            Text(
                "Created: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(pin.createdAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = TacticalColors.DisabledText,
                fontFamily = FontFamily.Monospace
            )

            // Action buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Navigate button
                Button(
                    onClick = { onNavigate(pin) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalColors.OliveGreen,
                        contentColor = TacticalColors.HighContrastWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Navigation, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isNavigating) "NAVIGATING..." else "NAVIGATE TO PIN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Edit button
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TacticalColors.OliveGreen
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, TacticalColors.OliveGreenDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("EDIT", fontWeight = FontWeight.Bold)
                    }

                    // Delete button
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TacticalColors.AlertRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, TacticalColors.AlertRed.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("DELETE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog) {
        PinEditDialog(
            initialPin = pin,
            onSave = { updatedPin ->
                onSave(updatedPin)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = TacticalColors.CardSurface,
            title = {
                Text("DELETE PIN", color = TacticalColors.AlertRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Delete \"${pin.name}\"? This cannot be undone.",
                    color = TacticalColors.SecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(pin)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalColors.AlertRed)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TacticalColors.SecondaryText)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun CoordItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TacticalColors.OliveGreen)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = TacticalColors.HighContrastWhite,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
