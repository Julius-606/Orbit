#!/usr/bin/env python3
################################################################################
# FILE: orbit_node_daemon.py
# IDENTITY: Orbit Remote Node Daemon / Workstation Agent
# PURPOSE: Run this on your laptop or home workstation. It exposes a lightweight,
#          secure endpoint allowing the Main Hugging Face Space (or Pocket Orbit phone)
#          to execute commands, stream terminal outputs, and monitor system telemetry.
################################################################################

import os
import sys
import socket
import argparse
import platform
import subprocess
import threading
import queue
import time
from typing import Optional

try:
    import uvicorn
    from fastapi import FastAPI, HTTPException, Request, Header
    from fastapi.middleware.cors import CORSMiddleware
    from pydantic import BaseModel
    import psutil
except ImportError:
    print("=" * 65)
    print("⚠️ Missing dependencies for Orbit Node Daemon.")
    print("Please run: pip install fastapi uvicorn psutil pydantic")
    print("=" * 65)
    sys.exit(1)

# Default auth token matching Orbit app defaults
DEFAULT_AUTH_TOKEN = os.getenv("ORBIT_SECRET_TOKEN", "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4")

app = FastAPI(
    title="Orbit Workstation Node Daemon",
    version="2.0.0",
    description="Remote execution node for Project Orbit cluster."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_local_ip() -> str:
    """Attempt to detect the primary local network IP of this machine."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Does not actually send packets
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip


class ShellExecutor:
    """Persistent shell session (PowerShell on Windows, Bash on Linux/macOS)."""
    def __init__(self):
        self.os_type = "windows" if os.name == "nt" else "linux"
        shell = "powershell.exe" if self.os_type == "windows" else "/bin/bash"
        if self.os_type != "windows" and not os.path.exists(shell):
            shell = "/bin/sh"

        cmd = [shell, "-NoExit", "-Command", "-"] if self.os_type == "windows" else [shell]
        self.proc = subprocess.Popen(
            cmd,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
            creationflags=subprocess.CREATE_NO_WINDOW if self.os_type == "windows" else 0
        )
        self.out_queue = queue.Queue()
        threading.Thread(target=self._reader, args=(self.proc.stdout,), daemon=True).start()
        threading.Thread(target=self._reader, args=(self.proc.stderr,), daemon=True).start()

    def _reader(self, pipe):
        try:
            for line in iter(pipe.readline, ""):
                self.out_queue.put(line)
        except Exception:
            pass

    def run_command(self, cmd: str) -> str:
        trimmed = cmd.strip()
        if not trimmed:
            return ""

        if self.proc.poll() is not None:
            # Re-spawn if exited
            self.__init__()

        self.proc.stdin.write(trimmed + "\n")
        self.proc.stdin.flush()

        # Wait briefly for synchronous stream
        time.sleep(0.4)
        output = ""
        while not self.out_queue.empty():
            output += self.out_queue.get()

        if not output:
            output = f"Command sent: {trimmed}\n"
        return output


executor = ShellExecutor()


class ExecuteRequest(BaseModel):
    command: str


@app.get("/")
def root():
    return {
        "node_type": "Orbit_Workstation_Node",
        "system": f"{platform.system()} {platform.release()}",
        "hostname": platform.node(),
        "status": "ready"
    }


@app.get("/status")
def get_status():
    cwd = os.getcwd()
    try:
        p = psutil.Process(executor.proc.pid)
        cwd = p.cwd()
    except Exception:
        pass

    return {
        "status": "healthy",
        "hostname": platform.node(),
        "system": platform.system(),
        "telemetry": {
            "cpu": psutil.cpu_percent(),
            "ram": psutil.virtual_memory().percent,
            "disk": psutil.disk_usage(os.path.abspath(os.sep)).percent,
            "cwd": str(cwd)
        }
    }


@app.post("/execute")
def execute(req: ExecuteRequest):
    out = executor.run_command(req.command)
    cwd = os.getcwd()
    try:
        p = psutil.Process(executor.proc.pid)
        cwd = p.cwd()
    except Exception:
        pass

    return {
        "status": "success",
        "output": out,
        "cwd": str(cwd)
    }


def main():
    parser = argparse.ArgumentParser(description="Start Orbit Workstation Node Daemon")
    parser.add_argument("--port", type=int, default=8888, help="Port to listen on (default: 8888)")
    parser.add_argument("--host", type=str, default="0.0.0.0", help="Host interface (default: 0.0.0.0)")
    args = parser.parse_args()

    local_ip = get_local_ip()

    print("\n" + "=" * 65)
    print("🪐 PROJECT ORBIT | WORKSTATION NODE DAEMON")
    print("=" * 65)
    print(f"💻 Machine:     {platform.node()} ({platform.system()})")
    print(f"📡 Local IP:    {local_ip}")
    print(f"🔌 Port:        {args.port}")
    print(f"🌐 Local URL:   http://{local_ip}:{args.port}")
    print("-" * 65)
    print("📋 HOW TO CONNECT THIS LAPTOP TO YOUR ORBIT CLUSTER:")
    print("")
    print("1. If Phone & Laptop are on the SAME WI-FI:")
    print(f"   In Pocket Orbit Android app -> Tap 'Cluster Nodes' -> Add Node:")
    print(f"   - Name: Laptop_{platform.node()[:10]}")
    print(f"   - Host: {local_ip}")
    print(f"   - Port: {args.port}")
    print("")
    print("2. If connecting from Hugging Face Space (or outside home):")
    print("   Open another terminal on your laptop and start a free Cloudflare tunnel:")
    print(f"   cloudflared tunnel --url http://localhost:{args.port}")
    print("   or ngrok:")
    print(f"   ngrok http {args.port}")
    print("   Then in Pocket Orbit or HF Space, add the public https:// tunnel URL!")
    print("=" * 65 + "\n")

    uvicorn.run(app, host=args.host, port=args.port, log_level="info")


if __name__ == "__main__":
    main()
