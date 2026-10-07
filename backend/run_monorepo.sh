#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "🪐 HUGGING FACE SPACES MULTI-BACKEND INITIALIZATION"
echo "  Target: Orbit + DebateHub Unified Space"
echo "  Public Port: 7860"
echo "=========================================================="

APP_DIR="${HOME}/app"
if [ ! -d "$APP_DIR" ]; then
    APP_DIR="$(pwd)"
fi

DEBATEHUB_DIR="${APP_DIR}/DebateHub"

# 1. Boot up DebateHub on internal port 3000
if [ -d "$DEBATEHUB_DIR" ]; then
    echo "🏛️ Initializing DebateHub Backend..."
    (
        cd "$DEBATEHUB_DIR"
        export PORT=3000
        export DEBATEHUB_PORT=3000
        export VITE_BASE=/debatehub/
        if [ -n "$DEBATEHUB_NEON_DATABASE_URL" ]; then
            echo "✅ DebateHub Neon DB secret detected."
        else
            echo "ℹ️ DEBATEHUB_NEON_DATABASE_URL not detected. DebateHub will operate with local JSON persistence until Neon secret is added."
        fi

        # Run with tsx, node, or npm
        if [ -f "server.js" ]; then
            node server.js
        elif command -v npx >/dev/null 2>&1; then
            npx tsx server.ts
        elif command -v bun >/dev/null 2>&1; then
            bun run dev
        elif command -v npm >/dev/null 2>&1; then
            npm run dev -- --host 0.0.0.0
        else
            echo "⚠️ Node.js runtime not found for DebateHub."
        fi
    ) &
    DEBATEHUB_PID=$!
    echo "🚀 DebateHub launched in background (PID: $DEBATEHUB_PID, Port: 3000)"
fi

# 2. Boot up Orbit FastAPI on port 7860
echo "🪐 Starting Project Orbit Brain on port 7860..."
cd "$APP_DIR"
exec uvicorn app.main:app --host 0.0.0.0 --port 7860
