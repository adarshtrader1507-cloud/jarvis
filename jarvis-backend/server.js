// JARVIS secure backend — Phase 4.
// The Android app NEVER talks to Anthropic/OpenAI/etc directly and never contains a
// provider key. It only ever calls THIS server, which holds the real secret.

import express from "express";
import rateLimit from "express-rate-limit";
import cors from "cors";
import dotenv from "dotenv";
import Anthropic from "@anthropic-ai/sdk";

dotenv.config();

const PORT = process.env.PORT || 8080;
const APP_SHARED_SECRET = process.env.APP_SHARED_SECRET;
const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY;

if (!ANTHROPIC_API_KEY) {
  console.error("Missing ANTHROPIC_API_KEY — set it in your environment / .env file.");
  process.exit(1);
}
if (!APP_SHARED_SECRET) {
  console.error("Missing APP_SHARED_SECRET — set it in your environment / .env file.");
  process.exit(1);
}

const anthropic = new Anthropic({ apiKey: ANTHROPIC_API_KEY });
const app = express();

app.use(express.json({ limit: "1mb" }));
app.use(cors()); // mobile app doesn't need this, but harmless and useful if you add a web client

// Basic abuse protection — a real deployment behind many users should move to per-user
// auth + per-user rate limits (see .env.example note), but this is the honest minimum.
const limiter = rateLimit({ windowMs: 60 * 1000, max: 20 });
app.use("/chat", limiter);

function requireAppAuth(req, res, next) {
  const header = req.get("Authorization") || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;
  if (token !== APP_SHARED_SECRET) {
    return res.status(401).json({ error: "Unauthorized" });
  }
  next();
}

const LANGUAGE_INSTRUCTIONS = {
  en: "Respond in clear English.",
  hi: "हिंदी में उत्तर दें (respond in Hindi).",
  hinglish: "Respond in natural Hinglish (mixed Hindi-English), matching how the user wrote.",
};

app.get("/health", (_req, res) => res.json({ status: "ok" }));

app.post("/chat", requireAppAuth, async (req, res) => {
  const { message, history, language, memories } = req.body || {};

  if (typeof message !== "string" || !message.trim()) {
    return res.status(400).json({ error: "message is required" });
  }

  const languageInstruction = LANGUAGE_INSTRUCTIONS[language] || LANGUAGE_INSTRUCTIONS.en;
  const historyText = Array.isArray(history) ? history.slice(-10).join("\n") : "";
  const memoryText = Array.isArray(memories) && memories.length
    ? `\nThings the user has asked you to remember about them:\n${memories.slice(0, 20).join("\n")}`
    : "";

  try {
    const response = await anthropic.messages.create({
      model: "claude-sonnet-4-5",
      max_tokens: 1024,
      system: `You are JARVIS, a helpful personal AI assistant running inside an Android app. ${languageInstruction} Keep answers conversational and reasonably concise since they may be read aloud by text-to-speech.${memoryText}`,
      messages: [
        {
          role: "user",
          content: historyText ? `Conversation so far:\n${historyText}\n\nUser: ${message}` : message,
        },
      ],
    });

    const reply = response.content
      .filter((block) => block.type === "text")
      .map((block) => block.text)
      .join("\n");

    res.json({ reply: reply || "I didn't generate a response — please try again." });
  } catch (err) {
    console.error("Anthropic API error:", err?.message || err);
    res.status(502).json({ error: "AI service unavailable" });
  }
});

app.listen(PORT, () => console.log(`JARVIS backend listening on port ${PORT}`));
