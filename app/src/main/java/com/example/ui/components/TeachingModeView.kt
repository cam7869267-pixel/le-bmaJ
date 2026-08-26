package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TeachingModeView(
    onGenerateLessonPlan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPersona by remember { mutableStateOf("High-School Student") }
    var topicInput by remember { mutableStateOf("") }

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
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Teaching Architect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Formulate lesson arcs, Feynman analogies, and Socratic challenge questions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Persona Selector
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Target Audience Persona:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val personas = listOf("Explain Like I'm 5", "High-School Student", "Senior Engineer", "Non-Tech Executive")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                personas.chunked(2).forEach { pair ->
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { persona ->
                            val isSelected = selectedPersona == persona
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedPersona = persona }
                            ) {
                                Text(
                                    text = persona,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Topic Input
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "What concept do you want to teach?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                placeholder = { Text("e.g., Cryptographic Hashing, Recursion, Photosynthesis...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Button(
                onClick = {
                    val prompt = if (topicInput.isNotBlank()) {
                        "Generate a complete 4-stage Teaching Blueprint for [$topicInput] tailored for [$selectedPersona], including hook, analogies, and student misconception alerts."
                    } else {
                        "Generate a complete Teaching Blueprint for [Declarative UI Programming] tailored for [$selectedPersona]."
                    }
                    onGenerateLessonPlan(prompt)
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
                Icon(Icons.Default.AutoStories, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Teaching Arc", fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Pre-configured Teaching Templates
        Text(
            text = "Popular Teaching Frameworks:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val frameworks = listOf(
            "Feynman Technique Blueprint" to "Simplify any complex topic into plain conversational English without jargon.",
            "Socratic Dialogue Guide" to "Lead learners to self-discovery through a sequence of provocative guided questions.",
            "Visual Metaphor Generator" to "Create 3 vivid everyday metaphors for abstract mathematical or software concepts."
        )

        frameworks.forEach { (title, desc) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onGenerateLessonPlan("Create a $title for: ${topicInput.ifBlank { "Modern AI Agents" }} tailored for $selectedPersona")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
