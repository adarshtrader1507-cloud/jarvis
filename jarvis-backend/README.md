# JARVIS backend

The only place that holds your real AI provider key. The Android app never sees it.

## Run locally
```
cp .env.example .env     # fill in ANTHROPIC_API_KEY and a random APP_SHARED_SECRET
npm install
npm start
```
Server listens on `http://localhost:8080`. Test it:
```
curl -X POST http://localhost:8080/chat \
  -H "Authorization: Bearer <APP_SHARED_SECRET>" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello","history":[],"language":"en"}'
```

## Deploy (pick one — all have free/cheap tiers)
- **Render.com**: New → Web Service → point at this folder → set env vars from `.env.example`
  → it builds the Dockerfile automatically. You'll get a URL like
  `https://jarvis-backend.onrender.com`.
- **Railway.app**: New Project → Deploy from repo → same env vars → gives a public URL.
- **Fly.io**: `fly launch` in this folder, then `fly secrets set ANTHROPIC_API_KEY=... APP_SHARED_SECRET=...`.

Whichever you pick, you'll end up with a base URL. Put that into the Android app's
**Settings → AI Provider → Backend URL** field (or bake it into
`app/build.gradle.kts`'s `BACKEND_BASE_URL` before building).

## Why a shared secret and not just leaving it open
Anyone who finds your backend URL could otherwise spend your Anthropic credits. The
`APP_SHARED_SECRET` is embedded in the APK (in `BuildConfig`/Settings), so it's not a true
secret against someone who decompiles the app — but it stops casual scraping/bots. If you
plan to publish this app to many users, replace it with real per-user authentication later.
