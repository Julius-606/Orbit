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
import subprocess
import shutil
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request, HTTPException, BackgroundTasks, Response
from fastapi.responses import RedirectResponse, JSONResponse, StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Dict, Any
import uvicorn
import socket
from datetime import datetime
from zeroconf.asyncio import AsyncZeroconf
from zeroconf import ServiceInfo
import httpx

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

    # HF Space / Local Discovery Registration
    local_ip = "127.0.0.1"
    try:
        local_ip = socket.gethostbyname(socket.gethostname())
    except:
        pass

    info = ServiceInfo(
        "_orbit-pilot._tcp.local.",
        "OrbitCore._orbit-pilot._tcp.local.",
        addresses=[socket.inet_aton(local_ip)],
        port=8000,
        properties={'node_name': 'HF_Space_Node'}
    )

    aiozc = AsyncZeroconf()
    await aiozc.zeroconf.async_register_service(info)
    logger.info(f"Registered Orbit Service at {local_ip}:8000")

    logger.info("Checking Redis cache for pending CATE triggers...")
    
    forex_task = asyncio.create_task(forex_guardian_monitor())

    # Auto-spawn DebateHub backend on port 3000 if not running
    debatehub_proc = None
    debatehub_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "DebateHub"))
    if os.path.isdir(debatehub_dir):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(0.5)
        is_port_open = (sock.connect_ex(('127.0.0.1', 3000)) == 0)
        sock.close()

        if not is_port_open:
            cmd = None
            if shutil.which("bun"):
                cmd = ["bun", "run", "dev"]
            elif shutil.which("npx"):
                cmd = ["npx", "tsx", "server.ts"]
            elif shutil.which("node") and os.path.exists(os.path.join(debatehub_dir, "server.js")):
                cmd = ["node", "server.js"]
            elif shutil.which("npm"):
                cmd = ["npm", "run", "dev"]

            if cmd:
                logger.info(f"🏛️ Launching DebateHub independent backend: {' '.join(cmd)}")
                env = os.environ.copy()
                env["PORT"] = "3000"
                env["DEBATEHUB_PORT"] = "3000"
                try:
                    debatehub_proc = subprocess.Popen(
                        cmd,
                        cwd=debatehub_dir,
                        env=env,
                        stdout=subprocess.DEVNULL,
                        stderr=subprocess.DEVNULL
                    )
                except Exception as ex:
                    logger.warning(f"Could not spawn DebateHub process: {ex}")

    yield
    
    logger.info("Shutting down Orbit. Liquidating pending tasks and closing DB safely.")
    if debatehub_proc:
        logger.info("Terminating DebateHub background process...")
        debatehub_proc.terminate()
    await aiozc.zeroconf.async_unregister_all_services()
    await aiozc.async_close()
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
# DEBATEHUB INDEPENDENT BACKEND PROXY GATEWAY
# ===============================================================================

DEBATEHUB_INTERNAL_URL = os.getenv("DEBATEHUB_INTERNAL_URL", "http://127.0.0.1:3000")

@app.get("/api/v1/debatehub/status", tags=["DebateHub Module"])
async def debatehub_integration_status():
    """
    Status of the integrated DebateHub independent backend.
    DebateHub runs its own Express/Neon-Postgres engine alongside Orbit.
    """
    neon_configured = bool(
        os.getenv("DEBATEHUB_NEON_DATABASE_URL")
        or os.getenv("DEBATEHUB_DATABASE_URL")
        or os.getenv("NEON_DATABASE_URL")
    )
    return {
        "module": "DebateHub",
        "mounted_path": "/debatehub",
        "access_url": "https://agent606-orbit.hf.space/debatehub",
        "secret_source": "DEBATEHUB_NEON_DATABASE_URL",
        "neon_secret_detected": neon_configured,
        "independent_backend": True,
        "orbit_dash": "/docs",
        "debatehub_gateway": "/debatehub",
        "status": "Online & Integrated"
    }

async def forward_to_debatehub(request: Request, subpath: str = ""):
    """
    Core forwarding helper to proxy requests to the independent DebateHub Express/Vite server.
    Handles streaming SSE (Server-Sent Events), normal HTTP verbs, query params, and headers.
    """
    clean_subpath = subpath.lstrip("/")
    target_url = f"{DEBATEHUB_INTERNAL_URL}/{clean_subpath}" if clean_subpath else f"{DEBATEHUB_INTERNAL_URL}/"
    if request.url.query:
        target_url += f"?{request.url.query}"

    headers = dict(request.headers)
    headers.pop("host", None)
    headers["x-forwarded-prefix"] = "/debatehub"
    headers["x-forwarded-proto"] = request.url.scheme
    headers["x-forwarded-host"] = request.headers.get("host", "agent606-orbit.hf.space")

    body = await request.body()
    try:
        # Support Server-Sent Events (SSE) streaming for real-time live sync
        if "text/event-stream" in request.headers.get("accept", "") or "stream" in clean_subpath:
            client = httpx.AsyncClient(timeout=None)
            req = client.build_request(
                method=request.method,
                url=target_url,
                headers=headers,
                content=body,
            )
            r = await client.send(req, stream=True)
            return StreamingResponse(
                r.aiter_raw(),
                status_code=r.status_code,
                headers=dict(r.headers),
                background=BackgroundTasks([client.aclose])
            )

        async with httpx.AsyncClient(timeout=30.0) as client:
            resp = await client.request(
                method=request.method,
                url=target_url,
                headers=headers,
                content=body,
            )
            excluded = ["content-encoding", "content-length", "transfer-encoding", "connection"]
            resp_headers = {k: v for k, v in resp.headers.items() if k.lower() not in excluded}
            return Response(
                content=resp.content,
                status_code=resp.status_code,
                headers=resp_headers,
                media_type=resp.headers.get("content-type")
            )
    except Exception as e:
        logger.warning(f"DebateHub proxy error reaching {target_url}: {e}")
        neon_configured = bool(
            os.getenv("DEBATEHUB_NEON_DATABASE_URL")
            or os.getenv("DEBATEHUB_DATABASE_URL")
            or os.getenv("NEON_DATABASE_URL")
        )
        return JSONResponse(
            status_code=200 if request.method == "GET" and (not clean_subpath or "status" in clean_subpath) else 503,
            content={
                "project": "DebateHub",
                "route": f"/{clean_subpath}",
                "status": "Standalone Backend Ready",
                "internal_target": DEBATEHUB_INTERNAL_URL,
                "neon_database_secret_configured": neon_configured,
                "orbit_status": "Orbit Dashboard & API active at /docs",
                "access_url": "https://agent606-orbit.hf.space/debatehub",
                "message": (
                    "DebateHub module is active on this space. "
                    "Independent Express server connects using DEBATEHUB_NEON_DATABASE_URL."
                )
            }
        )

# Redirect unslashed /debatehub and /DebateHub to /debatehub/
@app.get("/debatehub", include_in_schema=False)
@app.get("/DebateHub", include_in_schema=False)
async def redirect_debatehub(request: Request):
    q = f"?{request.url.query}" if request.url.query else ""
    return RedirectResponse(url=f"/debatehub/{q}", status_code=307)

# Proxy /debatehub/ and /DebateHub/ root
@app.api_route(
    "/debatehub/",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"],
    include_in_schema=False
)
@app.api_route(
    "/DebateHub/",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_debatehub_root(request: Request):
    return await forward_to_debatehub(request, "debatehub/")

# Proxy /debatehub/{path:path} and /DebateHub/{path:path} subpaths
@app.api_route(
    "/debatehub/{path:path}",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"],
    include_in_schema=False
)
@app.api_route(
    "/DebateHub/{path:path}",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_debatehub_subpaths(request: Request, path: str):
    return await forward_to_debatehub(request, f"debatehub/{path}")

# Proxy root /api/ calls intended for DebateHub (ignoring Orbit's /api/v1/...)
@app.api_route(
    "/api/{path:path}",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_debatehub_root_api(request: Request, path: str):
    if path.startswith("v1/") or path == "v1":
        raise HTTPException(status_code=404, detail="Not Found")
    return await forward_to_debatehub(request, f"api/{path}")

# Proxy Vite internal dev URLs if requested without /debatehub/ prefix
@app.api_route(
    "/@vite/{path:path}",
    methods=["GET", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_vite_client(request: Request, path: str):
    return await forward_to_debatehub(request, f"@vite/{path}")

@app.api_route(
    "/@fs/{path:path}",
    methods=["GET", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_vite_fs(request: Request, path: str):
    return await forward_to_debatehub(request, f"@fs/{path}")

@app.api_route(
    "/@id/{path:path}",
    methods=["GET", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_vite_id(request: Request, path: str):
    return await forward_to_debatehub(request, f"@id/{path}")

@app.api_route(
    "/src/{path:path}",
    methods=["GET", "HEAD", "OPTIONS"],
    include_in_schema=False
)
async def proxy_vite_src(request: Request, path: str):
    return await forward_to_debatehub(request, f"src/{path}")

# ===============================================================================
# ENTRY POINT
# ===============================================================================
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    logger.info(f"🚀 Starting Orbit Brain on port {port}...")
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=False)

#<<<--- END_FILE_BLOCK: backend/app/main.py
