package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveSandboxUiState

@Composable
fun AppBuilderView(
    sandboxState: LiveSandboxUiState,
    onUpdateSandbox: (
        title: String?,
        color: String?,
        radius: Int?,
        elevation: Int?,
        showBadge: Boolean?,
        badgeText: String?,
        showBtn: Boolean?,
        btnText: String?
    ) -> Unit,
    onGenerateCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customSnippetPrompt by remember { mutableStateOf("") }
    var interactiveClickCount by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Interference & UI Lab",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time Jetpack Compose sandbox, code generator, and architecture optimizer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Interactive Sandbox Preview
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Interactive Compose Preview:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "HOT RECOMPOSE",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            // Live Rendered Composable Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(sandboxState.elevationDp.dp, RoundedCornerShape(sandboxState.cornerRadiusDp.dp)),
                shape = RoundedCornerShape(sandboxState.cornerRadiusDp.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sandboxState.titleText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (sandboxState.showBadge) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = sandboxState.badgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = "Reactive Material 3 element dynamically rendering state hoisted parameters. Taps logged: $interactiveClickCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (sandboxState.showButton) {
                        Button(
                            onClick = {
                                interactiveClickCount++
                                Toast.makeText(context, "Interactive tap #${interactiveClickCount} registered!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(text = sandboxState.buttonText, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Sandbox Parametric Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Component Controls:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Title Input
                OutlinedTextField(
                    value = sandboxState.titleText,
                    onValueChange = { onUpdateSandbox(it, null, null, null, null, null, null, null) },
                    label = { Text("Title Text") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Corner Radius Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Corner Radius: ${sandboxState.cornerRadiusDp}dp", fontSize = 12.sp)
                    }
                    Slider(
                        value = sandboxState.cornerRadiusDp.toFloat(),
                        onValueChange = { onUpdateSandbox(null, null, it.toInt(), null, null, null, null, null) },
                        valueRange = 0f..32f
                    )
                }

                // Elevation Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shadow Elevation: ${sandboxState.elevationDp}dp", fontSize = 12.sp)
                    }
                    Slider(
                        value = sandboxState.elevationDp.toFloat(),
                        onValueChange = { onUpdateSandbox(null, null, null, it.toInt(), null, null, null, null) },
                        valueRange = 0f..16f
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Section 3: Android Jetpack Compose Code Generator
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Generate Android & Compose Code:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = customSnippetPrompt,
                onValueChange = { customSnippetPrompt = it },
                placeholder = { Text("e.g. Floating Speedometer, Draggable Bottom Sheet, Video Feed...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Button(
                onClick = {
                    val prompt = if (customSnippetPrompt.isNotBlank()) {
                        "Generate a production-ready Jetpack Compose component for: [$customSnippetPrompt] with Material 3 styling and full Kotlin snippet."
                    } else {
                        "Generate a production-ready Jetpack Compose Modern Metric Card with animated progress and copyable code."
                    }
                    onGenerateCode(prompt)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Compose Snippet", fontWeight = FontWeight.Bold)
            }
        }
    }
}
