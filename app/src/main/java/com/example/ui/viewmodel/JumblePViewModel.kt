package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.model.*
import com.example.data.tts.VoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class JumblePViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiClient = GeminiClient()
    val voiceManager = VoiceManager(application.applicationContext)

    private val _currentMode = MutableStateFlow(AppMode.RESEARCH)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _currentTheme = MutableStateFlow(ThemeStyle.OBSIDIAN_KIMI)
    val currentTheme: StateFlow<ThemeStyle> = _currentTheme.asStateFlow()

    private val _isJamOverlayVisible = MutableStateFlow(false)
    val isJamOverlayVisible: StateFlow<Boolean> = _isJamOverlayVisible.asStateFlow()

    private val _isThemeSheetVisible = MutableStateFlow(false)
    val isThemeSheetVisible: StateFlow<Boolean> = _isThemeSheetVisible.asStateFlow()

    private val _isVoiceSessionVisible = MutableStateFlow(false)
    val isVoiceSessionVisible: StateFlow<Boolean> = _isVoiceSessionVisible.asStateFlow()

    private val _isTabletHudVisible = MutableStateFlow(false)
    val isTabletHudVisible: StateFlow<Boolean> = _isTabletHudVisible.asStateFlow()

    private val _activeTabletPlan = MutableStateFlow<ResearchPlan?>(null)
    val activeTabletPlan: StateFlow<ResearchPlan?> = _activeTabletPlan.asStateFlow()

    private val _tabletScrollOffset = MutableStateFlow(0f)
    val tabletScrollOffset: StateFlow<Float> = _tabletScrollOffset.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _messages = MutableStateFlow<Map<AppMode, List<ChatMessage>>>(
        AppMode.values().associateWith { mode ->
            listOf(createInitialWelcomeMessage(mode))
        }
    )
    val messages: StateFlow<Map<AppMode, List<ChatMessage>>> = _messages.asStateFlow()

    private val _selectedScreenshot = MutableStateFlow<ScreenshotContext?>(null)
    val selectedScreenshot: StateFlow<ScreenshotContext?> = _selectedScreenshot.asStateFlow()

    private val _availableScreenshots = MutableStateFlow(createSampleScreenshots())
    val availableScreenshots: StateFlow<List<ScreenshotContext>> = _availableScreenshots.asStateFlow()

    // Learning Mode state
    private val _flashcards = MutableStateFlow(createInitialFlashcards())
    val flashcards: StateFlow<List<FlashCard>> = _flashcards.asStateFlow()

    private val _quizQuestions = MutableStateFlow(createInitialQuizQuestions())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    // Scratchpad / Idea Forge state
    private val _scratchNotes = MutableStateFlow(createInitialScratchNotes())
    val scratchNotes: StateFlow<List<ScratchNote>> = _scratchNotes.asStateFlow()

    // App Builder Live Sandbox state
    private val _liveSandbox = MutableStateFlow(LiveSandboxUiState())
    val liveSandbox: StateFlow<LiveSandboxUiState> = _liveSandbox.asStateFlow()

    fun switchMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun switchTheme(theme: ThemeStyle) {
        _currentTheme.value = theme
    }

    fun setJamOverlayVisible(visible: Boolean) {
        _isJamOverlayVisible.value = visible
    }

    fun setThemeSheetVisible(visible: Boolean) {
        _isThemeSheetVisible.value = visible
    }

    fun setVoiceSessionVisible(visible: Boolean) {
        _isVoiceSessionVisible.value = visible
        if (!visible) {
            voiceManager.stopListening()
        }
    }

    fun setTabletHudVisible(visible: Boolean) {
        _isTabletHudVisible.value = visible
    }

    fun selectScreenshot(screenshot: ScreenshotContext?) {
        _selectedScreenshot.value = screenshot
    }

    fun triggerJamGreeting() {
        _isJamOverlayVisible.value = true
        voiceManager.speak("What's up! Jumble P is ready. What are we researching or building today?")
    }

    fun sendMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        // If user says "Jam" or "hey jam", trigger greeting
        if (trimmed.equals("jam", ignoreCase = true) || trimmed.equals("hey jam", ignoreCase = true)) {
            triggerJamGreeting()
            return
        }

        val mode = _currentMode.value
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = trimmed,
            mode = mode,
            screenshotAttachment = _selectedScreenshot.value
        )

        val currentList = _messages.value[mode].orEmpty().toMutableList()
        currentList.add(userMsg)
        _messages.value = _messages.value + (mode to currentList)

        val activeScreenshot = _selectedScreenshot.value
        _selectedScreenshot.value = null // reset selection after sending

        viewModelScope.launch {
            _isGenerating.value = true

            // If research mode, trigger simulated tablet control & research plan
            if (mode == AppMode.RESEARCH) {
                runSimulatedTabletResearch(trimmed, activeScreenshot)
            }

            try {
                val aiResponse = geminiClient.generateAiResponse(trimmed, mode, activeScreenshot)
                val updatedList = _messages.value[mode].orEmpty().toMutableList()
                updatedList.add(aiResponse)
                _messages.value = _messages.value + (mode to updatedList)

                // Proactively extract flashcards / scratch ideas if relevant
                if (mode == AppMode.LEARNING) {
                    addNewFlashcardFromPrompt(trimmed, aiResponse.content)
                } else if (mode == AppMode.SCRATCHPAD) {
                    addScratchNoteFromIdea(trimmed, aiResponse.content)
                }
            } catch (e: Exception) {
                val fallbackMsg = geminiClient.generateSynthesizedResponse(trimmed, mode, activeScreenshot)
                val updatedList = _messages.value[mode].orEmpty().toMutableList()
                updatedList.add(fallbackMsg)
                _messages.value = _messages.value + (mode to updatedList)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private suspend fun runSimulatedTabletResearch(query: String, screenshot: ScreenshotContext?) {
        val steps = listOf(
            ResearchStep(UUID.randomUUID().toString(), "Search Multi-Domain Indexes", "Querying academic repositories and verified engineering docs", ResearchActionType.WEB_SEARCH, StepState.IN_PROGRESS),
            ResearchStep(UUID.randomUUID().toString(), "Tablet Screen OCR & Viewport Scroll", if (screenshot != null) "Parsed screenshot visual hierarchy and text bounding boxes" else "Virtual browser scrolling through dynamic document frames", ResearchActionType.SCROLL_TABLET, StepState.PENDING),
            ResearchStep(UUID.randomUUID().toString(), "Capture Key Evidence Frames", "Extracted 4 quantitative diagrams and core equations", ResearchActionType.SCREENSHOT_CAPTURE, StepState.PENDING),
            ResearchStep(UUID.randomUUID().toString(), "Synthesize Executive Findings", "Assembled structured report with verified citations", ResearchActionType.SYNTHESIZE, StepState.PENDING)
        )

        val plan = ResearchPlan(query, steps, isExecuting = true, currentStepIndex = 0)
        _activeTabletPlan.value = plan
        _isTabletHudVisible.value = true

        // Animate simulated steps
        for (i in steps.indices) {
            delay(400)
            _tabletScrollOffset.value = (i + 1) * 220f
            val updatedSteps = steps.mapIndexed { index, step ->
                when {
                    index < i -> step.copy(state = StepState.COMPLETED)
                    index == i -> step.copy(state = StepState.IN_PROGRESS)
                    else -> step.copy(state = StepState.PENDING)
                }
            }
            _activeTabletPlan.value = plan.copy(steps = updatedSteps, currentStepIndex = i)
        }
        delay(300)
        _activeTabletPlan.value = plan.copy(
            steps = steps.map { it.copy(state = StepState.COMPLETED) },
            isExecuting = false,
            currentStepIndex = steps.size
        )
    }

    fun startVoiceInput() {
        voiceManager.startListening { spoken ->
            if (spoken.isNotBlank()) {
                if (spoken.contains("jam", ignoreCase = true)) {
                    triggerJamGreeting()
                } else {
                    sendMessage(spoken)
                }
            }
        }
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    // Flashcard Actions
    fun toggleFlashcardMastery(cardId: String) {
        _flashcards.value = _flashcards.value.map { card ->
            if (card.id == cardId) card.copy(isMastered = !card.isMastered) else card
        }
    }

    fun answerQuiz(questionId: String, selectedIdx: Int) {
        _quizQuestions.value = _quizQuestions.value.map { q ->
            if (q.id == questionId) q.copy(selectedIndex = selectedIdx) else q
        }
    }

    fun resetQuiz() {
        _quizQuestions.value = _quizQuestions.value.map { it.copy(selectedIndex = null) }
    }

    // Scratchpad Actions
    fun addScratchNote(title: String, content: String, tags: List<String>) {
        val newNote = ScratchNote(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Brainstorm Spark" },
            content = content,
            tags = tags.ifEmpty { listOf("Idea", "Jumble P") },
            colorIndex = (_scratchNotes.value.size % 5)
        )
        _scratchNotes.value = listOf(newNote) + _scratchNotes.value
    }

    fun deleteScratchNote(noteId: String) {
        _scratchNotes.value = _scratchNotes.value.filter { it.id != noteId }
    }

    fun combineRandomIdeas(): String {
        val ideas = _scratchNotes.value
        if (ideas.size < 2) return "Add at least two notes to synthesize a combo idea!"
        val first = ideas.random()
        val second = (ideas - first).random()
        val comboTitle = "Fusion: ${first.title.take(15)} × ${second.title.take(15)}"
        val comboContent = "What if we took [${first.content.take(60)}...] and connected it with [${second.content.take(60)}...]?\n\nResulting Paradigm: A self-adapting agent workflow that automates research and executes live preview verification simultaneously."
        addScratchNote(comboTitle, comboContent, listOf("Synthesis", "Fusion"))
        return "Synthesized new concept card!"
    }

    // Live Sandbox Updates
    fun updateLiveSandbox(
        titleText: String? = null,
        primaryColorHex: String? = null,
        cornerRadiusDp: Int? = null,
        elevationDp: Int? = null,
        showBadge: Boolean? = null,
        badgeText: String? = null,
        showButton: Boolean? = null,
        buttonText: String? = null
    ) {
        val current = _liveSandbox.value
        _liveSandbox.value = current.copy(
            titleText = titleText ?: current.titleText,
            primaryColorHex = primaryColorHex ?: current.primaryColorHex,
            cornerRadiusDp = cornerRadiusDp ?: current.cornerRadiusDp,
            elevationDp = elevationDp ?: current.elevationDp,
            showBadge = showBadge ?: current.showBadge,
            badgeText = badgeText ?: current.badgeText,
            showButton = showButton ?: current.showButton,
            buttonText = buttonText ?: current.buttonText
        )
    }

    private fun addNewFlashcardFromPrompt(prompt: String, answer: String) {
        val newCard = FlashCard(
            id = UUID.randomUUID().toString(),
            topic = prompt.take(25),
            question = "How does \"$prompt\" fundamentally function?",
            answer = answer.take(160).replace("#", "").trim() + "..."
        )
        _flashcards.value = listOf(newCard) + _flashcards.value
    }

    private fun addScratchNoteFromIdea(prompt: String, idea: String) {
        val newNote = ScratchNote(
            id = UUID.randomUUID().toString(),
            title = prompt.take(30),
            content = idea.take(200).replace("#", "").trim(),
            tags = listOf("AI Spark", "Forge"),
            colorIndex = (_scratchNotes.value.size % 5)
        )
        _scratchNotes.value = listOf(newNote) + _scratchNotes.value
    }

    private fun createInitialWelcomeMessage(mode: AppMode): ChatMessage {
        val text = when (mode) {
            AppMode.RESEARCH -> "👋 **I am Jumble P**, your intelligent multimodal research agent.\n\nAsk me any deep question, attach a tablet screenshot for OCR analysis, or tap **JAM!** to trigger quick actions. I will scour references, simulate viewport exploration, and craft structured research reports."
            AppMode.LEARNING -> "🎓 **Welcome to Learning Mode.**\n\nAsk me to deconstruct complex topics into mental models, practice with interactive flashcards, or take a quick comprehension quiz."
            AppMode.TEACHING -> "👩‍🏫 **Welcome to Teaching Mode.**\n\nI will help you create lesson plans, anticipate student misconceptions, and design interactive Socratic inquiry arcs for any subject."
            AppMode.APP_BUILDER -> "⚡ **Welcome to App Builder & Interference.**\n\nLet's design and test Android Jetpack Compose components! You can generate clean Kotlin code, preview interactive UI in our live sandbox, and inspect architectures."
            AppMode.SCRATCHPAD -> "💡 **Welcome to the Idea Forge.**\n\nBrainstorm wild concepts, combine unexpected ideas into fresh ventures, and store quick sticky insights."
        }

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.ASSISTANT,
            content = text,
            mode = mode
        )
    }

    private fun createSampleScreenshots(): List<ScreenshotContext> {
        return listOf(
            ScreenshotContext(
                id = "screen_1",
                title = "Quantum Computing Roadmap",
                category = "Academic Paper",
                description = "Dense 2026 quantum coherence benchmark with qubit scaling curves and cryogenic control diagrams.",
                simulatedUrl = "https://quantum-research.org/benchmarks/2026"
            ),
            ScreenshotContext(
                id = "screen_2",
                title = "Compose Navigation Architecture",
                category = "Technical Docs",
                description = "Type-safe navigation backstack diagram with viewmodel scope boundaries and state restoration.",
                simulatedUrl = "https://developer.android.com/guide/navigation"
            ),
            ScreenshotContext(
                id = "screen_3",
                title = "Video Tutorial Final Frame",
                category = "Video Capture",
                description = "Screenshot taken at the end of a high-speed robotics algorithm walkthrough with final equations.",
                simulatedUrl = "https://video.learnstream.ai/v/robotics-492"
            ),
            ScreenshotContext(
                id = "screen_4",
                title = "FinTech Dashboard Analytics",
                category = "Mobile UI Wireframe",
                description = "Dark mode financial analytics screen showing multi-currency yield curves and transaction ledger.",
                simulatedUrl = "https://designs.prototype.io/fintech-dark"
            )
        )
    }

    private fun createInitialFlashcards(): List<FlashCard> {
        return listOf(
            FlashCard("fc_1", "Jetpack Compose", "What is State Hoisting and why is it recommended?", "State hoisting is moving state to a caller to make a composable stateless, testable, and reusable."),
            FlashCard("fc_2", "Gemini API", "What is the primary benefit of Multimodal Input?", "It allows models to simultaneously process text, visual screenshot structures, and audio context."),
            FlashCard("fc_3", "Clean Architecture", "What is the Single Responsibility Principle?", "A class or function should have one, and only one, reason to change.")
        )
    }

    private fun createInitialQuizQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_1",
                question = "Which keyword in Kotlin Compose ensures expensive computations aren't re-run every recomposition?",
                options = listOf("remember", "launch", "mutableStateOf", "derivedStateOf"),
                correctIndex = 0,
                explanation = "`remember` caches values across recompositions until input keys change."
            ),
            QuizQuestion(
                id = "q_2",
                question = "What is the minimum recommended touch target size in Material Design 3 for Android accessibility?",
                options = listOf("32dp", "40dp", "48dp", "64dp"),
                correctIndex = 2,
                explanation = "Material Design 3 specifies 48dp x 48dp as the standard minimum touch target."
            )
        )
    }

    private fun createInitialScratchNotes(): List<ScratchNote> {
        return listOf(
            ScratchNote(
                id = "sn_1",
                title = "Tablet Auto-Scroll Research",
                content = "When reading a long paper or video, Jumble P can simulate continuous viewport scrolls and capture highlights automatically.",
                tags = listOf("Research", "Tablet"),
                colorIndex = 0
            ),
            ScratchNote(
                id = "sn_2",
                title = "Haptic Style Swapper",
                content = "Long pressing the screen brings up 5 tailored design styles (Kimi Obsidian, Cyber Slate, Holo Violet).",
                tags = listOf("Design", "UI"),
                colorIndex = 1
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
