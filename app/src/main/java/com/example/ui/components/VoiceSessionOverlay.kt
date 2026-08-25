package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun VoiceSessionOverlay(
    isVisible: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    audioAmplitude: Float,
    spokenText: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSendSpokenText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "voiceRipple")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    val dynamicScale = remember(audioAmplitude, isListening) {
        if (isListening) (1f + audioAmplitude * 0.5f).coerceIn(1f, 1.6f) else 1f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Top Close bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isListening) MaterialTheme.colorScheme.primary
                                        else if (isSpeaking) MaterialTheme.colorScheme.secondary
                                        else Color.Gray
                                    )
                            )
                            Text(
                                text = if (isListening) "LISTENING..." else if (isSpeaking) "JUMBLE P SPEAKING" else "VOICE READY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Multi-ring Glowing Audio Pulse Center Orb
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening || isSpeaking) {
                        // Outer Ripple Ring 2
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .scale(pulse2)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        )
                        // Inner Ripple Ring 1
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .scale(pulse1)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        )
                    }

                    // Main Central Orb
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(dynamicScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .shadow(20.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                            .clickable {
                                if (isListening) onStopListening() else onStartListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // Spoken Transcription Display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (spokenText.isNotBlank()) "\"$spokenText\""
                            else if (isListening) "Listening to your voice... Speak anytime"
                            else "Tap the orb and say 'Jam' or ask Jumble P a question",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                // Action Buttons: Speak Now / Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isListening) onStopListening() else onStartListening()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isListening) "Stop" else "Listen")
                    }

                    Button(
                        onClick = {
                            if (spokenText.isNotBlank()) {
                                onSendSpokenText(spokenText)
                                onDismiss()
                            }
                        },
                        enabled = spokenText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Send Query")
                    }
                }
            }
        }
    }
}
