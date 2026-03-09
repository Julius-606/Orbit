#>>>--- START_FILE_BLOCK: backend/app/main.py
################################################################################
# FILE: backend/app/main.py
# VERSION: 1.0.7 | SYSTEM: Swagger UI Aesthetic Update & Stability
################################################################################
#
# Changes:
# - 🧹 SWAGGER COLLAPSE: Injected `swagger_ui_parameters={"docExpansion": "none"}` 
#   so the UI loads clean and collapsed, exactly like your Assistant Orbit journal requested!

import os
import logging
import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request, HTTPException, BackgroundTasks
from fastapi.responses import RedirectResponse
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Dict, Any
import uvicorn
from datetime import datetime

# 🔥 THE MISSING LIQUIDITY: Master router for Orbit-AI, Forex, etc.
from app.api.v1.api import api_router

# Configure logging for the VM
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger("OrbitBrain")

# ===============================================================================
# BACKGROUND TASKS & LIFESPAN
# ===============================================================================

async def forex_guardian_monitor():
    """Simulates the 24/7 Forex MT5 monitor. Never sleeps. Just like the markets."""
    try:
        while True:
            await asyncio.sleep(3600) # Check every hour in mock mode
    except asyncio.CancelledError:
        logger.info("Forex Guardian gracefully shutting down. Securing the bag.")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup logic: Connect to Postgres, Redis, and start workers
    logger.info("🪐 Orbit Brain booting up... Waking up Med-Scholar modules.")
    logger.info("Checking Redis cache for pending CATE triggers...")
    
    forex_task = asyncio.create_task(forex_guardian_monitor())
    yield
    
    logger.info("Shutting down Orbit. Liquidating pending tasks and closing DB safely.")
    forex_task.cancel()

# ===============================================================================
# APP INITIALIZATION (THE FIX IS HERE)
# ===============================================================================

app = FastAPI(
    title="Project Orbit API",
    description="The Life-OS backend for Med-Scholar, Forex Guardian, and CATE.",
    version="3.1.0",
    lifespan=lifespan,
    # 🚀 THE SWAGGER FIX: Forces all endpoints to be collapsed by default!
    swagger_ui_parameters={"docExpansion": "none"} 
)

origins = [
    "http://localhost",
    "http://localhost:8080",
    "*"  # Allows all origins for now.
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===============================================================================
# CORE ENDPOINTS
# ===============================================================================

@app.get("/", include_in_schema=False)
async def root():
    """Instantly redirects you to the beautiful Swagger UI."""
    return RedirectResponse(url="/docs")

@app.get("/health", tags=["System"])
async def health_check():
    """Render/HF uses this to verify the deployment didn't crash."""
    return {"status": "healthy", "brain": "locked in", "timestamp": datetime.utcnow().isoformat()}

# Mount the real endpoints
app.include_router(api_router, prefix="/api/v1")


# ===============================================================================
# CONTEXT-AWARE TRIGGER ENGINE (CATE)
# ===============================================================================

@app.post("/api/v1/cate/sync", tags=["CATE"])
async def sync_offline_messages(request: Request, background_tasks: BackgroundTasks):
    data = await request.json()
    staged_messages = data.get("messages", [])

    if not staged_messages:
        return {"status": "no_data", "message": "Nothing to sync. We are chilling."}

    logger.info(f"Received {len(staged_messages)} offline staged messages. Processing...")

    for msg in staged_messages:
        timestamp = msg.get("timestamp")
        content = msg.get("content")
        msg_type = msg.get("type", "task")
        logger.info(f"Processing delayed {msg_type} from {timestamp}: {content}")

    return {"status": "success", "synced_count": len(staged_messages)}

@app.post("/api/v1/cate/trigger", tags=["CATE"])
async def manual_cate_trigger(event_type: str):
    if event_type == "sleep_timer_zero":
        logger.info("CATE: Sleep timer hit zero. User has spawned. Initiating night-owl protocols.")
        return {"status": "triggered", "action": "night_owl_mode_activated"}
    return {"status": "ignored", "reason": "Unknown event type"}

# ===============================================================================
# MOCK ENDPOINTS
# ===============================================================================

@app.get("/api/v1/legacy/tasks", include_in_schema=False)
async def legacy_get_tasks():
    return {"tasks": ["Read Pathology", "Check XAUUSD 1H chart", "Sleep"]}

@app.post("/api/v1/legacy/notify", include_in_schema=False)
async def legacy_notify(message: str):
    return {"status": "blasted"}

# ===============================================================================
# ENTRY POINT
# ===============================================================================
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    logger.info(f"🚀 Starting Orbit Brain on port {port}...")
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=False)

#<<<--- END_FILE_BLOCK: backend/app/main.py
