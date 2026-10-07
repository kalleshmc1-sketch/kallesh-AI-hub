/**
 * Kallesh AI Hub — Production Full-Stack Backend Server (Node.js / Express / TypeScript / Prisma / PostgreSQL)
 * Implements:
 * - Structured API Responses: { "success": true, "data": {} } and { "success": false, "error": "Readable message" }
 * - Authentication & Authorization (/api/auth/register, /api/auth/login, /api/auth/logout, /api/auth/me)
 * - AI Provider Abstraction (Google Gemini, OpenAI, Resilient Engine)
 * - AI Chat & Conversation Persistence (/api/chat, /api/chat/history, /api/chat/:id)
 * - All 28 AI Tools Endpoints (/api/tools/:toolId, /api/tools/:toolId/history)
 * - File Upload & Processing (/api/files/upload, /api/files/:id)
 * - User Profile, Settings & Usage Monitoring (/api/user/profile, /api/user/settings, /api/usage)
 */

import express, { Request, Response, NextFunction } from "express";
import cors from "cors";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import crypto from "crypto";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();
const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "25mb" }));

const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 60,
  standardHeaders: true,
  legacyHeaders: false,
  handler: (_req, res) => {
    res.status(429).json({
      success: false,
      error: "Rate limit exceeded. Please wait a moment before sending additional requests."
    });
  }
});

app.use("/api/", apiLimiter);

const AUTH_SECRET = process.env.AUTH_SECRET || "change_this_secret_in_production";
const GOOGLE_AI_API_KEY = process.env.GOOGLE_AI_API_KEY || process.env.GEMINI_API_KEY || process.env.AI_API_KEY || "";
const OPENAI_API_KEY = process.env.OPENAI_API_KEY || "";

// Standardized Response Helpers
function sendSuccess<T>(res: Response, data: T, statusCode = 200) {
  return res.status(statusCode).json({
    success: true,
    data
  });
}

function sendError(res: Response, error: string, statusCode = 400) {
  return res.status(statusCode).json({
    success: false,
    error
  });
}

// AIProvider Abstraction
interface AIProviderResult {
  text: string;
  provider: string;
  modelUsed: string;
  citations: string[];
  estimatedTokens: number;
}

interface AIProvider {
  name: string;
  isConfigured(): boolean;
  complete(params: {
    modelId: string;
    prompt: string;
    systemInstruction: string;
    enableSearch?: boolean;
  }): Promise<AIProviderResult>;
}

class GoogleGeminiProvider implements AIProvider {
  name = "GOOGLE_GEMINI";
  isConfigured(): boolean {
    return Boolean(GOOGLE_AI_API_KEY && !GOOGLE_AI_API_KEY.startsWith("MY_"));
  }
  async complete(params: {
    modelId: string;
    prompt: string;
    systemInstruction: string;
    enableSearch?: boolean;
  }): Promise<AIProviderResult> {
    const model = params.modelId || "gemini-3-flash-preview";
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${GOOGLE_AI_API_KEY}`;
    const body: Record<string, unknown> = {
      systemInstruction: { parts: [{ text: params.systemInstruction }] },
      contents: [{ role: "user", parts: [{ text: params.prompt }] }]
    };
    if (params.enableSearch) {
      body.tools = [{ googleSearch: {} }];
    }
    const resp = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });
    if (!resp.ok) {
      throw new Error(`Gemini API HTTP ${resp.status}`);
    }
    const json: any = await resp.json();
    const candidate = json?.candidates?.[0];
    const text = candidate?.content?.parts?.map((p: any) => p.text || "").join("") || "";
    const citations: string[] =
      candidate?.groundingMetadata?.groundingChunks
        ?.map((c: any) => c?.web?.uri)
        ?.filter(Boolean) || [];
    return {
      text,
      provider: this.name,
      modelUsed: model,
      citations,
      estimatedTokens: Math.max(32, Math.round((params.prompt.length + text.length) / 4))
    };
  }
}

class OpenAIProvider implements AIProvider {
  name = "OPENAI";
  isConfigured(): boolean {
    return Boolean(OPENAI_API_KEY && !OPENAI_API_KEY.startsWith("MY_"));
  }
  async complete(params: {
    modelId: string;
    prompt: string;
    systemInstruction: string;
  }): Promise<AIProviderResult> {
    const resp = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${OPENAI_API_KEY}`
      },
      body: JSON.stringify({
        model: "gpt-4o-mini",
        messages: [
          { role: "system", content: params.systemInstruction },
          { role: "user", content: params.prompt }
        ]
      })
    });
    if (!resp.ok) {
      throw new Error(`OpenAI API HTTP ${resp.status}`);
    }
    const json: any = await resp.json();
    const text = json?.choices?.[0]?.message?.content || "";
    return {
      text,
      provider: this.name,
      modelUsed: "gpt-4o-mini",
      citations: [],
      estimatedTokens: Math.max(32, Math.round((params.prompt.length + text.length) / 4))
    };
  }
}

const geminiProvider = new GoogleGeminiProvider();
const openAiProvider = new OpenAIProvider();

async function executeWithAiProvider(params: {
  modelId: string;
  prompt: string;
  systemInstruction: string;
  enableSearch?: boolean;
}): Promise<AIProviderResult> {
  if (geminiProvider.isConfigured()) {
    return geminiProvider.complete(params);
  }
  if (openAiProvider.isConfigured()) {
    return openAiProvider.complete(params);
  }
  throw new Error("No AI provider API key is configured. Set GOOGLE_AI_API_KEY or OPENAI_API_KEY.");
}

// Authentication Middleware
interface AuthenticatedRequest extends Request {
  user?: { id: string; email: string; role: string; plan: string };
}

async function requireAuth(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  try {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
    if (!token) {
      return sendError(res, "Authentication required. Please sign in.", 401);
    }
    const decoded = jwt.verify(token, AUTH_SECRET) as { userId: string };
    const user = await prisma.user.findUnique({ where: { id: decoded.userId } });
    if (!user) {
      return sendError(res, "Invalid or expired session.", 401);
    }
    req.user = { id: user.id, email: user.email, role: user.role, plan: user.plan };
    next();
  } catch (_err) {
    return sendError(res, "Session expired or invalid token.", 401);
  }
}

// 1. AUTHENTICATION ENDPOINTS
app.post("/api/auth/register", async (req: Request, res: Response) => {
  try {
    const { email, password, displayName } = req.body || {};
    if (!email || !password || password.length < 6) {
      return sendError(res, "Valid email and password (minimum 6 characters) are required.", 422);
    }
    const normalizedEmail = String(email).trim().toLowerCase();
    const existing = await prisma.user.findUnique({ where: { email: normalizedEmail } });
    if (existing) {
      return sendError(res, "An account with this email already exists.", 409);
    }
    const salt = await bcrypt.genSalt(12);
    const passwordHash = await bcrypt.hash(password, salt);
    const user = await prisma.user.create({
      data: {
        email: normalizedEmail,
        passwordHash,
        passwordSalt: salt,
        displayName: displayName?.trim() || normalizedEmail.split("@")[0],
        settings: { create: {} }
      }
    });
    const token = jwt.sign({ userId: user.id }, AUTH_SECRET, { expiresIn: "30d" });
    const tokenHash = crypto.createHash("sha256").update(token).digest("hex");
    await prisma.session.create({
      data: {
        userId: user.id,
        tokenHash,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });
    return sendSuccess(
      res,
      {
        token,
        user: { id: user.id, email: user.email, displayName: user.displayName, plan: user.plan, role: user.role }
      },
      201
    );
  } catch (_err) {
    return sendError(res, "Registration could not be completed.", 500);
  }
});

app.post("/api/auth/login", async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body || {};
    if (!email || !password) {
      return sendError(res, "Email and password are required.", 422);
    }
    const user = await prisma.user.findUnique({ where: { email: String(email).trim().toLowerCase() } });
    if (!user || !(await bcrypt.compare(password, user.passwordHash))) {
      return sendError(res, "Invalid email or password.", 401);
    }
    const token = jwt.sign({ userId: user.id }, AUTH_SECRET, { expiresIn: "30d" });
    const tokenHash = crypto.createHash("sha256").update(token).digest("hex");
    await prisma.session.create({
      data: {
        userId: user.id,
        tokenHash,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      }
    });
    return sendSuccess(res, {
      token,
      user: { id: user.id, email: user.email, displayName: user.displayName, plan: user.plan, role: user.role }
    });
  } catch (_err) {
    return sendError(res, "Login failed.", 500);
  }
});

app.post("/api/auth/logout", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  try {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7).trim() : "";
    const tokenHash = crypto.createHash("sha256").update(token).digest("hex");
    await prisma.session.deleteMany({ where: { tokenHash } });
    return sendSuccess(res, { loggedOut: true });
  } catch (_err) {
    return sendError(res, "Logout failed.", 500);
  }
});

app.get("/api/auth/me", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  return sendSuccess(res, { user: req.user });
});

// 2. AI CHAT & CONVERSATION ENDPOINTS
app.post("/api/chat", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const start = Date.now();
  try {
    const { conversationId, prompt, modelId, systemInstruction, enableGoogleSearch } = req.body || {};
    if (!prompt || String(prompt).trim().length === 0) {
      return sendError(res, "Message prompt cannot be empty.", 422);
    }
    const userId = req.user!.id;
    let convId = conversationId;
    if (!convId) {
      const created = await prisma.conversation.create({
        data: {
          userId,
          title: String(prompt).trim().slice(0, 60),
          modelId: modelId || "gemini-3-flash-preview"
        }
      });
      convId = created.id;
    }

    await prisma.message.create({
      data: {
        conversationId: convId,
        userId,
        role: "user",
        content: String(prompt).trim(),
        modelId: modelId || "gemini-3-flash-preview"
      }
    });

    const aiRes = await executeWithAiProvider({
      modelId: modelId || "gemini-3-flash-preview",
      prompt: String(prompt).trim(),
      systemInstruction: systemInstruction || "You are Kallesh AI Hub Assistant.",
      enableSearch: Boolean(enableGoogleSearch)
    });

    const assistantMsg = await prisma.message.create({
      data: {
        conversationId: convId,
        userId,
        role: "model",
        content: aiRes.text,
        modelId: aiRes.modelUsed,
        citationsJson: JSON.stringify(aiRes.citations)
      }
    });

    await prisma.conversation.update({
      where: { id: convId },
      data: { messageCount: { increment: 2 } }
    });

    await prisma.apiUsage.create({
      data: {
        userId,
        endpoint: "/api/chat",
        httpMethod: "POST",
        provider: aiRes.provider,
        modelId: aiRes.modelUsed,
        statusCode: 200,
        latencyMs: Date.now() - start,
        tokensUsed: aiRes.estimatedTokens
      }
    });

    return sendSuccess(res, {
      conversationId: convId,
      message: assistantMsg,
      citations: aiRes.citations
    });
  } catch (err: any) {
    return sendError(res, err?.message || "Chat completion failed.", 500);
  }
});

app.get("/api/chat/history", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const conversations = await prisma.conversation.findMany({
    where: { userId: req.user!.id },
    orderBy: { updatedAt: "desc" }
  });
  return sendSuccess(res, { conversations });
});

app.get("/api/chat/:id", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const conversation = await prisma.conversation.findFirst({
    where: { id: req.params.id, userId: req.user!.id },
    include: { messages: { orderBy: { createdAt: "asc" } } }
  });
  if (!conversation) return sendError(res, "Conversation not found.", 404);
  return sendSuccess(res, { conversation });
});

app.delete("/api/chat/:id", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  await prisma.conversation.deleteMany({ where: { id: req.params.id, userId: req.user!.id } });
  return sendSuccess(res, { deleted: true });
});

// 3. 28 AI TOOLS ENDPOINTS
app.post("/api/tools/:toolId", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const start = Date.now();
  const { toolId } = req.params;
  try {
    const { prompt, parameters, enableGoogleSearch } = req.body || {};
    if (!prompt || String(prompt).trim().length === 0) {
      return sendError(res, `Input prompt is required for tool ${toolId}.`, 422);
    }
    const toolConfig = await prisma.toolConfiguration.findUnique({ where: { toolId } });
    const toolName = toolConfig?.name || toolId;
    const category = toolConfig?.category || "AI Tool";
    const modelId = toolConfig?.primaryModelId || "gemini-3-flash-preview";
    const sysInstruction =
      toolConfig?.systemInstruction || `You are ${toolName} inside Kallesh AI Hub. Deliver complete Markdown output.`;

    const aiRes = await executeWithAiProvider({
      modelId,
      prompt: parameters ? `Parameters: ${JSON.stringify(parameters)}\n\nInput:\n${prompt}` : String(prompt),
      systemInstruction: sysInstruction,
      enableSearch: Boolean(enableGoogleSearch)
    });

    const latencyMs = Date.now() - start;
    const usage = await prisma.aiToolUsage.create({
      data: {
        userId: req.user!.id,
        toolId,
        toolName,
        category,
        providerUsed: aiRes.provider,
        modelUsed: aiRes.modelUsed,
        promptInput: String(prompt),
        resultOutput: aiRes.text,
        status: "COMPLETED",
        responseTimeMs: latencyMs,
        estimatedTokens: aiRes.estimatedTokens
      }
    });

    await prisma.savedResult.create({
      data: {
        userId: req.user!.id,
        toolId,
        title: `${toolName}: ${String(prompt).slice(0, 48)}`,
        prompt: String(prompt),
        content: aiRes.text,
        category
      }
    });

    return sendSuccess(res, { usage });
  } catch (err: any) {
    return sendError(res, err?.message || `Execution failed for tool ${toolId}.`, 500);
  }
});

app.get("/api/tools/:toolId/history", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const history = await prisma.aiToolUsage.findMany({
    where: { userId: req.user!.id, toolId: req.params.toolId },
    orderBy: { createdAt: "desc" },
    take: 25
  });
  return sendSuccess(res, { history });
});

// 4. FILE UPLOAD & METADATA ENDPOINTS
app.post("/api/files/upload", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { fileName, mimeType, base64Data } = req.body || {};
    if (!fileName || !mimeType || !base64Data) {
      return sendError(res, "fileName, mimeType, and base64Data are required.", 422);
    }
    const buffer = Buffer.from(base64Data, "base64");
    if (buffer.length > 25 * 1024 * 1024) {
      return sendError(res, "File exceeds maximum 25 MB upload limit.", 413);
    }
    const checksumSha256 = crypto.createHash("sha256").update(buffer).digest("hex");
    const extractedPreview =
      mimeType.startsWith("text/") || mimeType.includes("json") || mimeType.includes("csv")
        ? buffer.toString("utf8").slice(0, 4000)
        : `Binary file (${mimeType}, ${buffer.length} bytes)`;

    const fileRecord = await prisma.uploadedFile.create({
      data: {
        userId: req.user!.id,
        fileName,
        mimeType,
        sizeBytes: buffer.length,
        extractedPreview,
        checksumSha256
      }
    });
    return sendSuccess(res, { file: fileRecord }, 201);
  } catch (_err) {
    return sendError(res, "File upload processing failed.", 500);
  }
});

app.get("/api/files/:id", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const file = await prisma.uploadedFile.findFirst({
    where: { id: req.params.id, userId: req.user!.id }
  });
  if (!file) return sendError(res, "File not found.", 404);
  return sendSuccess(res, { file });
});

// 5. USER PROFILE, SETTINGS & USAGE ENDPOINTS
app.get("/api/user/profile", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const user = await prisma.user.findUnique({
    where: { id: req.user!.id },
    include: { settings: true }
  });
  return sendSuccess(res, { profile: user });
});

app.put("/api/user/settings", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const { preferredLanguage, responseStyle, defaultModel, themeMode, voiceEnabled, memoryEnabled, memorySummary } =
    req.body || {};
  const settings = await prisma.userSettings.upsert({
    where: { userId: req.user!.id },
    update: { preferredLanguage, responseStyle, defaultModel, themeMode, voiceEnabled, memoryEnabled, memorySummary },
    create: {
      userId: req.user!.id,
      preferredLanguage: preferredLanguage || "English",
      responseStyle: responseStyle || "Professional",
      defaultModel: defaultModel || "gemini-3-flash-preview",
      themeMode: themeMode || "DARK"
    }
  });
  return sendSuccess(res, { settings });
});

app.get("/api/usage", requireAuth, async (req: AuthenticatedRequest, res: Response) => {
  const [toolRuns, apiCalls, savedCount, filesCount] = await Promise.all([
    prisma.aiToolUsage.count({ where: { userId: req.user!.id } }),
    prisma.apiUsage.count({ where: { userId: req.user!.id } }),
    prisma.savedResult.count({ where: { userId: req.user!.id } }),
    prisma.uploadedFile.count({ where: { userId: req.user!.id } })
  ]);
  return sendSuccess(res, {
    toolRuns,
    apiCalls,
    savedCount,
    filesCount,
    databaseStatus: "HEALTHY"
  });
});

export default app;
