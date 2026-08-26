package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppMode
import com.example.data.model.ThemeStyle

@Composable
fun TopHeaderBar(
    currentMode: AppMode,
    currentTheme: ThemeStyle,
    isWakeWordListening: Boolean,
    isWakeWordServiceRunning: Boolean,
    wakeWordAmplitude: Float,
    onModeSelected: (AppMode) -> Unit,
    onJamClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onWakeWordClick: () -> Unit,
    onThemeClick: () -> Unit,
    onLongPressHeader: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPressHeader()
                    }
                )
            }
            .padding(top = 8.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Logo Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { onLongPressHeader() }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JP",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Jumble P",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "v2.6",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Hold screen to style",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Quick Actions: Wake Word, JAM! Trigger, Voice, Theme
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Wake Word 'Jam' Listening Pill / Badge
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isWakeWordListening) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isWakeWordListening) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onWakeWordClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isWakeWordListening) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                        )
                        Icon(
                            imageVector = if (isWakeWordServiceRunning) Icons.Default.Hearing else Icons.Default.GraphicEq,
                            contentDescription = "Wake Word Settings",
                            tint = if (isWakeWordListening) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isWakeWordServiceRunning) "BG 'Jam'" else "'Jam'",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isWakeWordListening) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Charismatic "JAM" trigger button
                Button(
                    onClick = onJamClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = "Trigger Jam Greeting",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "JAM!",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                // Voice Trigger
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice Session",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Theme palette button
                IconButton(
                    onClick = onThemeClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Customize Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Horizontal Mode Selector Tabs (Kimi-inspired clean pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                val chipBackground by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    animationSpec = tween(200),
                    label = "chipBg"
                )
                val chipBorderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    animationSpec = tween(200),
                    label = "chipBorder"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "chipText"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(chipBackground)
                        .border(1.dp, chipBorderColor, RoundedCornerShape(14.dp))
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val icon = when (mode) {
                            AppMode.RESEARCH -> Icons.Default.Search
                            AppMode.LEARNING -> Icons.Default.School
                            AppMode.TEACHING -> Icons.Default.Psychology
                            AppMode.APP_BUILDER -> Icons.Default.Code
                            AppMode.SCRATCHPAD -> Icons.Default.Lightbulb
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = mode.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
