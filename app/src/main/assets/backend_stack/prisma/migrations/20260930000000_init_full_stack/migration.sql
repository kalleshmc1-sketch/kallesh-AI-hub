-- Kallesh AI Hub — PostgreSQL Initial Migration (v1 -> Full-Stack Production Schema)
-- Creates all 10 relational tables with foreign keys, indexes, and cascading deletes.

CREATE TABLE "User" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "passwordSalt" TEXT NOT NULL,
    "displayName" TEXT NOT NULL,
    "avatarUrl" TEXT,
    "plan" TEXT NOT NULL DEFAULT 'FREE',
    "role" TEXT NOT NULL DEFAULT 'USER',
    "hasSeenFounderIntro" BOOLEAN NOT NULL DEFAULT false,
    "passwordResetToken" TEXT,
    "passwordResetExpires" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "User_email_key" ON "User"("email");
CREATE INDEX "User_email_idx" ON "User"("email");

CREATE TABLE "Session" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "tokenHash" TEXT NOT NULL,
    "userAgent" TEXT,
    "ipAddress" TEXT,
    "expiresAt" TIMESTAMP(3) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "Session_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "Session_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX "Session_tokenHash_key" ON "Session"("tokenHash");
CREATE INDEX "Session_userId_idx" ON "Session"("userId");

CREATE TABLE "Conversation" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "title" TEXT NOT NULL,
    "modelId" TEXT NOT NULL DEFAULT 'gemini-3-flash-preview',
    "folder" TEXT NOT NULL DEFAULT 'General',
    "isPinned" BOOLEAN NOT NULL DEFAULT false,
    "isShared" BOOLEAN NOT NULL DEFAULT false,
    "shareId" TEXT,
    "lastMessagePreview" TEXT NOT NULL DEFAULT '',
    "messageCount" INTEGER NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "Conversation_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "Conversation_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX "Conversation_shareId_key" ON "Conversation"("shareId");
CREATE INDEX "Conversation_userId_updatedAt_idx" ON "Conversation"("userId", "updatedAt");

CREATE TABLE "Message" (
    "id" TEXT NOT NULL,
    "conversationId" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "role" TEXT NOT NULL,
    "content" TEXT NOT NULL,
    "modelId" TEXT NOT NULL,
    "feedback" TEXT NOT NULL DEFAULT 'NONE',
    "citationsJson" TEXT NOT NULL DEFAULT '[]',
    "attachmentName" TEXT NOT NULL DEFAULT '',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "Message_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "Message_conversationId_fkey" FOREIGN KEY ("conversationId") REFERENCES "Conversation"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "Message_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX "Message_conversationId_createdAt_idx" ON "Message"("conversationId", "createdAt");
CREATE INDEX "Message_userId_idx" ON "Message"("userId");

CREATE TABLE "AiToolUsage" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "toolId" TEXT NOT NULL,
    "toolName" TEXT NOT NULL,
    "category" TEXT NOT NULL,
    "providerUsed" TEXT NOT NULL,
    "modelUsed" TEXT NOT NULL,
    "promptInput" TEXT NOT NULL,
    "resultOutput" TEXT NOT NULL,
    "mediaPathOrUrl" TEXT NOT NULL DEFAULT '',
    "status" TEXT NOT NULL DEFAULT 'COMPLETED',
    "responseTimeMs" INTEGER NOT NULL DEFAULT 0,
    "estimatedTokens" INTEGER NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "AiToolUsage_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "AiToolUsage_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX "AiToolUsage_userId_toolId_createdAt_idx" ON "AiToolUsage"("userId", "toolId", "createdAt");

CREATE TABLE "UploadedFile" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "fileName" TEXT NOT NULL,
    "mimeType" TEXT NOT NULL,
    "sizeBytes" INTEGER NOT NULL,
    "storagePath" TEXT NOT NULL,
    "extractedText" TEXT NOT NULL DEFAULT '',
    "checksumSha256" TEXT NOT NULL DEFAULT '',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "UploadedFile_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "UploadedFile_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX "UploadedFile_userId_createdAt_idx" ON "UploadedFile"("userId", "createdAt");

CREATE TABLE "UserSetting" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "preferredLanguage" TEXT NOT NULL DEFAULT 'English',
    "responseStyle" TEXT NOT NULL DEFAULT 'Professional',
    "defaultModel" TEXT NOT NULL DEFAULT 'gemini-3-flash-preview',
    "themeMode" TEXT NOT NULL DEFAULT 'DARK',
    "voiceEnabled" BOOLEAN NOT NULL DEFAULT true,
    "memoryEnabled" BOOLEAN NOT NULL DEFAULT true,
    "memorySummary" TEXT NOT NULL DEFAULT '',
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "UserSetting_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "UserSetting_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX "UserSetting_userId_key" ON "UserSetting"("userId");

CREATE TABLE "SavedResult" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "toolId" TEXT NOT NULL,
    "type" TEXT NOT NULL,
    "title" TEXT NOT NULL,
    "prompt" TEXT NOT NULL,
    "content" TEXT NOT NULL,
    "mediaData" TEXT NOT NULL DEFAULT '',
    "aspectRatio" TEXT NOT NULL DEFAULT '1:1',
    "category" TEXT NOT NULL DEFAULT 'General',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "SavedResult_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "SavedResult_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX "SavedResult_userId_createdAt_idx" ON "SavedResult"("userId", "createdAt");

CREATE TABLE "ApiUsage" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "endpoint" TEXT NOT NULL,
    "httpMethod" TEXT NOT NULL,
    "provider" TEXT NOT NULL,
    "modelId" TEXT NOT NULL,
    "statusCode" INTEGER NOT NULL,
    "latencyMs" INTEGER NOT NULL,
    "tokensUsed" INTEGER NOT NULL DEFAULT 0,
    "usedFallback" BOOLEAN NOT NULL DEFAULT false,
    "errorMessage" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "ApiUsage_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "ApiUsage_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX "ApiUsage_userId_createdAt_idx" ON "ApiUsage"("userId", "createdAt");
CREATE INDEX "ApiUsage_endpoint_idx" ON "ApiUsage"("endpoint");

CREATE TABLE "ToolConfiguration" (
    "toolId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "category" TEXT NOT NULL,
    "version" TEXT NOT NULL DEFAULT '2.4.0',
    "status" TEXT NOT NULL DEFAULT 'ACTIVE',
    "primaryProvider" TEXT NOT NULL DEFAULT 'GOOGLE_GEMINI',
    "primaryModelId" TEXT NOT NULL DEFAULT 'gemini-3-flash-preview',
    "fallbackProvider" TEXT NOT NULL DEFAULT 'OPENAI_COMPATIBLE',
    "fallbackModelId" TEXT NOT NULL DEFAULT 'gemini-2.5-flash',
    "systemInstruction" TEXT NOT NULL,
    "dailyLimitFree" INTEGER NOT NULL DEFAULT 20,
    "dailyLimitPro" INTEGER NOT NULL DEFAULT 100,
    "dailyLimitPremium" INTEGER NOT NULL DEFAULT 500,
    "rolloutPercentage" INTEGER NOT NULL DEFAULT 100,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    CONSTRAINT "ToolConfiguration_pkey" PRIMARY KEY ("toolId")
);
