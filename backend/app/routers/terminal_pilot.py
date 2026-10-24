################################################################################
# FILE: backend/app/routers/terminal_pilot.py
# VERSION: 3.0.0 | SYSTEM: Orbit Decentralized Cluster Workspace
# IDENTITY: Real OS detection, multi-command procedure suggestions, and VS Code terminal sync.
################################################################################

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import os
import sys
import json
import sqlite3
import platform
import asyncio
import logging
from google import genai
from google.genai import types
from app.core.config import settings
from app.core.key_rotation import gemini_key_manager
from app.core.nodes import cluster_manager

logger = logging.getLogger("TerminalPilot")

router = APIRouter(prefix="/terminal", tags=["Terminal Pilot"])

def get_terminal_genai_client():
    key = gemini_key_manager.get_current_key()
    if not key:
        return None
    try:
        return genai.Client(api_key=key)
    except Exception:
        return None

class TerminalManager:
    def __init__(self):
        base_dir = os.path.dirname(os.path.abspath(__file__))
        candidate_path = os.path.join(base_dir, "vault.db")
        try:
            with open(candidate_path, "a"):
                pass
            self.db_path = candidate_path
        except Exception:
            import tempfile
            self.db_path = os.path.join(tempfile.gettempdir(), "orbit_vault.db")
        self._init_vault()
        self.os_type = "windows" if os.name == "nt" else "linux"
        self.cwd = os.getcwd()
        self.log_buffer = f"Orbit Terminal Deck [System: {platform.system()} {platform.release()}]\nConnected to cluster node.\n{self.get_prompt()}\n"

    def _init_vault(self):
        try:
            conn = sqlite3.connect(self.db_path)
            conn.execute("CREATE TABLE IF NOT EXISTS commands (id INTEGER PRIMARY KEY, name TEXT, cmd TEXT, category TEXT)")
            conn.commit()
            conn.close()
        except Exception as e:
            logger.warning(f"Vault init warning: {e}")

    def get_prompt(self, cwd: Optional[str] = None) -> str:
        active_cwd = cwd or self.cwd
        if self.os_type == "windows":
            return f"{active_cwd}> "
        else:
            folder = os.path.basename(active_cwd.rstrip("/\\")) or "/"
            return f"orbit@{platform.node()}:{folder}$ "

    def save_command(self, name, cmd, category="General"):
        try:
            conn = sqlite3.connect(self.db_path)
            conn.execute("CREATE TABLE IF NOT EXISTS commands (id INTEGER PRIMARY KEY, name TEXT, cmd TEXT, category TEXT)")
            conn.execute("INSERT INTO commands (name, cmd, category) VALUES (?, ?, ?)", (name, cmd, category))
            conn.commit()
            conn.close()
        except Exception as e:
            logger.error(f"Error saving to vault: {e}")

    def get_vault_commands(self):
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.execute("SELECT id, name, cmd, category FROM commands")
            rows = cursor.fetchall()
            conn.close()
            return [{"id": r[0], "name": r[1], "cmd": r[2], "category": r[3]} for r in rows]
        except Exception:
            return []

manager = TerminalManager()

class CommandRequest(BaseModel):
    command: str

class SuggestionRequest(BaseModel):
    user_goal: str
    terminal_output: Optional[str] = None

class SuggestedStep(BaseModel):
    step: int
    title: str
    cmd: str

class SuggestionResponse(BaseModel):
    explanation: str
    command: str
    commands: Optional[List[SuggestedStep]] = []

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
    telemetry = status_data.get("telemetry", {})
    cwd = telemetry.get("cwd", os.getcwd())
    manager.cwd = cwd
    
    return {
        "active_node": cluster_manager.active_node_name,
        "nodes": list(cluster_manager.nodes.keys()),
        "nodes_details": cluster_manager.get_nodes_info(),
        "telemetry": {
            "cpu": telemetry.get("cpu", 0),
            "ram": telemetry.get("ram", 0),
            "disk": telemetry.get("disk", 0),
            "cwd": cwd,
            "os": manager.os_type,
            "shell": "powershell" if manager.os_type == "windows" else "bash"
        },
        "output": manager.log_buffer,
        "os": manager.os_type,
        "prompt": manager.get_prompt(cwd)
    }

@router.post("/execute")
async def execute_command(req: CommandRequest):
    node = cluster_manager.get_active()
    current_prompt = manager.get_prompt()
    manager.log_buffer += f"{current_prompt}{req.command}\n"

    output = await node.execute(req.command)
    manager.log_buffer += output
    if not manager.log_buffer.endswith("\n"):
        manager.log_buffer += "\n"

    # Refresh status to get new cwd if changed (e.g. after cd)
    status_data = await node.get_status()
    new_cwd = status_data.get("telemetry", {}).get("cwd", manager.cwd)
    manager.cwd = new_cwd
    new_prompt = manager.get_prompt(new_cwd)
    manager.log_buffer += new_prompt

    return {
        "status": "success",
        "message": "Command executed",
        "output": output,
        "cwd": new_cwd,
        "prompt": new_prompt
    }

@router.post("/suggest")
async def suggest_command(req: SuggestionRequest):
    os_name = "Windows (PowerShell)" if manager.os_type == "windows" else "Linux/Unix (bash)"
    cwd = manager.cwd

    terminal_log_snippet = ""
    if req.terminal_output and req.terminal_output.strip():
        clean_logs = req.terminal_output.strip()[:4000]
        terminal_log_snippet = (
            f"\n\n--- ACTUAL TERMINAL EXECUTION LOGS & CONTEXT ---\n"
            f"```\n{clean_logs}\n```\n"
            f"DIAGNOSTIC INSTRUCTIONS: The user has attached the above terminal output or error logs from their active session. "
            f"Analyze any error messages, exit codes, missing dependencies, tracebacks, or system states to diagnose the exact problem "
            f"and provide precise, working remedial commands.\n"
        )

    if gemini_key_manager.has_keys():
        try:
            prompt = (
                f"You are an expert DevOps and Systems Administrator in a VS Code terminal environment.\n"
                f"Operating System: {os_name}\n"
                f"Current Working Directory: {cwd}\n"
                f"User Goal: {req.user_goal}\n"
                f"{terminal_log_snippet}\n"
                f"Generate a concise, safe procedure to accomplish this goal. You may provide a single command "
                f"or a sequence of procedure steps (up to 4 steps) if the task involves multiple steps (e.g. git, docker, setup, debugging).\n"
                f"Return strictly valid JSON with this structure:\n"
                f"{{\n"
                f"  \"explanation\": \"Brief explanation of what this procedure does and why (referencing any log errors if diagnosed)\",\n"
                f"  \"command\": \"The primary command or one-liner\",\n"
                f"  \"commands\": [\n"
                f"    {{\"step\": 1, \"title\": \"Step description\", \"cmd\": \"exact command\"}},\n"
                f"    {{\"step\": 2, \"title\": \"Step description\", \"cmd\": \"exact command\"}}\n"
                f"  ]\n"
                f"}}"
            )

            target_models = ['gemini-2.5-flash', 'gemini-1.5-flash', 'gemini-3.8-flash']
            response = None
            max_rotation_attempts = max(1, gemini_key_manager.key_count)

            for attempt in range(max_rotation_attempts):
                client = get_terminal_genai_client()
                if not client:
                    gemini_key_manager.rotate_to_next_key()
                    continue

                for m in target_models:
                    try:
                        response = await client.aio.models.generate_content(
                            model=m,
                            contents=prompt,
                            config=types.GenerateContentConfig(
                                response_mime_type="application/json"
                            )
                        )
                        if response:
                            break
                    except Exception as model_err:
                        err_str = str(model_err).lower()
                        if "quota" in err_str or "429" in err_str or "resource_exhausted" in err_str or "invalid" in err_str:
                            logger.warning(f"Terminal pilot key quota exceeded on {m}. Rotating key...")
                            gemini_key_manager.rotate_to_next_key()
                            break
                        logger.warning(f"Terminal pilot model {m} fallback: {model_err}")

                if response:
                    break

            if not response:
                raise Exception("All terminal suggestion models unavailable")

            data = json.loads(response.text)
            commands_list = data.get("commands", [])
            primary_cmd = data.get("command") or (commands_list[0]["cmd"] if commands_list else "ls")
            return {
                "explanation": data.get("explanation", "Recommended command procedure"),
                "command": primary_cmd,
                "commands": commands_list
            }
        except Exception as e:
            pass

    # Heuristic fallback if API key is absent or network fails
    log_lower = (req.terminal_output or "").lower()
    if "permission denied" in log_lower or "eacces" in log_lower:
        cmd_fix = "sudo !!" if manager.os_type != "windows" else "Start-Process powershell -Verb runAs"
        return {
            "explanation": "Permission denied error detected in terminal logs. Elevation or permission adjustment required.",
            "command": cmd_fix,
            "commands": [
                {"step": 1, "title": "Inspect file ownership and permissions", "cmd": "ls -la" if manager.os_type != "windows" else "Get-Acl ."},
                {"step": 2, "title": "Execute with elevated permissions", "cmd": cmd_fix}
            ]
        }
    elif "command not found" in log_lower or "not recognized as" in log_lower:
        return {
            "explanation": "Missing command or binary detected in terminal output. Inspecting PATH and environment.",
            "command": "echo $PATH" if manager.os_type != "windows" else "echo $env:PATH",
            "commands": [
                {"step": 1, "title": "Inspect executable path variable", "cmd": "echo $PATH" if manager.os_type != "windows" else "echo $env:PATH"},
                {"step": 2, "title": "Verify available developer toolchains", "cmd": "which git node python3 2>/dev/null || where.exe git node python"}
            ]
        }
    elif "port" in log_lower and ("already in use" in log_lower or "eaddrinuse" in log_lower or "bind" in log_lower):
        return {
            "explanation": "Port binding collision detected in logs. Locating and freeing busy network socket.",
            "command": "lsof -i -P -n | grep LISTEN" if manager.os_type != "windows" else "netstat -ano | findstr LISTENING",
            "commands": [
                {"step": 1, "title": "Find listening port and PID", "cmd": "lsof -i -P -n | grep LISTEN" if manager.os_type != "windows" else "netstat -ano | findstr LISTENING"},
                {"step": 2, "title": "Terminate conflicting process", "cmd": "kill -9 <PID>" if manager.os_type != "windows" else "taskkill /F /PID <PID>"}
            ]
        }

    goal_lower = req.user_goal.lower()
    if "git" in goal_lower or "commit" in goal_lower or "push" in goal_lower:
        return {
            "explanation": "Git repository synchronization procedure: check status, stage changes, commit, and push.",
            "command": "git status && git add . && git commit -m 'update' && git push",
            "commands": [
                {"step": 1, "title": "Check repository status", "cmd": "git status"},
                {"step": 2, "title": "Stage all changes", "cmd": "git add ."},
                {"step": 3, "title": "Commit with descriptive message", "cmd": "git commit -m 'chore: update changes'"},
                {"step": 4, "title": "Push to remote repository", "cmd": "git push"}
            ]
        }
    elif "docker" in goal_lower:
        return {
            "explanation": "Docker inspection and build procedure.",
            "command": "docker ps -a",
            "commands": [
                {"step": 1, "title": "List running containers", "cmd": "docker ps"},
                {"step": 2, "title": "List all container images", "cmd": "docker images"},
                {"step": 3, "title": "View container system stats", "cmd": "docker stats --no-stream"}
            ]
        }
    elif "disk" in goal_lower or "space" in goal_lower or "memory" in goal_lower or "ram" in goal_lower:
        if manager.os_type == "windows":
            return {
                "explanation": "Check Windows memory and disk utilization.",
                "command": "Get-PSDrive -PSProvider FileSystem; Get-Process | Sort-Object WorkingSet -Descending | Select-Object -First 5",
                "commands": [
                    {"step": 1, "title": "Check storage space", "cmd": "Get-PSDrive -PSProvider FileSystem"},
                    {"step": 2, "title": "Top RAM-consuming processes", "cmd": "Get-Process | Sort-Object WorkingSet -Descending | Select-Object -First 5"}
                ]
            }
        else:
            return {
                "explanation": "Check Linux storage and RAM status.",
                "command": "df -h; free -m",
                "commands": [
                    {"step": 1, "title": "Disk space utilization", "cmd": "df -h"},
                    {"step": 2, "title": "Memory / RAM metrics", "cmd": "free -h"},
                    {"step": 3, "title": "Top CPU processes", "cmd": "ps aux --sort=-%cpu | head -n 6"}
                ]
            }
    else:
        default_cmd = "dir" if manager.os_type == "windows" else "ls -la"
        return {
            "explanation": f"Inspect current directory contents in {os_name}.",
            "command": default_cmd,
            "commands": [
                {"step": 1, "title": "List directory entries", "cmd": default_cmd}
            ]
        }

@router.get("/vault")
async def get_vault():
    return {"commands": manager.get_vault_commands()}

@router.post("/vault/save")
async def save_to_vault(req: VaultSaveRequest):
    manager.save_command(req.name, req.cmd, req.category or "General")
    return {"status": "success", "message": "Command saved to vault"}

@router.get("/nodes")
async def list_nodes():
    return {
        "active_node": cluster_manager.active_node_name,
        "nodes": cluster_manager.get_nodes_info()
    }

@router.post("/nodes/connect")
async def connect_node(req: ConnectNodeRequest):
    cluster_manager.add_node(req.name, req.host, req.port)
    return {
        "status": "success",
        "message": f"Node '{req.name}' registered to cluster",
        "active_node": cluster_manager.active_node_name,
        "nodes": list(cluster_manager.nodes.keys())
    }

@router.post("/nodes/select")
async def select_node(req: SelectNodeRequest):
    cluster_manager.select_node(req.name)
    return {"status": "success", "selected": cluster_manager.active_node_name}

@router.delete("/nodes/{name}")
async def disconnect_node(name: str):
    removed = cluster_manager.remove_node(name)
    if not removed:
        raise HTTPException(status_code=400, detail=f"Cannot remove protected or nonexistent node '{name}'")
    return {
        "status": "success",
        "message": f"Node '{name}' removed",
        "active_node": cluster_manager.active_node_name,
        "nodes": list(cluster_manager.nodes.keys())
    }

@router.post("/clear")
async def clear_terminal():
    prompt = manager.get_prompt()
    manager.log_buffer = prompt
    return {"status": "success", "prompt": prompt}

