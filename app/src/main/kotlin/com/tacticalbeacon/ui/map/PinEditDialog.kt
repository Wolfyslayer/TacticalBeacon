package com.tacticalbeacon.ui.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tacticalbeacon.data.model.*
import com.tacticalbeacon.ui.theme.TacticalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEditDialog(
    initialPin: Pin,
    onSave: (Pin) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialPin.name) }
    var notes by remember { mutableStateOf(initialPin.notes) }
    var selectedColor by remember { mutableStateOf(initialPin.color) }
    var selectedIcon by remember { mutableStateOf(initialPin.icon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TacticalColors.CardSurface,
        titleContentColor = TacticalColors.HighContrastWhite,
        textContentColor = TacticalColors.SecondaryText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AddLocation,
                    contentDescription = null,
                    tint = TacticalColors.OliveGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (initialPin.name.startsWith("Pin ")) "DROP PIN" else "EDIT PIN",
                    style = MaterialTheme.typography.titleMedium,
                    color = TacticalColors.OliveGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Coordinates display
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
                        CoordDisplay("LAT", String.format("%.6f", initialPin.latitude))
                        CoordDisplay("LON", String.format("%.6f", initialPin.longitude))
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("PIN NAME") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = tacticalTextFieldColors()
                )

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("NOTES") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = tacticalTextFieldColors()
                )

                // Icon selection
                Text(
                    "ICON TYPE",
                    style = MaterialTheme.typography.labelMedium,
                    color = TacticalColors.OliveGreen,
                    fontWeight = FontWeight.Bold
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PinIcon.entries) { icon ->
                        val isSelected = icon == selectedIcon
                        Surface(
                            onClick = { selectedIcon = icon },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TacticalColors.OliveGreenContainer
                                    else TacticalColors.ElevatedSurface,
                            border = if (isSelected) BorderStroke(1.dp, TacticalColors.OliveGreen)
                                     else null,
                            modifier = Modifier.aspectRatio(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = pinIconVector(icon),
                                    contentDescription = icon.label,
                                    tint = if (isSelected) TacticalColors.OliveGreen
                                           else TacticalColors.SecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    icon.label.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) TacticalColors.OliveGreen
                                            else TacticalColors.DisabledText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Color selection
                Text(
                    "PIN COLOR",
                    style = MaterialTheme.typography.labelMedium,
                    color = TacticalColors.OliveGreen,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PinColor.entries.forEach { color ->
                        val isSelected = color == selectedColor
                        val colorValue = pinColorValue(color)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorValue)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) TacticalColors.HighContrastWhite
                                            else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TacticalColors.HighContrastWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            initialPin.copy(
                                name = name.trim(),
                                notes = notes.trim(),
                                color = selectedColor,
                                icon = selectedIcon,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalColors.OliveGreen,
                    contentColor = TacticalColors.HighContrastWhite
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("SAVE PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TacticalColors.SecondaryText)
            ) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun CoordDisplay(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TacticalColors.OliveGreen)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TacticalColors.HighContrastWhite,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun tacticalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TacticalColors.OliveGreen,
    unfocusedBorderColor = TacticalColors.DisabledText,
    focusedLabelColor = TacticalColors.OliveGreen,
    unfocusedLabelColor = TacticalColors.DisabledText,
    cursorColor = TacticalColors.OliveGreen,
    focusedTextColor = TacticalColors.HighContrastWhite,
    unfocusedTextColor = TacticalColors.HighContrastWhite
)

fun pinIconVector(icon: PinIcon) = when (icon) {
    PinIcon.CAMP -> Icons.Default.Home
    PinIcon.VEHICLE -> Icons.Default.DirectionsCar
    PinIcon.CACHE -> Icons.Default.Inventory2
    PinIcon.HUNTING_STAND -> Icons.Default.Visibility
    PinIcon.WAYPOINT -> Icons.Default.LocationOn
    PinIcon.DANGER -> Icons.Default.Warning
    PinIcon.OBJECTIVE -> Icons.Default.Flag
    PinIcon.EXTRACTION -> Icons.Default.FlightTakeoff
    PinIcon.MEDICAL -> Icons.Default.LocalHospital
    PinIcon.WATER -> Icons.Default.Water
    PinIcon.FOOD -> Icons.Default.Restaurant
    PinIcon.OBSERVATION -> Icons.Default.Visibility
}

fun pinColorValue(color: PinColor) = when (color) {
    PinColor.OLIVE -> TacticalColors.PinOlive
    PinColor.RED -> TacticalColors.PinRed
    PinColor.AMBER -> TacticalColors.PinAmber
    PinColor.BLUE -> TacticalColors.PinBlue
    PinColor.WHITE -> TacticalColors.PinWhite
    PinColor.CYAN -> TacticalColors.PinCyan
    PinColor.PURPLE -> TacticalColors.PinPurple
    PinColor.ORANGE -> TacticalColors.PinOrange
}
