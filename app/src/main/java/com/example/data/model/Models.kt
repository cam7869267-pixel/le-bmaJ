package com.example.data.model

import android.graphics.Bitmap

enum class AppMode(val displayName: String, val tagline: String, val iconName: String) {
    RESEARCH("Master Research", "Deep web search, tablet control & screen analysis", "AutoAwesome"),
    LEARNING("Learning Mode", "Step-by-step concepts, quizzes & flashcards", "School"),
    TEACHING("Teaching Mode", "Pedagogical frameworks, lesson plans & Q&A", "Psychology"),
    APP_BUILDER("App Builder", "Jetpack Compose sandbox, code generator & UI lab", "Code"),
    SCRATCHPAD("Idea Forge", "Random thought synthesis & chaotic brainstorming", "Lightbulb")
}

enum class ThemeStyle(val displayName: String, val subtitle: String) {
    OBSIDIAN_KIMI("Kimi Obsidian", "Deep dark canvas with neon emerald highlights"),
    TITANIUM_CYBER("Titanium Cyber", "Polished dark slate with electric cyan"),
    HOLOGRAPHIC_VIOLET("Holo Violet", "Cosmic indigo with radiant purple-pink glow"),
    PAPER_LIGHT("Paper Minimal", "Crisp editorial light theme with deep charcoal text"),
    SOLAR_AMBER("Solar Amber", "Charcoal noir with luminous warm amber")
}

data class Citation(
    val title: String,
    val url: String,
    val snippet: String,
    val sourceIndex: Int
)

data class CodeSnippet(
    val language: String,
    val title: String,
    val code: String,
    val isRunnable: Boolean = true
)

enum class StepState {
    PENDING, IN_PROGRESS, COMPLETED
}

enum class ResearchActionType {
    WEB_SEARCH, SCROLL_TABLET, SCREENSHOT_CAPTURE, OCR_EXTRACT, SYNTHESIZE
}

data class ResearchStep(
    val id: String,
    val title: String,
    val description: String,
    val actionType: ResearchActionType,
    val state: StepState = StepState.PENDING,
    val durationMs: Long = 1200L
)

data class ResearchPlan(
    val query: String,
    val steps: List<ResearchStep>,
    val isExecuting: Boolean = false,
    val currentStepIndex: Int = 0
)

data class ScreenshotContext(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val simulatedUrl: String = "",
    val sampleBitmap: Bitmap? = null
)

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: AppMode = AppMode.RESEARCH,
    val reasoningSteps: List<String> = emptyList(),
    val citations: List<Citation> = emptyList(),
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val screenshotAttachment: ScreenshotContext? = null,
    val researchPlan: ResearchPlan? = null,
    val isResearchReport: Boolean = false,
    val isThinking: Boolean = false
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class FlashCard(
    val id: String,
    val topic: String,
    val question: String,
    val answer: String,
    val isMastered: Boolean = false
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val selectedIndex: Int? = null
)

data class ScratchNote(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    val colorIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveSandboxUiState(
    val componentType: String = "Card",
    val titleText: String = "Dynamic Metric Card",
    val primaryColorHex: String = "#00D492",
    val cornerRadiusDp: Int = 16,
    val elevationDp: Int = 4,
    val showBadge: Boolean = true,
    val badgeText: String = "PRO",
    val showButton: Boolean = true,
    val buttonText: String = "Explore Details",
    val isInteractiveActive: Boolean = false
)
