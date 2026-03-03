# ==========================================
# IDENTITY: The Brain / Central Hub
# FILEPATH: backend/app/main.py
# COMPONENT: Backend Entry Point
# ROLE: The main server file. Routes traffic, handles the "Blast" WebSocket protocol.
# VIBE: The Grand Central Station of your Life-OS. Everything passes through here.
# ==========================================

from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from core.config import settings
import logging

# Set up logging so we know when the app is acting mid
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("Orbit-Core")

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="The centralized brain for the Life-OS."
)

# CORS - Let the Android app and Laptop interface talk to us
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # In prod, restrict this to your actual IPs!
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# The Blast Protocol (Connection Manager)
class ConnectionManager:
    def __init__(self):
        self.active_connections: list[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)
        logger.info("New satellite device connected to Orbit. 🪐")

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)
        logger.info("Satellite device disconnected.")

    async def broadcast(self, message: dict):
        # Sends message to Ubuntu Workstation AND Android Pocket
        for connection in self.active_connections:
            await connection.send_json(message)

manager = ConnectionManager()

@app.get("/")
async def root():
    return {"status": "Orbit-Core is online. Ready to grind.", "version": settings.VERSION}

@app.websocket("/ws/blast")
async def websocket_endpoint(websocket: WebSocket):
    """
    The main artery for Dual-Device Sync.
    If a trade hits SL, this route blasts it to your phone and laptop instantly.
    """
    await manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            # Echo back for now, but eventually this triggers CATE logic
            await manager.broadcast({"event": "SYNC_PING", "data": data})
    except WebSocketDisconnect:
        manager.disconnect(websocket)
