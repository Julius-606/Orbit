################################################################################
#FILE: backend/app/main.py
#VERSION: 3.1.2 | SYSTEM: Jarvis Protocol
################################################################################

# ==========================================
# IDENTITY: The Brain / Central Hub
# FILEPATH: backend/app/main.py
# COMPONENT: Backend Entry Point
# SYSTEM VERSION: 3.1.2 (Sync & Blast Protocol Patch)
# FILE VERSION: 1.2.0
# ROLE: The main server file. Routes traffic, handles the "Blast" WebSocket protocol.
# VIBE: The Grand Central Station of your Life-OS. Everything passes through here.
# ==========================================


from contextlib import asynccontextmanager
from fastapi import FastAPI, Request, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
import logging
import time

from app.api.v1.api import api_router

# 🚀 THE FIX: We gotta actually import the Blast Manager so we can mount the WebSocket!
from app.services.blast import blast_engine

# -------------------------------------------------------------------
# 🛠️ VM Logging Setup (Crucial for bare-metal debugging)
# -------------------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger("orbit_brain")

# -------------------------------------------------------------------
# 🚀 Lifespan Context Manager (The Jarvis Boot Sequence)
# -------------------------------------------------------------------
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup Sequence: When Uvicorn first spins up
    logger.info("🚀 Project Orbit (Jarvis Era) is booting...")
    logger.info("📈 Forex Guardian: Waking up MT5 listeners...")
    logger.info("🩺 Med-Scholar: Loading clinical data and syllabus...")
    logger.info("🌍 SHOFCO Ops: Syncing community data...")
    yield
    # Shutdown Sequence: When you Ctrl+C to update the matrix
    logger.info("💤 Project Orbit spinning down. Catch you at the London session.")


# -------------------------------------------------------------------
# 🧠 Initializing The Brain (Project Orbit)
# -------------------------------------------------------------------
app = FastAPI(
    title="Project Orbit API (The Jarvis Era)",
    description="The Life-OS Brain for balancing Clinical Meds, SHOFCO ops, and sniping Forex setups. WAGMI 🚀📈",
    version="3.1.2",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

# -------------------------------------------------------------------
# 🛡️ CORS Setup - Letting Pocket Orbit (Android) talk to the VM
# -------------------------------------------------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Open for local testing. Lock this down to specific IPs in prod!
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------------------------------------------------
# ⏱️ Performance Middleware - Tracking latency for trade execution
# -------------------------------------------------------------------
@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = time.time() - start_time
    response.headers["X-Process-Time"] = str(process_time)

    # Log the request method, path, and how fast it processed
    logger.info(f"{request.method} {request.url.path} - {process_time:.4f}s")
    return response

# -------------------------------------------------------------------
# 🔌 Plugging the API Router into the Main Brain
# -------------------------------------------------------------------
# This right here is what populates your Swagger UI with all the endpoints
app.include_router(api_router, prefix="/api/v1")

# -------------------------------------------------------------------
# 📡 THE BLAST PROTOCOL (WebSocket Mount)
# -------------------------------------------------------------------
# 🚀 THE FIX: This route was missing! Pocket Orbit was trying to connect to a ghost.
@app.websocket("/ws/blast")
async def blast_websocket(websocket: WebSocket):
    await blast_engine.connect(websocket)
    try:
        while True:
            # We just hold the connection open. The VM is the one doing the blasting.
            # If the phone talks back via WS, we'd handle it here.
            data = await websocket.receive_text()
            logger.info(f"Received chatter from Android: {data}")
    except WebSocketDisconnect:
        blast_engine.disconnect(websocket)
        logger.info("Android disconnected. Phone probably died or hit a margin call.")

# -------------------------------------------------------------------
# 🟢 The Root Endpoint - Health Check
# -------------------------------------------------------------------
@app.get("/")
async def root():
    return {
        "message": "Project Orbit is LIVE. The VM is cooking. Time to catch these pips and pass these meds. 📈🩺",
        "status": "operational",
        "db_sync": "active",
        "vibe": "immaculate, no cap"
    }