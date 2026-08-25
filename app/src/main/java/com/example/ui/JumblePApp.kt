package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppMode
import com.example.ui.components.*
import com.example.ui.theme.JumblePTheme
import com.example.ui.viewmodel.JumblePViewModel
import kotlinx.coroutines.launch

@Composable
fun JumblePApp(
    viewModel: JumblePViewModel
) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val messagesMap by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isJamOverlayVisible by viewModel.isJamOverlayVisible.collectAsStateWithLifecycle()
    val isThemeSheetVisible by viewModel.isThemeSheetVisible.collectAsStateWithLifecycle()
    val isVoiceSessionVisible by viewModel.isVoiceSessionVisible.collectAsStateWithLifecycle()
    val isTabletHudVisible by viewModel.isTabletHudVisible.collectAsStateWithLifecycle()
    val activeTabletPlan by viewModel.activeTabletPlan.collectAsStateWithLifecycle()
    val tabletScrollOffset by viewModel.tabletScrollOffset.collectAsStateWithLifecycle()
    val availableScreenshots by viewModel.availableScreenshots.collectAsStateWithLifecycle()
    val selectedScreenshot by viewModel.selectedScreenshot.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val quizQuestions by viewModel.quizQuestions.collectAsStateWithLifecycle()
    val scratchNotes by viewModel.scratchNotes.collectAsStateWithLifecycle()
    val liveSandboxState by viewModel.liveSandbox.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val spokenText by viewModel.voiceManager.spokenText.collectAsStateWithLifecycle()
    val audioAmplitude by viewModel.voiceManager.audioAmplitude.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentMessages = messagesMap[currentMode].orEmpty()

    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    JumblePTheme(themeStyle = currentTheme) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            viewModel.setThemeSheetVisible(true)
                        }
                    )
                },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopHeaderBar(
                    currentMode = currentMode,
                    currentTheme = currentTheme,
                    onModeSelected = { viewModel.switchMode(it) },
                    onJamClick = { viewModel.triggerJamGreeting() },
                    onThemeClick = { viewModel.setThemeSheetVisible(true) },
                    onVoiceClick = {
                        viewModel.setVoiceSessionVisible(true)
                        viewModel.startVoiceInput()
                    },
                    onLongPressHeader = { viewModel.setThemeSheetVisible(true) }
                )
            },
            bottomBar = {
                // Bottom Input Bar (Always accessible)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Attached screenshot pill indicator
                        if (selectedScreenshot != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Screenshot,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Attached: ${selectedScreenshot?.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { viewModel.selectScreenshot(null) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove screenshot",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Screenshot / Tablet Inspector Button
                            IconButton(
                                onClick = {
                                    viewModel.setTabletHudVisible(!isTabletHudVisible)
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isTabletHudVisible) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                Icon(
                                    Icons.Default.TabletAndroid,
                                    contentDescription = "Tablet View",
                                    tint = if (isTabletHudVisible) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Text Input Field
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = when (currentMode) {
                                            AppMode.RESEARCH -> "Ask research question or say 'Jam'..."
                                            AppMode.LEARNING -> "Ask to break down any concept..."
                                            AppMode.TEACHING -> "Ask for lesson plans & student Q&A..."
                                            AppMode.APP_BUILDER -> "Ask for Android & Compose UI..."
                                            AppMode.SCRATCHPAD -> "Drop a quick thought spark..."
                                        },
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                maxLines = 4
                            )

                            // Voice Input Trigger Button
                            IconButton(
                                onClick = {
                                    viewModel.setVoiceSessionVisible(true)
                                    viewModel.startVoiceInput()
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Send Button
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        viewModel.sendMessage(inputText)
                                        inputText = ""
                                    }
                                },
                                enabled = inputText.isNotBlank() && !isGenerating,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (inputText.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Send Message",
                                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Content Based on Selected Mode
                when (currentMode) {
                    AppMode.RESEARCH -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Animated Tablet Research HUD if opened
                            AnimatedVisibility(visible = isTabletHudVisible) {
                                TabletResearchHud(
                                    plan = activeTabletPlan,
                                    scrollOffset = tabletScrollOffset,
                                    availableScreenshots = availableScreenshots,
                                    selectedScreenshot = selectedScreenshot,
                                    onSelectScreenshot = { viewModel.selectScreenshot(it) },
                                    onExecuteResearchPrompt = { prompt ->
                                        viewModel.sendMessage(prompt)
                                    },
                                    onCloseHud = { viewModel.setTabletHudVisible(false) }
                                )
                            }

                            // Chat Thread
                            LazyColumn(
                                state = chatListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(currentMessages, key = { it.id }) { msg ->
                                    ChatMessageItem(
                                        message = msg,
                                        onSpeak = { viewModel.speakText(it) }
                                    )
                                }

                                if (isGenerating) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Jumble P is researching & synthesizing...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppMode.LEARNING -> {
                        LearningModeView(
                            flashcards = flashcards,
                            quizQuestions = quizQuestions,
                            onToggleMastery = { viewModel.toggleFlashcardMastery(it) },
                            onAnswerQuiz = { qId, optIdx -> viewModel.answerQuiz(qId, optIdx) },
                            onResetQuiz = { viewModel.resetQuiz() },
                            onAskLearningQuestion = { viewModel.sendMessage(it) }
                        )
                    }

                    AppMode.TEACHING -> {
                        TeachingModeView(
                            onGenerateLessonPlan = { viewModel.sendMessage(it) }
                        )
                    }

                    AppMode.APP_BUILDER -> {
                        AppBuilderView(
                            sandboxState = liveSandboxState,
                            onUpdateSandbox = { title, color, radius, elev, showBadge, badgeTxt, showBtn, btnTxt ->
                                viewModel.updateLiveSandbox(title, color, radius, elev, showBadge, badgeTxt, showBtn, btnTxt)
                            },
                            onGenerateCode = { viewModel.sendMessage(it) }
                        )
                    }

                    AppMode.SCRATCHPAD -> {
                        ScratchpadView(
                            notes = scratchNotes,
                            onAddNote = { t, c, tags -> viewModel.addScratchNote(t, c, tags) },
                            onDeleteNote = { viewModel.deleteScratchNote(it) },
                            onCombineIdeas = { viewModel.combineRandomIdeas() },
                            onAskIdeaForge = { viewModel.sendMessage(it) }
                        )
                    }
                }

                // Global Overlays & Modals
                JamGreetingOverlay(
                    isVisible = isJamOverlayVisible,
                    onDismiss = { viewModel.setJamOverlayVisible(false) },
                    onQuickAction = { prompt -> viewModel.sendMessage(prompt) },
                    onVoiceClick = {
                        viewModel.setVoiceSessionVisible(true)
                        viewModel.startVoiceInput()
                    }
                )

                ThemeCustomizerSheet(
                    isVisible = isThemeSheetVisible,
                    currentTheme = currentTheme,
                    onThemeSelected = { viewModel.switchTheme(it) },
                    onDismiss = { viewModel.setThemeSheetVisible(false) }
                )

                VoiceSessionOverlay(
                    isVisible = isVoiceSessionVisible,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    audioAmplitude = audioAmplitude,
                    spokenText = spokenText,
                    onStartListening = { viewModel.voiceManager.startListening {} },
                    onStopListening = { viewModel.voiceManager.stopListening() },
                    onSendSpokenText = { text ->
                        if (text.contains("jam", ignoreCase = true)) {
                            viewModel.triggerJamGreeting()
                        } else {
                            viewModel.sendMessage(text)
                        }
                    },
                    onDismiss = { viewModel.setVoiceSessionVisible(false) }
                )
            }
        }
    }
}
