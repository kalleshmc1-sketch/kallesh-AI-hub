package com.example.ui.platform

data class ToolUiPresetConfig(
    val parameterLabel: String,
    val parameterOptions: List<String>,
    val samplePrompts: List<String>
)

object ToolSpecificPresetsCatalog {

    fun getConfigForFeature(featureId: String): ToolUiPresetConfig {
        return when (featureId) {
            "feat_ai_chat" -> ToolUiPresetConfig(
                parameterLabel = "Conversation Mode",
                parameterOptions = listOf("Comprehensive Markdown", "Concise Executive", "Socratic Step-by-Step", "Technical Deep-Dive"),
                samplePrompts = listOf(
                    "Design a scalable microservices architecture with API gateway and PostgreSQL",
                    "Compare renewable energy storage technologies in a Markdown table",
                    "Write a complete go-to-market launch plan for a SaaS product"
                )
            )
            "feat_ai_search" -> ToolUiPresetConfig(
                parameterLabel = "Search Grounding Scope",
                parameterOptions = listOf("Live Breaking News", "Global Tech & Markets", "Scientific & Fact Check", "Official Documentation"),
                samplePrompts = listOf(
                    "What are the latest breaking announcements in artificial intelligence today?",
                    "Summarize today's major global technology and stock market headlines",
                    "What are the newest Android Jetpack Compose release updates?"
                )
            )
            "feat_ai_research" -> ToolUiPresetConfig(
                parameterLabel = "Research Framework",
                parameterOptions = listOf("Executive Literature Review", "Market & TAM Analysis", "Scientific Synthesis", "Comparative Matrix"),
                samplePrompts = listOf(
                    "Conduct a deep research synthesis on solid-state battery commercialization in 2026",
                    "Analyze enterprise adoption metrics and ROI of multi-agent AI systems",
                    "Compare quantum error correction architectures with verified citations"
                )
            )
            "feat_image_gen" -> ToolUiPresetConfig(
                parameterLabel = "aspectRatio",
                parameterOptions = listOf("1:1", "16:9", "9:16", "4:3", "3:4"),
                samplePrompts = listOf(
                    "Futuristic eco-cyberpunk skyline at sunset with glowing skybridges, 8k photorealistic",
                    "Minimalist 3D glassmorphic AI crystal emblem floating over dark obsidian surface",
                    "Cinematic portrait of an astronaut exploring a bioluminescent crystal cavern"
                )
            )
            "feat_image_edit" -> ToolUiPresetConfig(
                parameterLabel = "aspectRatio",
                parameterOptions = listOf("1:1", "16:9", "9:16", "4:3"),
                samplePrompts = listOf(
                    "Transform lighting into dramatic golden-hour studio illumination with neon rim light",
                    "Restyle into a clean isometric 3D digital illustration with vibrant studio colors",
                    "Enhance contrast, sharpen architectural details, and add cinematic atmosphere"
                )
            )
            "feat_video_veo" -> ToolUiPresetConfig(
                parameterLabel = "aspectRatio",
                parameterOptions = listOf("16:9", "9:16"),
                samplePrompts = listOf(
                    "Cinematic FPV drone flight gliding over a misty alpine lake at golden sunrise",
                    "Slow-motion macro shot of liquid gold and cyan neon ink colliding in zero gravity",
                    "Futuristic autonomous electric hypercar cruising through rain-slicked Tokyo streets at night"
                )
            )
            "feat_audio_music" -> ToolUiPresetConfig(
                parameterLabel = "duration",
                parameterOptions = listOf("30s Studio Clip", "Full Track"),
                samplePrompts = listOf(
                    "Upbeat synthwave electronic track with warm analog arpeggios and driving bass, 118 BPM",
                    "Relaxing lo-fi chillhop study beat with Rhodes piano and vinyl warmth, 84 BPM",
                    "Cinematic orchestral sci-fi score with sweeping strings and deep percussion"
                )
            )
            "feat_voice_tts" -> ToolUiPresetConfig(
                parameterLabel = "voice",
                parameterOptions = listOf("Puck", "Charon", "Kore", "Fenrir", "Aoede"),
                samplePrompts = listOf(
                    "Welcome to Kallesh AI Hub — your unified full-stack platform for 28 intelligent AI tools.",
                    "In today's executive briefing, we examine the rapid evolution of multimodal AI systems.",
                    "Let's walk through the three core pillars of clean software architecture."
                )
            )
            "feat_speech_stt" -> ToolUiPresetConfig(
                parameterLabel = "Transcript Formatting",
                parameterOptions = listOf("Clean Punctuated Text", "Speaker Diarization & Timestamps", "Meeting Action Items", "Verbatim Transcript"),
                samplePrompts = listOf(
                    "Format and punctuate this spoken meeting note: we need to ship the PostgreSQL migration by Friday and verify all 28 API endpoints.",
                    "Convert these raw voice notes into structured executive meeting minutes with owners",
                    "Clean up filler words and structure this spoken technical brainstorm into bullet points"
                )
            )
            "feat_ai_vision" -> ToolUiPresetConfig(
                parameterLabel = "Visual Inspection Mode",
                parameterOptions = listOf("Architecture & Diagram Analysis", "UI/UX Accessibility Audit", "Scene & Object Breakdown", "Chart Data Extraction"),
                samplePrompts = listOf(
                    "Inspect the system architecture layout and identify potential single points of failure",
                    "Perform a Material Design 3 UI/UX audit covering contrast, spacing, and touch targets",
                    "Analyze the visual components and explain the workflow step-by-step"
                )
            )
            "feat_ai_ocr" -> ToolUiPresetConfig(
                parameterLabel = "OCR Output Format",
                parameterOptions = listOf("Markdown Tables & Text", "Structured JSON Schema", "Receipt & Invoice Line Items", "LaTeX Math & Formulas"),
                samplePrompts = listOf(
                    "Extract all invoice line items, quantities, tax rates, and totals into a Markdown table and JSON object",
                    "Convert handwritten whiteboard engineering notes into structured Markdown documentation",
                    "Extract tabular financial figures and verify row/column sums"
                )
            )
            "feat_ai_documents" -> ToolUiPresetConfig(
                parameterLabel = "Document Intelligence Mode",
                parameterOptions = listOf("Executive Summary & Risks", "Contract & Clause Audit", "Action Items & Owners", "Structured Q&A"),
                samplePrompts = listOf(
                    "Analyze this service agreement for SLA commitments, liability caps, and termination clauses",
                    "Extract key strategic initiatives, KPIs, and quarterly milestones from this business plan",
                    "Synthesize an executive briefing and risk matrix from the provided specification"
                )
            )
            "feat_pdf_analysis" -> ToolUiPresetConfig(
                parameterLabel = "PDF Extraction Depth",
                parameterOptions = listOf("Section-by-Section Summary", "Extract All Data Tables", "Academic Methodology & Citations", "Executive Briefing"),
                samplePrompts = listOf(
                    "Parse the research paper methodology, experimental benchmarks, and key limitations",
                    "Summarize each section of the technical manual and extract configuration tables",
                    "Provide a comprehensive executive breakdown of the annual financial report"
                )
            )
            "feat_file_analysis" -> ToolUiPresetConfig(
                parameterLabel = "Data File Inspector Mode",
                parameterOptions = listOf("Schema & Anomaly Detection", "CSV/JSON Statistical Summary", "Log Error Root-Cause", "Data Cleaning Plan"),
                samplePrompts = listOf(
                    "Inspect this JSON/CSV dataset schema, detect null/outlier anomalies, and compute summary statistics",
                    "Analyze server access logs to identify latency spikes, HTTP 5xx patterns, and rate-limit bottlenecks",
                    "Validate the configuration payload and recommend schema normalizations"
                )
            )
            "feat_ai_coding" -> ToolUiPresetConfig(
                parameterLabel = "Target Stack",
                parameterOptions = listOf("Kotlin + Jetpack Compose", "TypeScript + Node/Prisma", "Python + FastAPI", "PostgreSQL + SQL", "React + Next.js"),
                samplePrompts = listOf(
                    "Implement a thread-safe token bucket rate limiter in Kotlin with Coroutines and unit tests",
                    "Write a complete Express + Prisma PostgreSQL repository for user sessions and tool usage tracking",
                    "Design a clean MVVM repository layer with offline-first Room caching and exponential backoff"
                )
            )
            "feat_code_debugging" -> ToolUiPresetConfig(
                parameterLabel = "Diagnostic Focus",
                parameterOptions = listOf("Stacktrace Root-Cause Fix", "Concurrency & Race Condition", "Memory Leak & Performance", "SQL / ORM Query Fix"),
                samplePrompts = listOf(
                    "Diagnose and fix ConcurrentModificationException in a multi-threaded coroutine flow collector",
                    "Resolve Prisma P2002 unique constraint violation during concurrent user session creation",
                    "Fix Jetpack Compose infinite recomposition loop caused by unstable lambda state captures"
                )
            )
            "feat_code_generation" -> ToolUiPresetConfig(
                parameterLabel = "Boilerplate Target",
                parameterOptions = listOf("REST API + Prisma CRUD", "Room Entity + DAO + Migration", "Compose M3 Screen + ViewModel", "GitHub Actions CI/CD"),
                samplePrompts = listOf(
                    "Generate a complete REST API controller for /api/tools/:toolId with input validation and error envelopes",
                    "Generate a Room Database entity, DAO, and non-destructive migration for uploaded files",
                    "Generate a responsive Material 3 dashboard screen with WindowSizeClass support"
                )
            )
            "feat_ai_study" -> ToolUiPresetConfig(
                parameterLabel = "Study Format",
                parameterOptions = listOf("Concept + Analogies + Quiz", "10 Active-Recall Flashcards", "Exam Cheat-Sheet Notes", "Step-by-Step STEM Derivation"),
                samplePrompts = listOf(
                    "Explain Transformer self-attention mechanisms with intuitive analogies, equations, and a 5-question quiz",
                    "Create an exam revision guide on ACID transactions, isolation levels, and PostgreSQL MVCC",
                    "Teach dynamic programming memoization vs tabulation with complexity proofs and practice problems"
                )
            )
            "feat_ai_writing" -> ToolUiPresetConfig(
                parameterLabel = "Tone & Format",
                parameterOptions = listOf("Executive Product Launch", "Technical Engineering Blog", "Investor Memo", "High-Conversion Landing Copy"),
                samplePrompts = listOf(
                    "Write an official product launch announcement for Kallesh AI Hub 2.4 featuring 28 full-stack AI tools",
                    "Draft a compelling technical blog post on building resilient multi-provider AI routers",
                    "Write a persuasive executive memo proposing enterprise AI workflow automation"
                )
            )
            "feat_ai_translation" -> ToolUiPresetConfig(
                parameterLabel = "Target Languages",
                parameterOptions = listOf("Kannada + Hindi + Tamil + Telugu", "Spanish + French + German", "Japanese + Korean + Mandarin", "All Major Indian & Global"),
                samplePrompts = listOf(
                    "Translate 'Welcome to Kallesh AI Hub — Empowering creators and engineers with 28 intelligent AI tools' with phonetic guides",
                    "Translate a customer onboarding welcome email into Kannada, Hindi, and Spanish with formal register",
                    "Provide localized UI terminology for AI Chat, Image Studio, and Workspace across 5 languages"
                )
            )
            "feat_ai_summarization" -> ToolUiPresetConfig(
                parameterLabel = "Summary Style",
                parameterOptions = listOf("1-Min Executive TL;DR + Table", "Key Decisions & Action Items", "Bullet-Point Chapter Digest", "Risk & Opportunity Brief"),
                samplePrompts = listOf(
                    "Summarize a 60-minute product architecture review into a 1-sentence TL;DR, key decisions, and action owners",
                    "Condense key findings on global cloud infrastructure growth into an executive briefing table",
                    "Extract the top 5 strategic takeaways and timeline milestones from this quarterly update"
                )
            )
            "feat_ai_presentation" -> ToolUiPresetConfig(
                parameterLabel = "Deck Structure",
                parameterOptions = listOf("10-Slide Investor Pitch Deck", "Executive Strategy Presentation", "Technical Architecture Deck", "Product Launch Keynote"),
                samplePrompts = listOf(
                    "Create a 10-slide Series A investor pitch deck for Kallesh AI Hub with speaker notes and visual layouts",
                    "Generate an 8-slide enterprise technical presentation on full-stack AI security and governance",
                    "Build a product keynote slide deck showcasing 28 integrated AI studios"
                )
            )
            "feat_ai_data_analysis" -> ToolUiPresetConfig(
                parameterLabel = "Analytics Method",
                parameterOptions = listOf("KPI Growth & Cohort Analysis", "Financial Unit Economics", "Anomaly & Trend Forecasting", "SQL Metric Modeling"),
                samplePrompts = listOf(
                    "Analyze SaaS metrics: MRR $142k (+18% MoM), CAC $110, LTV $1,980, Churn 2.1% — compute payback and growth forecast",
                    "Evaluate daily AI tool usage cohorts across Free, Pro, and Enterprise tiers with optimization recommendations",
                    "Design SQL analytical queries and KPI dashboards for tracking API latency and token efficiency"
                )
            )
            "feat_ai_web_research" -> ToolUiPresetConfig(
                parameterLabel = "Intelligence Focus",
                parameterOptions = listOf("Competitor Comparison Matrix", "Live Market Landscape", "Pricing & Feature Benchmark", "Technology Radar"),
                samplePrompts = listOf(
                    "Build a live competitive comparison matrix for full-stack multi-model AI platforms in 2026",
                    "Analyze current market positioning and pricing tiers across enterprise generative AI suites",
                    "Synthesize emerging web standards and developer ecosystem trends with grounded citations"
                )
            )
            "feat_ai_agent" -> ToolUiPresetConfig(
                parameterLabel = "Agent Execution Mode",
                parameterOptions = listOf("Goal Decomposition + Full Execution", "Autonomous Technical Audit", "Multi-Phase Launch Playbook", "Research & Deliverable Synthesis"),
                samplePrompts = listOf(
                    "Decompose and execute a complete production readiness audit for a full-stack PostgreSQL + AI web & mobile platform",
                    "Autonomously plan and generate a 30-day developer community growth and content engine",
                    "Design and verify an end-to-end disaster recovery and zero-downtime migration protocol"
                )
            )
            "feat_ai_automation" -> ToolUiPresetConfig(
                parameterLabel = "Workflow Architecture",
                parameterOptions = listOf("Webhook + API Pipeline", "Cron Scheduled Job + Retry", "CI/CD Deployment Automation", "Customer Onboarding SOP"),
                samplePrompts = listOf(
                    "Build an automated webhook pipeline that ingests uploaded PDFs, runs AI extraction, and stores results in PostgreSQL",
                    "Design a daily UTC midnight quota reset and usage aggregation cron workflow with dead-letter retry",
                    "Create an automated incident response workflow when API error rate exceeds 2%"
                )
            )
            "feat_ai_productivity" -> ToolUiPresetConfig(
                parameterLabel = "Planning Framework",
                parameterOptions = listOf("Eisenhower Matrix + Time-Blocking", "Quarterly OKRs & KPIs", "2-Week Agile Sprint Plan", "Deep-Work Daily Schedule"),
                samplePrompts = listOf(
                    "Create a prioritized daily time-blocked schedule and Eisenhower Matrix for launching 5 new AI features this week",
                    "Draft measurable Q4 Engineering & Product OKRs with weekly key result checkpoints",
                    "Organize a 2-week full-stack sprint backlog covering database migrations, API security, and UI polish"
                )
            )
            else -> ToolUiPresetConfig(
                parameterLabel = "Workspace Action",
                parameterOptions = listOf("Organize & Tag Artifacts", "Synthesize Prompt Vault", "Export Full Portfolio", "Audit Saved Results"),
                samplePrompts = listOf(
                    "Create a structured prompt engineering vault organized by Coding, Research, Design, and Strategy",
                    "Synthesize key insights across my saved workspace documents into a master executive index",
                    "Generate a reusable template library for team-wide AI workflows"
                )
            )
        }
    }
}
