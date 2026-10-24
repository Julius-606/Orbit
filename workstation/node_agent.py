#!/usr/bin/env python3
################################################################################
# FILE: workstation/node_agent.py
# VERSION: 2.5.0 | SYSTEM: Unified Multi-Transport Workstation Node Daemon
# IDENTITY: Orbit Remote Node Daemon (Hotspot, USB Tethering, Bluetooth & LAN)
################################################################################

import os
import sys
import json
import socket
import argparse
import platform
import subprocess
import threading
import queue
import time
from typing import Optional, List, Dict, Any

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
    version="2.5.0",
    description="Multi-transport remote execution node for Project Orbit."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_all_interfaces() -> List[Dict[str, str]]:
    """Enumerate all network interfaces and classify them (Hotspot, USB, LAN)."""
    detected = []
    try:
        addrs = psutil.net_if_addrs()
        for iface_name, nic_addrs in addrs.items():
            for addr in nic_addrs:
                if addr.family == socket.AF_INET and not addr.address.startswith("127."):
                    ip = addr.address
                    lower_name = iface_name.lower()

                    if "rndis" in lower_name or "usb" in lower_name or ip.startswith("192.168.42."):
                        category = "USB Tethering"
                    elif "ap" in lower_name or "softap" in lower_name or ip.startswith("192.168.43."):
                        category = "Phone Hotspot"
                    elif "wlan" in lower_name or "wi-fi" in lower_name:
                        category = "Wi-Fi LAN"
                    elif "eth" in lower_name or "ethernet" in lower_name:
                        category = "Ethernet"
                    else:
                        category = "Network Interface"

                    detected.append({
                        "name": iface_name,
                        "ip": ip,
                        "category": category
                    })
    except Exception:
        pass

    if not detected:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            s.connect(("8.8.8.8", 80))
            detected.append({"name": "primary", "ip": s.getsockname()[0], "category": "LAN"})
        except Exception:
            detected.append({"name": "loopback", "ip": "127.0.0.1", "category": "Localhost"})
        finally:
            s.close()

    return detected


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
            self.__init__()

        self.proc.stdin.write(trimmed + "\n")
        self.proc.stdin.flush()

        time.sleep(0.4)
        output = ""
        while not self.out_queue.empty():
            output += self.out_queue.get()

        if not output:
            output = f"Command sent: {trimmed}\n"
        return output

    def get_cwd(self) -> str:
        cwd = os.getcwd()
        try:
            p = psutil.Process(self.proc.pid)
            cwd = p.cwd()
        except Exception:
            pass
        return str(cwd)


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
    return {
        "status": "healthy",
        "hostname": platform.node(),
        "node_name": f"PC_{platform.node().replace(' ', '_')}",
        "system": platform.system(),
        "cpu_usage": psutil.cpu_percent(),
        "ram_usage": psutil.virtual_memory().percent,
        "disk_usage": psutil.disk_usage(os.path.abspath(os.sep)).percent,
        "cwd": executor.get_cwd(),
        "telemetry": {
            "cpu": psutil.cpu_percent(),
            "ram": psutil.virtual_memory().percent,
            "disk": psutil.disk_usage(os.path.abspath(os.sep)).percent,
            "cwd": executor.get_cwd()
        }
    }


@app.post("/execute")
def execute(req: ExecuteRequest):
    out = executor.run_command(req.command)
    return {
        "status": "success",
        "output": out,
        "cwd": executor.get_cwd(),
        "exit_code": 0
    }


def run_bluetooth_server(channel: int = 1):
    if not hasattr(socket, "AF_BLUETOOTH"):
        return

    try:
        bt_proto = getattr(socket, "BTPROTO_RFCOMM", 3)
        server_sock = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, bt_proto)
        server_sock.bind(("", channel))
        server_sock.listen(1)

        while True:
            try:
                client_sock, client_info = server_sock.accept()
                fileobj = client_sock.makefile("r")
                while True:
                    line = fileobj.readline()
                    if not line:
                        break
                    
                    try:
                        req_data = json.loads(line.strip())
                        action = req_data.get("action", "execute")
                        
                        if action == "execute":
                            cmd = req_data.get("command", "")
                            out = executor.run_command(cmd)
                            resp = {
                                "status": "success",
                                "output": out,
                                "cwd": executor.get_cwd(),
                                "exit_code": 0
                            }
                        elif action == "status":
                            resp = {
                                "status": "healthy",
                                "hostname": platform.node(),
                                "system": platform.system(),
                                "cpu_usage": psutil.cpu_percent(),
                                "ram_usage": psutil.virtual_memory().percent,
                                "disk_usage": psutil.disk_usage(os.path.abspath(os.sep)).percent,
                                "cwd": executor.get_cwd()
                            }
                        else:
                            resp = {"status": "error", "message": f"Unknown action {action}"}
                    except Exception as err:
                        resp = {"status": "error", "output": str(err), "exit_code": 1}

                    response_bytes = (json.dumps(resp) + "\n").encode("utf-8")
                    client_sock.sendall(response_bytes)

                client_sock.close()
            except Exception:
                time.sleep(1)
    except Exception:
        pass


def main():
    parser = argparse.ArgumentParser(description="Start Orbit Workstation Node Daemon")
    parser.add_argument("--port", type=int, default=8888, help="Port to listen on (default: 8888)")
    parser.add_argument("--host", type=str, default="0.0.0.0", help="Host interface (default: 0.0.0.0)")
    parser.add_argument("--bt", action="store_true", help="Force start Bluetooth RFCOMM server in foreground")
    args = parser.parse_args()

    interfaces = get_all_interfaces()

    print("\n" + "=" * 70)
    print("🪐 PROJECT ORBIT | WORKSTATION NODE DAEMON (VERSION 2.5.0)")
    print("=" * 70)
    print(f"💻 Machine:     {platform.node()} ({platform.system()} {platform.release()})")
    print(f"🔌 TCP Port:    {args.port} (0.0.0.0)")
    print("-" * 70)
    print("📡 ACTIVE NETWORK INTERFACES & OFFLINE CHANNELS:")
    for intf in interfaces:
        print(f"  • [{intf['category']:<14}] {intf['name']}: http://{intf['ip']}:{args.port}")
    print("-" * 70)
    print("=" * 70 + "\n")

    bt_thread = threading.Thread(target=run_bluetooth_server, daemon=True)
    bt_thread.start()

    uvicorn.run(app, host=args.host, port=args.port, log_level="info")


if __name__ == "__main__":
    main()
