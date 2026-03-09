################################################################################
# FILE: backend/app/main.py
# VERSION: 1.0.2 | SYSTEM: Dynamic Port Binding & Full Restoration
################################################################################
#
# Changes:
# - Restored the full 120+ line architecture (Med-Scholar, Forex Guardian, CATE).
# - Added dynamic port binding at the bottom for Render compatibility.

import os
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Dict, Any
import uvicorn
from datetime import datetime

# Configure logging for the VM
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger("OrbitBrain")

# --- System Lifespan (Startup & Shutdown) ---
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup logic: Connect to Postgres & Redis
    logger.info("🪐 Orbit Brain booting up... Waking up the Forex Guardian and Med-Scholar modules.")
    logger.info("Checking Redis cache for pending CATE triggers...")
    # TODO: Initialize async database engines and Redis connection pools here
    yield
    # Shutdown logic: Close connections
    logger.info("Shutting down Orbit. Securing the bag and closing DB connections safely.")

# Initialize FastAPI app (Orbit Brain)
app = FastAPI(
    title="Project Orbit API",
    description="The Life-OS backend for Med-Scholar, Forex Guardian, and CATE.",
    version="3.1.0",
    lifespan=lifespan
)

# --- CORS Configuration ---
# Must allow the Android app (Pocket Orbit) and Ubuntu Workstation
origins = [
    "http://localhost",
    "http://localhost:8080",
    "*"  # Allows all origins for now. Make sure to lock this down in prod!
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

@app.get("/")
async def root():
    return {"message": "Orbit API is online and vibing. 🪐", "status": "active"}

@app.get("/health")
async def health_check():
    """Render uses this to verify the deployment didn't crash."""
    return {"status": "healthy", "brain": "locked in", "timestamp": datetime.utcnow().isoformat()}

# ===============================================================================
# MED-SCHOLAR ENDPOINTS
# ===============================================================================

@app.get("/api/v1/med-scholar/tasks")
async def get_study_tasks():
    """Fetches upcoming Medicine/Pathology syllabus tasks."""
    logger.info("Fetching Med-Scholar tasks for the day...")
    # Mock data - integrate with Postgres later
    return {
        "tasks": [
            {"id": 1, "subject": "Pathology", "topic": "Cellular Injury", "due": "14:00"},
            {"id": 2, "subject": "Pharmacology", "topic": "Autonomic Nervous System", "due": "18:00"}
        ]
    }

# ===============================================================================
# FOREX GUARDIAN ENDPOINTS
# ===============================================================================

@app.post("/api/v1/forex/webhook")
async def forex_webhook(request: Request):
    """Listens for MT5 triggers (e.g., SL hit, TP hit, Margin alerts)."""
    payload = await request.json()
    logger.warning(f"Forex Guardian Alert: {payload}")

    # Check if a trade hit SL
    if payload.get("event") == "stop_loss":
        logger.error("Stop loss hit! Sending blast notification to Ubuntu & Pocket Orbit.")
        # TODO: Trigger push notification pipeline

    return {"status": "received", "action": "monitoring"}

# ===============================================================================
# CONTEXT-AWARE TRIGGER ENGINE (CATE)
# ===============================================================================

@app.post("/api/v1/cate/sync")
async def sync_offline_messages(request: Request, background_tasks: BackgroundTasks):
    """
    Handles staged offline messages from the phone.
    Crucial for when Safaricom drops connection in Thika.
    """
    data = await request.json()
    staged_messages = data.get("messages", [])

    if not staged_messages:
        return {"status": "no_data", "message": "Nothing to sync."}

    logger.info(f"Received {len(staged_messages)} offline staged messages. Processing...")

    for msg in staged_messages:
        timestamp = msg.get("timestamp")
        content = msg.get("content")
        msg_type = msg.get("type", "task") # task vs reminder

        # Log the historical context so Orbit AI knows this isn't a *new* request
        logger.info(f"Processing delayed {msg_type} from {timestamp}: {content}")

        # TODO: Forward to LLM to distinguish between actionable tasks and reminders

    return {"status": "success", "synced_count": len(staged_messages)}


# ===============================================================================
# SERVER ENTRY POINT & RENDER PORT BINDING
# ===============================================================================

if __name__ == "__main__":
    # RENDER FIX: Read $PORT from the environment variables.
    # If it's not there (like on your local VM in Thika), default to 8000.
    port = int(os.environ.get("PORT", 8000))

    logger.info(f"🚀 Starting Orbit Brain on port {port}...")

    # Run Uvicorn programmatically, binding to 0.0.0.0 so Render can route traffic to it
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=False)