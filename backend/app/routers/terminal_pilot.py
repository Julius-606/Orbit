################################################################################
# FILE: backend/app/routers/terminal_pilot.py
# VERSION: 2.0.0 | SYSTEM: Orbit Decentralized Cluster Workspace
# IDENTITY: Integrates multi-node target execution with dynamic suggestions.
################################################################################

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import os
import json
import sqlite3
import google.generativeai as genai
from app.core.config import settings
from app.core.nodes import cluster_manager

router = APIRouter(prefix="/terminal", tags=["Terminal Pilot"])

class TerminalManager:
    def __init__(self):
        self.db_path = "vault.db"
        self._init_vault()
        self.log_buffer = "Terminal initialized...\nPS C:\\Users\\Administrator> "

    def _init_vault(self):
        conn = sqlite3.connect(self.db_path)
        conn.execute("CREATE TABLE IF NOT EXISTS commands (id INTEGER PRIMARY KEY, name TEXT, cmd TEXT, category TEXT)")
        conn.commit()
        conn.close()

    def save_command(self, name, cmd, category="General"):
        conn = sqlite3.connect(self.db_path)
        conn.execute("INSERT INTO commands (name, cmd, category) VALUES (?, ?, ?)", (name, cmd, category))
        conn.commit()
        conn.close()

    def get_vault_commands(self):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.execute("SELECT id, name, cmd, category FROM commands")
        rows = cursor.fetchall()
        conn.close()
        return [{"id": r[0], "name": r[1], "cmd": r[2], "category": r[3]} for r in rows]

manager = TerminalManager()

class CommandRequest(BaseModel):
    command: str

class SuggestionRequest(BaseModel):
    user_goal: str

class VaultSaveRequest(BaseModel):
    name: str
    cmd: str
    category: Optional[str] = "General"

class ConnectNodeRequest(BaseModel):
    name: str
    host: str
    port: int = 8888

class SelectNodeRequest(BaseModel):
    name: str

@router.get("/status")
async def get_status():
    node = cluster_manager.get_active()
    status_data = await node.get_status()
    # Synchronize prompt log style locally
    return {
        "active_node": cluster_manager.active_node_name,
        "nodes": list(cluster_manager.nodes.keys()),
        "telemetry": status_data.get("telemetry", {"cpu": 0, "ram": 0, "disk": 0, "cwd": "unknown"}),
        "output": manager.log_buffer
    }

@router.post("/execute")
async def execute_command(req: CommandRequest):
    node = cluster_manager.get_active()
    manager.log_buffer += req.command + "\nPS C:\\Users\\Administrator> "
    await node.execute(req.command)
    return {"status": "success", "message": "Command piped to cluster target"}

@router.post("/suggest")
async def suggest_command(req: SuggestionRequest):
    if not settings.GEMINI_API_KEY:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY not configured on backend")

    try:
        prompt = (
            f"You are an expert system administrator and PowerShell master. "
            f"Your task is to convert the following user goal into a PowerShell command line and a brief explanation.\n\n"
            f"Terminal Context:\n{manager.log_buffer[-2000:]}\n\n"
            f"User Goal: {req.user_goal}\n\n"
            f"Format your response as a JSON object with two keys: 'command' (the raw PowerShell string) and 'explanation' (a brief one-sentence description)."
        )
        genai.configure(api_key=settings.GEMINI_API_KEY)
        # 🔥 Fixed model to use latest Gemini 2.5 Flash for elite functionality without breakages
        model = genai.GenerativeModel('gemini-2.5-flash')
        response = model.generate_content(
            prompt,
            generation_config={"response_mime_type": "application/json"}
        )
        data = json.loads(response.text)
        return data
    except Exception as e:
        return {"command": f"# Error: {str(e)}", "explanation": "Failed to generate AI suggestion."}

@router.get("/vault")
async def get_vault():
    return {"commands": manager.get_vault_commands()}

@router.post("/vault/save")
async def save_to_vault(req: VaultSaveRequest):
    manager.save_command(req.name, req.cmd, req.category)
    return {"status": "success", "message": "Command saved to vault"}

@router.post("/nodes/connect")
async def connect_node(req: ConnectNodeRequest):
    cluster_manager.add_node(req.name, req.host, req.port)
    return {"status": "success", "message": f"Node {req.name} attached to cluster mapping"}

@router.post("/nodes/select")
async def select_node(req: SelectNodeRequest):
    cluster_manager.select_node(req.name)
    return {"status": "success", "selected": cluster_manager.active_node_name}

@router.post("/clear")
async def clear_terminal():
    manager.log_buffer = "PS C:\\Users\\Administrator> "
    return {"status": "success"}
