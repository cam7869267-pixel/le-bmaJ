package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlashCard
import com.example.data.model.QuizQuestion

@Composable
fun LearningModeView(
    flashcards: List<FlashCard>,
    quizQuestions: List<QuizQuestion>,
    onToggleMastery: (String) -> Unit,
    onAnswerQuiz: (String, Int) -> Unit,
    onResetQuiz: () -> Unit,
    onAskLearningQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Mode Header Banner
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
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Recall Lab",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Flip flashcards or challenge yourself with instant micro-quizzes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: Flashcards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interactive Flashcards (${flashcards.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap card to flip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(flashcards) { card ->
                    FlashcardItem(card = card, onToggleMastery = { onToggleMastery(card.id) })
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Section 2: Mini-Quiz Challenge
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comprehension Micro-Quiz",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onResetQuiz) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset Quiz", fontSize = 12.sp)
                }
            }

            quizQuestions.forEach { question ->
                QuizCardItem(
                    question = question,
                    onOptionSelected = { idx -> onAnswerQuiz(question.id, idx) }
                )
            }
        }

        // Quick Learning Prompt Suggestion Chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Ask Jumble P to Explain:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val learningPrompts = listOf(
                "Explain Quantum Superposition using a spinning coin analogy",
                "How does Kotlin Coroutines Dispatchers.IO work behind the scenes?",
                "Break down Transformer Attention mechanisms in 4 steps"
            )

            learningPrompts.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAskLearningQuestion(prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: FlashCard,
    onToggleMastery: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(180.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable { isFlipped = !isFlipped }
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (card.isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .graphicsLayer {
                    // Counter-rotate text so it doesn't show backwards when flipped
                    if (rotation > 90f) rotationY = 180f
                },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isFlipped) "ANSWER" else card.topic.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onToggleMastery,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (card.isMastered) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                        contentDescription = "Toggle Mastered",
                        tint = if (card.isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Body
            Text(
                text = if (isFlipped) card.answer else card.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isFlipped) FontWeight.Normal else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4
            )

            // Footer hint
            Text(
                text = if (isFlipped) "Tap to see question" else "Tap to flip answer",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun QuizCardItem(
    question: QuizQuestion,
    onOptionSelected: (Int) -> Unit
) {
    val isAnswered = question.selectedIndex != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEachIndexed { index, option ->
                    val isSelected = question.selectedIndex == index
                    val isCorrect = index == question.correctIndex

                    val chipBg = when {
                        !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        isSelected && isCorrect -> Color(0xFF10B981).copy(alpha = 0.2f)
                        isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    val borderColor = when {
                        !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                        isAnswered && isCorrect -> Color(0xFF10B981)
                        isAnswered && isSelected && !isCorrect -> Color(0xFFEF4444)
                        else -> Color.Transparent
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = chipBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAnswered) { onOptionSelected(index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${('A' + index)}. $option",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (isAnswered) {
                                if (isCorrect) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Incorrect",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isAnswered) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Explanation:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
