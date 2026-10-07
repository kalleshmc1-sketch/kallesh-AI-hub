# Kallesh AI Hub — Full-Stack Production Architecture, API Reference & Deployment Guide

## 1. Architecture Overview

**Kallesh AI Hub** is a full-stack multi-provider AI platform featuring **28 functional AI tools**, real-time **Google Search grounding**, multimodal file processing, authentication, and relational database persistence:

- **Frontend Layer**:
  - Preserves the existing **Kallesh AI Hub** UI, navigation, branding, responsive layouts, Creative Studios, AI Tool Hub (all 28 modules), Personal Workspace, Pricing & Limits, and Founder Admin Center.
  - Every one of the **28 AI tools** includes tool-specific parameter controls (`ToolSpecificPresetsCatalog`), 1-tap sample inputs, file upload (`POST /api/files/upload`), live execution progress indicators, rich Markdown/code/media output rendering, 1-click Copy & Markdown file download, and persisted database execution history (`GET /api/tools/:toolId/history`).
- **Backend & AI Provider Layer (`FullStackBackendEngine` + `server.ts`)**:
  - `AIProvider` interface abstracting **Google Gemini** (`gemini-3-flash-preview`, `gemini-3.1-pro-preview`, `gemini-3.1-flash-image-preview`, `veo-3.1-fast-generate-preview`, `gemini-2.5-flash-preview-tts`), **OpenAI** (`gpt-4o-mini`), and the resilient studio engine.
  - Standardized JSON envelopes:
    - Success: `{ "success": true, "data": { ... } }`
    - Error: `{ "success": false, "error": "Readable error message" }`
- **Database Layer (PostgreSQL + Prisma ORM & On-Device Relational SQL)**:
  - 10 normalized relational tables with foreign keys and indexes:
    1. `users` (`User`)
    2. `sessions` (`Session`)
    3. `conversations` (`Conversation`)
    4. `messages` (`Message`)
    5. `ai_tool_usage` (`AiToolUsage`)
    6. `uploaded_files` (`UploadedFile`)
    7. `user_settings` (`UserSettings`)
    8. `saved_results` (`SavedResult`)
    9. `api_usage` (`ApiUsage`)
    10. `tool_configurations` (`ToolConfiguration`)

---

## 2. Environment Configuration (`.env` / `.env.example`)

Never expose private keys in client code. Configure keys via the AI Studio **Secrets** panel or server `.env`:

```env
GEMINI_API_KEY=your_google_gemini_api_key
GOOGLE_AI_API_KEY=your_google_gemini_api_key
AI_API_KEY=your_primary_ai_api_key
OPENAI_API_KEY=your_openai_api_key
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/kallesh_ai_hub?schema=public
AUTH_SECRET=your_64_char_jwt_and_session_secret
```

---

## 3. PostgreSQL + Prisma Setup & Migration Instructions

```bash
# 1. Navigate to the backend stack directory
cd app/src/main/assets/backend_stack

# 2. Install dependencies
npm install express cors helmet express-rate-limit bcryptjs jsonwebtoken @prisma/client
npm install -D prisma typescript @types/node @types/express

# 3. Generate Prisma Client & run PostgreSQL migrations
npx prisma generate
npx prisma migrate deploy
```

---

## 4. REST API Documentation

| Endpoint | Method | Auth | Description |
| :--- | :--- | :--- | :--- |
| `/api/auth/register` | `POST` | Public | Register user with salted password hash & create session |
| `/api/auth/login` | `POST` | Public | Authenticate user and issue session token |
| `/api/auth/logout` | `POST` | Bearer | Revoke active user session |
| `/api/auth/me` | `GET` | Bearer | Retrieve current authenticated user profile |
| `/api/chat` | `POST` | Bearer | Stream/complete AI chat with optional Google Search grounding |
| `/api/chat/history` | `GET` | Bearer | List user conversations ordered by `updated_at DESC` |
| `/api/chat/:id` | `GET` / `DELETE` | Bearer | Load or delete a specific conversation and its messages |
| `/api/tools/:toolId` | `POST` | Bearer | Execute any of the 28 AI tools and persist to `ai_tool_usage` |
| `/api/tools/:toolId/history` | `GET` | Bearer | Retrieve saved execution history for a specific AI tool |
| `/api/files/upload` | `POST` | Bearer | Validate MIME/size, compute SHA-256, and extract text preview |
| `/api/files/:id` | `GET` | Bearer | Retrieve uploaded file metadata and extracted preview |
| `/api/user/profile` | `GET` | Bearer | Get user profile, plan tier, and account metadata |
| `/api/user/settings` | `PUT` | Bearer | Update language, response style, default model, and AI memory |
| `/api/usage` | `GET` | Bearer | Get tool usage counts, API telemetry, and database status |

---

## 5. Complete 28 AI Tools Verification Checklist

All 28 tools are registered in `DefaultFeatureCatalog`, seeded into `tool_configurations`, and verified end-to-end (`Frontend → API → AIProvider → Database → Response → UI`):

- [x] 1. `feat_ai_chat` — AI Chat (`POST /api/tools/feat_ai_chat`)
- [x] 2. `feat_ai_search` — AI Search with Live Google Grounding (`POST /api/tools/feat_ai_search`)
- [x] 3. `feat_ai_research` — AI Deep Research (`POST /api/tools/feat_ai_research`)
- [x] 4. `feat_image_gen` — Image Generation (`POST /api/tools/feat_image_gen`)
- [x] 5. `feat_image_edit` — Image Editing (`POST /api/tools/feat_image_edit`)
- [x] 6. `feat_video_veo` — Video Generation (`POST /api/tools/feat_video_veo`)
- [x] 7. `feat_audio_music` — Audio / Music Generation (`POST /api/tools/feat_audio_music`)
- [x] 8. `feat_voice_tts` — Text to Speech (`POST /api/tools/feat_voice_tts`)
- [x] 9. `feat_speech_stt` — Speech to Text (`POST /api/tools/feat_speech_stt`)
- [x] 10. `feat_ai_vision` — AI Vision (`POST /api/tools/feat_ai_vision`)
- [x] 11. `feat_ai_ocr` — OCR / Image to Text (`POST /api/tools/feat_ai_ocr`)
- [x] 12. `feat_ai_documents` — Document AI (`POST /api/tools/feat_ai_documents`)
- [x] 13. `feat_pdf_analysis` — PDF Analysis (`POST /api/tools/feat_pdf_analysis`)
- [x] 14. `feat_file_analysis` — File Analysis (`POST /api/tools/feat_file_analysis`)
- [x] 15. `feat_ai_coding` — AI Coding Assistant (`POST /api/tools/feat_ai_coding`)
- [x] 16. `feat_code_debugging` — Code Debugging (`POST /api/tools/feat_code_debugging`)
- [x] 17. `feat_code_generation` — Code Generation (`POST /api/tools/feat_code_generation`)
- [x] 18. `feat_ai_study` — Study / Homework Assistant (`POST /api/tools/feat_ai_study`)
- [x] 19. `feat_ai_writing` — AI Writing / Content Generator (`POST /api/tools/feat_ai_writing`)
- [x] 20. `feat_ai_translation` — Translation (`POST /api/tools/feat_ai_translation`)
- [x] 21. `feat_ai_summarization` — Summarization (`POST /api/tools/feat_ai_summarization`)
- [x] 22. `feat_ai_presentation` — Presentation / Report Generator (`POST /api/tools/feat_ai_presentation`)
- [x] 23. `feat_ai_data_analysis` — Data Analysis (`POST /api/tools/feat_ai_data_analysis`)
- [x] 24. `feat_ai_web_research` — Web / URL Research (`POST /api/tools/feat_ai_web_research`)
- [x] 25. `feat_ai_agent` — AI Agent / Task Assistant (`POST /api/tools/feat_ai_agent`)
- [x] 26. `feat_ai_automation` — Automation / Workflow Tool (`POST /api/tools/feat_ai_automation`)
- [x] 27. `feat_ai_productivity` — Personal AI Productivity Assistant (`POST /api/tools/feat_ai_productivity`)
- [x] 28. `feat_saved_workspace` — Saved Workspace / Prompt Tools (`POST /api/tools/feat_saved_workspace`)
