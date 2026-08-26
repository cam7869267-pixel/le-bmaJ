package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.wakeword.WakeDetectionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WakeWordControlSheet(
    isVisible: Boolean,
    isListening: Boolean,
    isBackgroundServiceRunning: Boolean,
    wakeWordStatus: WakeDetectionStatus,
    audioAmplitude: Float,
    detectionCount: Int,
    lastDetectedKeyword: String?,
    sensitivity: Float,
    onToggleListening: (Boolean) -> Unit,
    onToggleBackgroundService: (Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onSimulateWakeWord: () -> Unit,
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "wakePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .clickable(enabled = false) {}
                    .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hearing,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Wake Word Detection",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Say \"Jam\" to summon Jumble P",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Live Audio Waveform & Status Hero Box
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (wakeWordStatus == WakeDetectionStatus.WAKE_WORD_DETECTED) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Live Status Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (wakeWordStatus) {
                                    WakeDetectionStatus.WAKE_WORD_DETECTED -> MaterialTheme.colorScheme.primary
                                    WakeDetectionStatus.LISTENING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .scale(if (isListening) pulseScale else 1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (wakeWordStatus == WakeDetectionStatus.WAKE_WORD_DETECTED) Color.Black
                                                else if (isListening) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                    )
                                    Text(
                                        text = when (wakeWordStatus) {
                                            WakeDetectionStatus.WAKE_WORD_DETECTED -> "✨ 'JAM' HEARD! LAUNCHING..."
                                            WakeDetectionStatus.LISTENING -> "MIC ACTIVE • LISTENING FOR 'JAM'"
                                            WakeDetectionStatus.ERROR -> "MIC PERMISSION REQUIRED"
                                            WakeDetectionStatus.IDLE -> "LISTENER PAUSED"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (wakeWordStatus == WakeDetectionStatus.WAKE_WORD_DETECTED) Color.Black
                                        else if (isListening) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Dynamic Multi-Bar Waveform Visualizer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val barCount = 14
                                for (i in 0 until barCount) {
                                    val factor = ((i - barCount / 2).coerceAtLeast(-barCount / 2).toFloat() / barCount)
                                    val waveOffset = (1f - kotlin.math.abs(factor))
                                    val heightFactor = if (isListening) {
                                        ((audioAmplitude * 1.5f + (i % 3) * 0.1f) * waveOffset).coerceIn(0.12f, 1f)
                                    } else {
                                        0.08f
                                    }

                                    val barHeight = (44.dp * heightFactor).coerceAtLeast(6.dp)

                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                )
                                            )
                                    )
                                }
                            }

                            Text(
                                text = "Supported triggers: \"Jam\", \"Hey Jam\", \"OK Jam\", \"Jumble P\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Toggles Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. In-App Wake Word Toggle
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "In-App Wake Detection",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Detects 'Jam' hands-free while app is open",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isListening,
                                    onCheckedChange = {
                                        onRequestPermissions()
                                        onToggleListening(it)
                                    }
                                )
                            }
                        }

                        // 2. Foreground / Background Service Toggle
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Background Listener Service",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "PRO",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Runs persistent foreground mic service to launch Jumble P from lockscreen or other apps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isBackgroundServiceRunning,
                                    onCheckedChange = {
                                        onRequestPermissions()
                                        onToggleBackgroundService(it)
                                    }
                                )
                            }
                        }
                    }

                    // Sensitivity Slider
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Microphone Sensitivity",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(sensitivity * 100).toInt()}% (${if (sensitivity > 0.8f) "High" else if (sensitivity > 0.5f) "Balanced" else "Strict"})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = sensitivity,
                            onValueChange = onSensitivityChange,
                            valueRange = 0.2f..1.0f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Stats & History Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Times Triggered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$detectionCount detections",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Last Detected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = lastDetectedKeyword?.let { "\"$it\" just now" } ?: "None yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Test Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestPermissions,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Permissions", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                onSimulateWakeWord()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Test Say 'Jam!'", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
