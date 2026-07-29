package com.tacticalbeacon.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tacticalbeacon.ui.theme.TacticalColors

/**
 * Dialog for downloading offline map tiles for the current map view.
 */
@Composable
fun OfflineMapDialog(
    currentZoom: Double,
    onDownload: (minZoom: Int, maxZoom: Int) -> Unit,
    onDismiss: () -> Unit,
    isDownloading: Boolean = false,
    downloadProgress: Int = 0,
    totalTiles: Int = 0
) {
    var minZoom by remember { mutableIntStateOf(maxOf(8, currentZoom.toInt() - 4)) }
    var maxZoom by remember { mutableIntStateOf(minOf(18, currentZoom.toInt() + 2)) }

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        containerColor = TacticalColors.CardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Download,
                    null,
                    tint = TacticalColors.OliveGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "DOWNLOAD OFFLINE MAP",
                    style = MaterialTheme.typography.titleMedium,
                    color = TacticalColors.OliveGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isDownloading) {
                    // Progress display
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Downloading tiles...",
                            color = TacticalColors.HighContrastWhite
                        )
                        LinearProgressIndicator(
                            progress = { if (totalTiles > 0) downloadProgress.toFloat() / totalTiles else 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = TacticalColors.OliveGreen,
                            trackColor = TacticalColors.ElevatedSurface
                        )
                        Text(
                            "$downloadProgress / $totalTiles tiles",
                            style = MaterialTheme.typography.labelMedium,
                            color = TacticalColors.SecondaryText
                        )
                    }
                } else {
                    Text(
                        "Downloads map tiles for the current view area so the app works completely offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalColors.SecondaryText
                    )

                    Surface(
                        color = TacticalColors.ElevatedSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Min zoom level", color = TacticalColors.SecondaryText)
                                Text("$minZoom", color = TacticalColors.HighContrastWhite, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = minZoom.toFloat(),
                                onValueChange = { minZoom = it.toInt() },
                                valueRange = 4f..16f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = TacticalColors.OliveGreen,
                                    activeTrackColor = TacticalColors.OliveGreen,
                                    inactiveTrackColor = TacticalColors.CardSurface
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Max zoom level", color = TacticalColors.SecondaryText)
                                Text("$maxZoom", color = TacticalColors.HighContrastWhite, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = maxZoom.toFloat(),
                                onValueChange = { maxZoom = it.toInt().coerceAtLeast(minZoom) },
                                valueRange = 8f..18f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = TacticalColors.OliveGreen,
                                    activeTrackColor = TacticalColors.OliveGreen,
                                    inactiveTrackColor = TacticalColors.CardSurface
                                )
                            )
                        }
                    }

                    Surface(
                        color = TacticalColors.AlertAmber.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = TacticalColors.AlertAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Higher zoom levels require significantly more storage. Zoom 16 can use 100+ MB for a small area.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalColors.AlertAmber
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(
                    onClick = { onDownload(minZoom, maxZoom) },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalColors.OliveGreen)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("DOWNLOAD", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloading,
                colors = ButtonDefaults.textButtonColors(contentColor = TacticalColors.SecondaryText)
            ) {
                Text(if (isDownloading) "RUNNING..." else "CANCEL")
            }
        }
    )
}
