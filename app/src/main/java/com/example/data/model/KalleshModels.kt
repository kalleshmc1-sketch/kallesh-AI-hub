package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class UsagePeriod(val label: String, val multiplier: Int, val resetLabel: String) {
    DAILY("Daily", 1, "Resets daily at 00:00 UTC (Daily Plan: ₹5 / 4 uses)"),
    MONTHLY("Monthly", 1, "30-day billing cycle (Monthly Plan: ₹200 / 100 uses)"),
    YEARLY("Yearly", 1, "365-day annual cycle (Yearly Plan: ₹1,300 / 1,300 uses)")
}

enum class BillingCycle(val label: String, val discountBadge: String) {
    DAILY("Daily (₹5)", "4 Uses/Day"),
    MONTHLY("Monthly (₹200)", "100 Uses/Mo"),
    YEARLY("Yearly (₹1,300)", "1,300 Uses/Yr")
}

enum class SubscriptionPlan(
    val id: String,
    val displayName: String,
    val badgeText: String,
    val priceLabel: String,
    val yearlyPriceLabel: String,
    val priceInr: Int,
    val toolGenerationLimit: Int,
    val planPeriod: UsagePeriod,
    val dailyChatLimit: Int,
    val dailyImageLimit: Int,
    val dailyVideoLimit: Int,
    val dailyFileLimit: Int,
    val dailyVoiceLimit: Int,
    val dailyMusicLimit: Int,
    val maxFileSizeMb: Int
) {
    FREE(
        id = "DAILY",
        displayName = "Daily Plan",
        badgeText = "DAILY • ₹5",
        priceLabel = "₹5 per day",
        yearlyPriceLabel = "₹5 per day (4 AI Tool Generations / day)",
        priceInr = 5,
        toolGenerationLimit = 4,
        planPeriod = UsagePeriod.DAILY,
        dailyChatLimit = 999999, // AI Chat is UNLIMITED and does NOT consume tool generation limits
        dailyImageLimit = 4,
        dailyVideoLimit = 4,
        dailyFileLimit = 4,
        dailyVoiceLimit = 4,
        dailyMusicLimit = 4,
        maxFileSizeMb = 10
    ),
    PRO(
        id = "MONTHLY",
        displayName = "Monthly Plan",
        badgeText = "MONTHLY • ₹200",
        priceLabel = "₹200 per month",
        yearlyPriceLabel = "₹200 per month (100 AI Tool Generations / month)",
        priceInr = 200,
        toolGenerationLimit = 100,
        planPeriod = UsagePeriod.MONTHLY,
        dailyChatLimit = 999999, // AI Chat is UNLIMITED and does NOT consume tool generation limits
        dailyImageLimit = 100,
        dailyVideoLimit = 100,
        dailyFileLimit = 100,
        dailyVoiceLimit = 100,
        dailyMusicLimit = 100,
        maxFileSizeMb = 25
    ),
    PREMIUM(
        id = "YEARLY",
        displayName = "Yearly Plan",
        badgeText = "YEARLY • ₹1,300",
        priceLabel = "₹1,300 per year",
        yearlyPriceLabel = "₹1,300 per year (1,300 AI Tool Generations / year)",
        priceInr = 1300,
        toolGenerationLimit = 1300,
        planPeriod = UsagePeriod.YEARLY,
        dailyChatLimit = 999999, // AI Chat is UNLIMITED and does NOT consume tool generation limits
        dailyImageLimit = 1300,
        dailyVideoLimit = 1300,
        dailyFileLimit = 1300,
        dailyVoiceLimit = 1300,
        dailyMusicLimit = 1300,
        maxFileSizeMb = 100
    );

    val priceDisplay: String get() = "₹$priceInr"
    val periodGenerationLimit: Int get() = toolGenerationLimit
    val billingPeriodLabel: String get() = when (planPeriod) {
        UsagePeriod.DAILY -> "day"
        UsagePeriod.MONTHLY -> "month"
        UsagePeriod.YEARLY -> "year"
    }

    val monthlyChatLimit: Int get() = 999999
    val monthlyImageLimit: Int get() = when (this) {
        FREE -> 4
        PRO -> 100
        PREMIUM -> 1300
    }
    val monthlyVideoLimit: Int get() = monthlyImageLimit
    val yearlyChatLimit: Int get() = 999999
    val yearlyImageLimit: Int get() = when (this) {
        FREE -> 4
        PRO -> 100
        PREMIUM -> 1300
    }
    val yearlyVideoLimit: Int get() = yearlyImageLimit

    fun dailyLimitFor(category: UsageCategory): Int = when (category) {
        UsageCategory.CHAT -> dailyChatLimit
        UsageCategory.IMAGE -> toolGenerationLimit
        UsageCategory.VIDEO -> toolGenerationLimit
        UsageCategory.FILE -> toolGenerationLimit
        UsageCategory.VOICE -> toolGenerationLimit
        UsageCategory.MUSIC -> toolGenerationLimit
    }

    fun monthlyLimitFor(category: UsageCategory): Int =
        if (category == UsageCategory.CHAT) 999999 else toolGenerationLimit

    fun yearlyLimitFor(category: UsageCategory): Int =
        if (category == UsageCategory.CHAT) 999999 else toolGenerationLimit

    fun limitForPeriod(category: UsageCategory, period: UsagePeriod): Int =
        if (category == UsageCategory.CHAT) 999999 else toolGenerationLimit

    fun priceForBillingCycle(cycle: BillingCycle): String = priceLabel

    companion object {
        val DAILY: SubscriptionPlan get() = FREE
        val MONTHLY: SubscriptionPlan get() = PRO
        val YEARLY: SubscriptionPlan get() = PREMIUM
        val ENTERPRISE: SubscriptionPlan get() = PREMIUM

        fun fromId(id: String?): SubscriptionPlan = when (id?.uppercase()?.trim()) {
            "DAILY", "FREE", "STARTER" -> FREE
            "MONTHLY", "PRO" -> PRO
            "YEARLY", "PREMIUM", "ENTERPRISE", "ANNUAL" -> PREMIUM
            else -> FREE
        }
    }
}

enum class UsageCategory(val fieldName: String, val label: String) {
    CHAT("chatCount", "AI Messages"),
    IMAGE("imageCount", "Image Generations"),
    VIDEO("videoCount", "Video Generations"),
    FILE("fileCount", "File Analyses"),
    VOICE("voiceCount", "Voice & Audio"),
    MUSIC("musicCount", "Music Tracks")
}

data class LimitReachedAlertState(
    val category: UsageCategory,
    val usedCount: Int,
    val dailyLimit: Int,
    val currentPlan: SubscriptionPlan,
    val featureName: String = category.label,
    val message: String = "You have reached today's ${category.label} limit ($usedCount / $dailyLimit) on ${currentPlan.displayName}. Upgrade your plan to unlock higher daily limits immediately."
)

data class AiModelOption(
    val alias: String,
    val modelId: String,
    val badge: String,
    val description: String,
    val supportsVision: Boolean = true,
    val requiresPro: Boolean = false
)

val AVAILABLE_CHAT_MODELS = listOf(
    AiModelOption(
        alias = "Kallesh Auto",
        modelId = "gemini-3.5-flash",
        badge = "AUTO",
        description = "General tasks, balanced reasoning, speed, and multimodal intelligence (gemini-3.5-flash)",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Fast AI",
        modelId = "gemini-3.1-flash-lite",
        badge = "FAST",
        description = "Ultra-low latency responses for tasks that should happen fast (gemini-3.1-flash-lite)",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Smart AI",
        modelId = "gemini-3.5-flash",
        badge = "SMART",
        description = "General multi-turn chat, summarization, and document analysis (gemini-3.5-flash)",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Advanced AI",
        modelId = "gemini-3.1-pro-preview",
        badge = "PRO",
        description = "Particularly complex tasks: coding, STEM, architecture, and deep reasoning (gemini-3.1-pro-preview)",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Vision AI",
        modelId = "gemini-3.5-flash",
        badge = "VISION",
        description = "Specialized for reading diagrams, photos, and visual Q&A (gemini-3.5-flash)",
        supportsVision = true,
        requiresPro = false
    )
)

data class ChatbotRolePreset(
    val id: String,
    val title: String,
    val badge: String,
    val recommendedModelId: String,
    val systemInstruction: String
)

val AVAILABLE_CHATBOT_ROLES = listOf(
    ChatbotRolePreset(
        id = "general_assistant",
        title = "Kallesh Assistant",
        badge = "GENERAL",
        recommendedModelId = "gemini-3.5-flash",
        systemInstruction = "You are Kallesh AI Assistant, a friendly, accurate, and comprehensive multi-turn AI helper for general daily tasks, brainstorming, summarization, and problem solving."
    ),
    ChatbotRolePreset(
        id = "software_architect",
        title = "Code Architect",
        badge = "COMPLEX / PRO",
        recommendedModelId = "gemini-3.1-pro-preview",
        systemInstruction = "You are a Principal Software Architect and Senior Full-Stack & Android Engineer. Provide clean, production-grade code, architectural explanations, complexity analysis, and edge-case handling."
    ),
    ChatbotRolePreset(
        id = "fast_concise",
        title = "Quick Express",
        badge = "FAST / LITE",
        recommendedModelId = "gemini-3.1-flash-lite",
        systemInstruction = "You are a high-speed executive assistant optimized for rapid, ultra-concise, bulleted answers with zero fluff."
    ),
    ChatbotRolePreset(
        id = "study_tutor",
        title = "AI Study Tutor",
        badge = "TUTOR",
        recommendedModelId = "gemini-3.5-flash",
        systemInstruction = "You are a Socratic AI Tutor and Exam Coach. Break down complex concepts using intuitive analogies, step-by-step derivations, and active-recall practice questions."
    ),
    ChatbotRolePreset(
        id = "creative_director",
        title = "Creative Director",
        badge = "CREATIVE",
        recommendedModelId = "gemini-3.5-flash",
        systemInstruction = "You are an award-winning Creative Director specializing in visual storytelling, cinematic Veo 3 video prompts, photorealistic AI image prompts, and brand copywriting."
    ),
    ChatbotRolePreset(
        id = "business_strategist",
        title = "Startup Advisor",
        badge = "STRATEGY",
        recommendedModelId = "gemini-3.1-pro-preview",
        systemInstruction = "You are a seasoned Startup Founder and Product Strategist. Deliver structured go-to-market plans, unit economics analyses, pitch deck outlines, and actionable roadmaps."
    )
)

const val DEFAULT_FOUNDER_INTRO_TITLE = "Welcome to Kallesh AI Hub"
const val DEFAULT_FOUNDER_INTRO_SCRIPT = """Welcome to Kallesh AI Hub.

I’m Kallesh MC, the founder of Kallesh AI Hub.

Kallesh AI Hub is created with a simple vision — to bring powerful AI tools together in one place and make them easier for everyone to use.

Here, you can chat with AI, ask questions, understand files, work with images, generate creative content, and explore new AI-powered tools.

This is more than just a chatbot. It is a growing AI hub built to help you learn, create, and turn your ideas into reality.

Welcome to Kallesh AI Hub.

Let’s create something amazing."""

data class UserProfile(
    val userId: String = "",
    val displayName: String = "Explorer",
    val email: String = "",
    val avatarUrl: String? = null,
    val plan: String = "FREE",
    val hasSeenFounderIntro: Boolean = false,
    val memoryEnabled: Boolean = true,
    val memorySummary: String = "",
    val preferredLanguage: String = "English",
    val responseStyle: String = "Professional",
    val defaultModel: String = "gemini-3.5-flash",
    val themeMode: String = "DARK",
    val voiceEnabled: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val subscriptionPlan: SubscriptionPlan
        get() = SubscriptionPlan.fromId(plan)

    val isAdminUser: Boolean
        get() = email.equals("mckallesh14@gmail.com", ignoreCase = true)

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.take(100).ifBlank { "Explorer" },
        "email" to email.take(200),
        "avatarUrl" to avatarUrl?.take(1000),
        "plan" to plan,
        "hasSeenFounderIntro" to hasSeenFounderIntro,
        "founder_intro_seen" to hasSeenFounderIntro,
        "memoryEnabled" to memoryEnabled,
        "memorySummary" to memorySummary.take(4000),
        "preferredLanguage" to preferredLanguage.take(50),
        "responseStyle" to responseStyle.take(50),
        "defaultModel" to defaultModel.take(100),
        "themeMode" to themeMode,
        "voiceEnabled" to voiceEnabled,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }.mapValues { it.value!! }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): UserProfile? {
            if (!doc.exists()) return null
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return UserProfile(
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "Explorer",
                email = doc.getString("email") ?: "",
                avatarUrl = doc.getString("avatarUrl"),
                plan = doc.getString("plan") ?: "DAILY",
                hasSeenFounderIntro = doc.getBoolean("founder_intro_seen")
                    ?: doc.getBoolean("hasSeenFounderIntro")
                    ?: false,
                memoryEnabled = doc.getBoolean("memoryEnabled") ?: true,
                memorySummary = doc.getString("memorySummary") ?: "",
                preferredLanguage = doc.getString("preferredLanguage") ?: "English",
                responseStyle = doc.getString("responseStyle") ?: "Professional",
                defaultModel = doc.getString("defaultModel") ?: "gemini-3.5-flash",
                themeMode = doc.getString("themeMode") ?: "DARK",
                voiceEnabled = doc.getBoolean("voiceEnabled") ?: true,
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class Conversation(
    val id: String = "",
    val userId: String = "",
    val title: String = "New Conversation",
    val modelId: String = "gemini-3.5-flash",
    val folder: String = "General",
    val isPinned: Boolean = false,
    val isShared: Boolean = false,
    val shareId: String = "",
    val lastMessagePreview: String = "Start a conversation...",
    val messageCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200).ifBlank { "New Conversation" },
        "modelId" to modelId.take(100),
        "folder" to folder.take(80),
        "isPinned" to isPinned,
        "isShared" to isShared,
        "shareId" to shareId.take(128),
        "lastMessagePreview" to lastMessagePreview.take(500),
        "messageCount" to messageCount,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Conversation {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return Conversation(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: "Conversation",
                modelId = doc.getString("modelId") ?: "gemini-3.5-flash",
                folder = doc.getString("folder") ?: "General",
                isPinned = doc.getBoolean("isPinned") ?: false,
                isShared = doc.getBoolean("isShared") ?: false,
                shareId = doc.getString("shareId") ?: "",
                lastMessagePreview = doc.getString("lastMessagePreview") ?: "",
                messageCount = (doc.getLong("messageCount") ?: 0L).toInt(),
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val role: String = "user",
    val content: String = "",
    val modelId: String = "gemini-3.5-flash",
    val feedback: String = "NONE",
    val citations: List<String> = emptyList(),
    val attachmentName: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "conversationId" to conversationId,
        "role" to if (role == "model") "model" else "user",
        "content" to content.take(50000).ifBlank { "..." },
        "modelId" to modelId.take(100),
        "feedback" to if (feedback in listOf("NONE", "LIKE", "DISLIKE")) feedback else "NONE",
        "citations" to citations.take(10).map { it.take(500) },
        "attachmentName" to attachmentName.take(255),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ChatMessage {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            val rawCitations = (doc.get("citations") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return ChatMessage(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                conversationId = doc.getString("conversationId") ?: "",
                role = doc.getString("role") ?: "user",
                content = doc.getString("content") ?: "",
                modelId = doc.getString("modelId") ?: "gemini-3.5-flash",
                feedback = doc.getString("feedback") ?: "NONE",
                citations = rawCitations,
                attachmentName = doc.getString("attachmentName") ?: "",
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class DailyUsage(
    val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val chatCount: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val fileCount: Int = 0,
    val voiceCount: Int = 0,
    val musicCount: Int = 0,
    val monthlyPriorOffsets: Map<UsageCategory, Int> = emptyMap(),
    val yearlyPriorOffsets: Map<UsageCategory, Int> = emptyMap(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun countFor(category: UsageCategory): Int = when (category) {
        UsageCategory.CHAT -> chatCount
        UsageCategory.IMAGE -> imageCount
        UsageCategory.VIDEO -> videoCount
        UsageCategory.FILE -> fileCount
        UsageCategory.VOICE -> voiceCount
        UsageCategory.MUSIC -> musicCount
    }

    fun countForPeriod(category: UsageCategory, period: UsagePeriod): Int = when (period) {
        UsagePeriod.DAILY -> countFor(category)
        UsagePeriod.MONTHLY -> countFor(category) + (monthlyPriorOffsets[category] ?: 0)
        UsagePeriod.YEARLY -> countFor(category) + (yearlyPriorOffsets[category] ?: 0)
    }

    fun usedForCategory(category: UsageCategory, period: UsagePeriod): Int =
        countForPeriod(category, period)

    fun totalLimitedGenerationsUsed(period: UsagePeriod = UsagePeriod.DAILY): Int {
        return countForPeriod(UsageCategory.IMAGE, period) +
            countForPeriod(UsageCategory.VIDEO, period) +
            countForPeriod(UsageCategory.FILE, period) +
            countForPeriod(UsageCategory.VOICE, period) +
            countForPeriod(UsageCategory.MUSIC, period)
    }

    fun limitFor(category: UsageCategory, plan: SubscriptionPlan): Int =
        plan.dailyLimitFor(category)

    fun limitForPeriod(category: UsageCategory, plan: SubscriptionPlan, period: UsagePeriod): Int =
        plan.limitForPeriod(category, period)

    fun withUpdatedCategory(category: UsageCategory, count: Int): DailyUsage = when (category) {
        UsageCategory.CHAT -> copy(chatCount = count)
        UsageCategory.IMAGE -> copy(imageCount = count)
        UsageCategory.VIDEO -> copy(videoCount = count)
        UsageCategory.FILE -> copy(fileCount = count)
        UsageCategory.VOICE -> copy(voiceCount = count)
        UsageCategory.MUSIC -> copy(musicCount = count)
    }

    fun resetAll(): DailyUsage = copy(
        chatCount = 0,
        imageCount = 0,
        videoCount = 0,
        fileCount = 0,
        voiceCount = 0,
        musicCount = 0
    )

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "dateKey" to dateKey.take(10),
        "chatCount" to chatCount,
        "imageCount" to imageCount,
        "videoCount" to videoCount,
        "fileCount" to fileCount,
        "voiceCount" to voiceCount,
        "musicCount" to musicCount,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot, fallbackUserId: String, fallbackDateKey: String): DailyUsage {
            if (!doc.exists()) {
                return DailyUsage(
                    id = "${fallbackUserId}_${fallbackDateKey}",
                    userId = fallbackUserId,
                    dateKey = fallbackDateKey
                )
            }
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return DailyUsage(
                id = doc.id,
                userId = doc.getString("userId") ?: fallbackUserId,
                dateKey = doc.getString("dateKey") ?: fallbackDateKey,
                chatCount = (doc.getLong("chatCount") ?: 0L).toInt(),
                imageCount = (doc.getLong("imageCount") ?: 0L).toInt(),
                videoCount = (doc.getLong("videoCount") ?: 0L).toInt(),
                fileCount = (doc.getLong("fileCount") ?: 0L).toInt(),
                voiceCount = (doc.getLong("voiceCount") ?: 0L).toInt(),
                musicCount = (doc.getLong("musicCount") ?: 0L).toInt(),
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class WorkspaceItem(
    val id: String = "",
    val userId: String = "",
    val type: String = "PROMPT",
    val title: String = "",
    val prompt: String = "",
    val content: String = "",
    val mediaData: String = "",
    val aspectRatio: String = "1:1",
    val status: String = "COMPLETED",
    val category: String = "General",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "type" to type,
        "title" to title.take(200).ifBlank { "Untitled Artifact" },
        "prompt" to prompt.take(10000),
        "content" to content.take(60000),
        "mediaData" to mediaData.take(200000),
        "aspectRatio" to aspectRatio.take(20),
        "status" to status,
        "category" to category.take(80),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): WorkspaceItem {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return WorkspaceItem(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                type = doc.getString("type") ?: "PROMPT",
                title = doc.getString("title") ?: "Artifact",
                prompt = doc.getString("prompt") ?: "",
                content = doc.getString("content") ?: "",
                mediaData = doc.getString("mediaData") ?: "",
                aspectRatio = doc.getString("aspectRatio") ?: "1:1",
                status = doc.getString("status") ?: "COMPLETED",
                category = doc.getString("category") ?: "General",
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO",
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200).ifBlank { "Notification" },
        "message" to message.take(2000).ifBlank { "..." },
        "type" to type.take(50).ifBlank { "INFO" },
        "isRead" to isRead,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): AppNotification {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return AppNotification(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: "",
                message = doc.getString("message") ?: "",
                type = doc.getString("type") ?: "INFO",
                isRead = doc.getBoolean("isRead") ?: false,
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class SystemConfig(
    val id: String = "founder_intro",
    val userId: String = "",
    val title: String = DEFAULT_FOUNDER_INTRO_TITLE,
    val description: String = DEFAULT_FOUNDER_INTRO_SCRIPT,
    val voiceName: String = "Puck",
    val language: String = "en",
    val audioEnabled: Boolean = true,
    val showOnFirstLogin: Boolean = true,
    val allowReplay: Boolean = true,
    val announcement: String = "Welcome to Kallesh AI Hub — One AI Hub. Limitless Possibilities.",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200).ifBlank { DEFAULT_FOUNDER_INTRO_TITLE },
        "description" to description.take(10000).ifBlank { DEFAULT_FOUNDER_INTRO_SCRIPT },
        "voiceName" to voiceName.take(60).ifBlank { "Puck" },
        "language" to language.take(20).ifBlank { "en" },
        "audioEnabled" to audioEnabled,
        "showOnFirstLogin" to showOnFirstLogin,
        "allowReplay" to allowReplay,
        "announcement" to announcement.take(1000),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): SystemConfig {
            if (!doc.exists()) return SystemConfig()
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return SystemConfig(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: DEFAULT_FOUNDER_INTRO_TITLE,
                description = doc.getString("description") ?: DEFAULT_FOUNDER_INTRO_SCRIPT,
                voiceName = doc.getString("voiceName") ?: "Puck",
                language = doc.getString("language") ?: "en",
                audioEnabled = doc.getBoolean("audioEnabled") ?: true,
                showOnFirstLogin = doc.getBoolean("showOnFirstLogin") ?: true,
                allowReplay = doc.getBoolean("allowReplay") ?: true,
                announcement = doc.getString("announcement") ?: "",
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

const val SEO_WEBSITE_TITLE = "Kallesh AI Hub – AI Chat, Image & Video Generator"
const val SEO_META_DESCRIPTION = "Kallesh AI Hub is an AI platform for AI chat, image generation, video generation and other AI-powered tools."
const val PRODUCTION_CUSTOM_DOMAIN = "https://kalleshaihub.com"

enum class PublicPortalPage(
    val navLabel: String,
    val routePath: String,
    val seoTitle: String,
    val metaDescription: String,
    val h1Heading: String
) {
    HOME(
        navLabel = "Home",
        routePath = "/",
        seoTitle = SEO_WEBSITE_TITLE,
        metaDescription = SEO_META_DESCRIPTION,
        h1Heading = "Kallesh AI Hub – AI Chat, Image & Video Generator"
    ),
    AI_CHAT(
        navLabel = "AI Chat",
        routePath = "/chat",
        seoTitle = "AI Chat – $SEO_WEBSITE_TITLE",
        metaDescription = "Multi-model conversational AI chat with Gemini 3 Flash & Pro, live Google Search, Maps grounding, and persistent chat history.",
        h1Heading = "AI Chat & Multi-Model Assistant"
    ),
    AI_IMAGE_GENERATOR(
        navLabel = "AI Image Generator",
        routePath = "/image-generator",
        seoTitle = "AI Image Generator – $SEO_WEBSITE_TITLE",
        metaDescription = "Generate photorealistic images, digital art, logos, and natural-language photo edits in 1:1, 16:9, and 9:16.",
        h1Heading = "AI Image Generator & Photo Studio"
    ),
    AI_VIDEO_GENERATOR(
        navLabel = "AI Video Generator",
        routePath = "/video-generator",
        seoTitle = "AI Video Generator – $SEO_WEBSITE_TITLE",
        metaDescription = "Create cinematic 16:9 landscape and 9:16 vertical videos from text prompts or reference images with Veo 3.",
        h1Heading = "Veo 3 AI Video Generator"
    ),
    FEATURES(
        navLabel = "Features",
        routePath = "/features",
        seoTitle = "Features & AI Tools – $SEO_WEBSITE_TITLE",
        metaDescription = "Explore 28+ integrated AI capabilities across Chat, Image Generation, Video Generation, Music, Voice, Code, and Documents.",
        h1Heading = "All-in-One AI Platform Features"
    ),
    PRICING(
        navLabel = "Pricing",
        routePath = "/pricing",
        seoTitle = "Pricing & Plans – $SEO_WEBSITE_TITLE",
        metaDescription = "Transparent Daily (₹5/day), Monthly (₹200/month), and Yearly (₹1,300/year) plans on Kallesh AI Hub with unlimited AI Chat and backend-verified generation quotas.",
        h1Heading = "Plans, Pricing & Daily / Monthly / Yearly Quotas"
    ),
    ABOUT(
        navLabel = "About",
        routePath = "/about",
        seoTitle = "About – $SEO_WEBSITE_TITLE",
        metaDescription = "Founded by Kallesh MC, Kallesh AI Hub brings AI chat, image generation, video generation, and 28+ AI tools into one unified platform.",
        h1Heading = "About Kallesh AI Hub"
    ),
    CONTACT(
        navLabel = "Contact",
        routePath = "/contact",
        seoTitle = "Contact Us – $SEO_WEBSITE_TITLE",
        metaDescription = "Contact Kallesh AI Hub support and enterprise sales for custom integrations, billing, and feedback.",
        h1Heading = "Contact Kallesh AI Hub"
    ),
    PRIVACY_POLICY(
        navLabel = "Privacy Policy",
        routePath = "/privacy-policy",
        seoTitle = "Privacy Policy – $SEO_WEBSITE_TITLE",
        metaDescription = "Learn how Kallesh AI Hub protects your account data, encrypted chat history, generated media, and privacy.",
        h1Heading = "Privacy Policy"
    ),
    TERMS_OF_SERVICE(
        navLabel = "Terms of Service",
        routePath = "/terms-of-service",
        seoTitle = "Terms of Service – $SEO_WEBSITE_TITLE",
        metaDescription = "Read the Terms of Service governing your use of Kallesh AI Hub's AI chat, image, video, and workspace tools.",
        h1Heading = "Terms of Service"
    );

    val canonicalUrl: String
        get() = if (routePath == "/") "$PRODUCTION_CUSTOM_DOMAIN/" else "$PRODUCTION_CUSTOM_DOMAIN$routePath"
}

data class ApiEndpointSpec(
    val method: String,
    val path: String,
    val upstreamModelOrService: String,
    val description: String,
    val authRequired: Boolean = true
)

val PRODUCTION_API_ENDPOINTS = listOf(
    ApiEndpointSpec(
        method = "POST",
        path = "/api/v1/auth/session",
        upstreamModelOrService = "Firebase Authentication + Google Identity CredentialManager",
        description = "Secure user signup, login, logout, session token verification, and password/credential security"
    ),
    ApiEndpointSpec(
        method = "GET / PUT",
        path = "/api/v1/users/profile",
        upstreamModelOrService = "Cloud Firestore (users/{uid}) + Local Room DB",
        description = "User account & profile management, AI memory, preferred language, response tone, and theme settings"
    ),
    ApiEndpointSpec(
        method = "POST",
        path = "/api/v1/chat/completions",
        upstreamModelOrService = "gemini-3-flash-preview / gemini-3.1-pro-preview",
        description = "Multi-turn AI chat with Google Search & Maps grounding, streaming replies, and retry support"
    ),
    ApiEndpointSpec(
        method = "GET / POST / PUT / DELETE",
        path = "/api/v1/conversations",
        upstreamModelOrService = "Cloud Firestore (conversations/{id}/messages) + Room DB",
        description = "Persistent chat history, new chat creation, rename chat, folder organization, share, and delete chat"
    ),
    ApiEndpointSpec(
        method = "POST",
        path = "/api/v1/images/generate",
        upstreamModelOrService = "gemini-3.1-flash-image-preview / gemini-2.5-flash-image",
        description = "Text-to-image generation and reference photo editing in 1:1, 16:9, 9:16, 4:3, 3:4"
    ),
    ApiEndpointSpec(
        method = "POST",
        path = "/api/v1/videos/generate",
        upstreamModelOrService = "veo-3.1-fast-generate-preview (:predictLongRunning)",
        description = "Cinematic text-to-video and image-to-video generation with operation polling"
    ),
    ApiEndpointSpec(
        method = "GET / POST",
        path = "/api/v1/usage/quotas",
        upstreamModelOrService = "Cloud Firestore (usage/{uid}_{dateKey}) + Local Room DB",
        description = "Backend-validated daily, monthly, and yearly usage tracking and quota enforcement"
    ),
    ApiEndpointSpec(
        method = "GET / POST",
        path = "/api/v1/subscriptions/plan",
        upstreamModelOrService = "Cloud Firestore (users/{uid}.plan) + Room Subscription Cache",
        description = "Starter Free, Kallesh Pro, and Kallesh Enterprise plan activation with monthly/yearly billing cycles"
    ),
    ApiEndpointSpec(
        method = "POST",
        path = "/api/v1/audio/music-and-speech",
        upstreamModelOrService = "lyria-3-clip-preview / lyria-3-pro-preview / gemini-2.5-flash-preview-tts",
        description = "AI music studio synthesis, neural Text-to-Speech, and Live Voice conversation"
    ),
    ApiEndpointSpec(
        method = "POST",
        path = "/api/v1/telemetry/errors",
        upstreamModelOrService = "Structured Firestore Error Handler & Audit Logger",
        description = "Sanitized error reporting, retry recovery, and security audit logging without exposing secrets"
    )
)
