################################################################################
# FILE: backend/app/core/nodes.py
# VERSION: 2.0.0 | SYSTEM: Orbit Decentralized Node & Cluster Framework
# IDENTITY: Multi-node cluster routing. Hugging Face Space is the 'Main' coordinator,
#           and external connected laptops/servers act as controllable execution nodes.
################################################################################

import os
import json
import httpx
import logging
import subprocess
import threading
import queue
import psutil
from typing import Dict, Any, List

logger = logging.getLogger("OrbitNodes")

MAIN_NODE_NAME = "Main_HF_Space"
LEGACY_NODE_ALIAS = "HF_Space_Node"

NODES_PERSISTENCE_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "cluster_nodes.json")


def clean_node_url(host: str, port: int = 8888) -> str:
    """Normalize any user-entered host, IP, or tunnel into a valid base URL."""
    h = host.strip().rstrip("/")
    if h.startswith("http://") or h.startswith("https://"):
        return h
    if ":" in h:
        # User entered e.g. 192.168.1.50:8000
        return f"http://{h}"
    return f"http://{h}:{port}"


class BaseNode:
    def __init__(self, name: str, node_type: str = "remote", host: str = "localhost", port: int = 8888):
        self.name = name
        self.node_type = node_type
        self.host = host
        self.port = port

    async def get_status(self) -> dict:
        raise NotImplementedError

    async def execute(self, command: str) -> str:
        raise NotImplementedError


class LocalNode(BaseNode):
    """The Main coordinator node running on the Hugging Face Space container itself."""
    def __init__(self, name: str = MAIN_NODE_NAME):
        super().__init__(name, "local", "localhost", 7860)
        shell = 'powershell.exe' if os.name == 'nt' else 'bash'
        self.proc = subprocess.Popen(
            [shell, '-NoExit', '-Command', '-'] if os.name == 'nt' else [shell],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
            creationflags=subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
        )
        self.out_queue = queue.Queue()
        threading.Thread(target=self._read_stream, args=(self.proc.stdout,), daemon=True).start()
        threading.Thread(target=self._read_stream, args=(self.proc.stderr,), daemon=True).start()

    def _read_stream(self, stream):
        try:
            for line in iter(stream.readline, ""):
                self.out_queue.put(line)
        except Exception:
            pass

    async def get_status(self) -> dict:
        cwd = os.getcwd()
        try:
            p = psutil.Process(self.proc.pid)
            cwd = p.cwd()
        except Exception:
            pass
        return {
            "status": "healthy",
            "is_main": True,
            "telemetry": {
                "cpu": psutil.cpu_percent(),
                "ram": psutil.virtual_memory().percent,
                "disk": psutil.disk_usage('/').percent,
                "cwd": str(cwd)
            }
        }

    async def execute(self, command: str) -> str:
        if self.proc.poll() is None:
            self.proc.stdin.write(command + "\n")
            self.proc.stdin.flush()
            import asyncio
            await asyncio.sleep(0.5)
            output = ""
            while not self.out_queue.empty():
                output += self.out_queue.get()
            return output
        return "Local terminal process has terminated."


class RemoteAgentNode(BaseNode):
    """
    A remote laptop, workstation, or server connected as an execution node.
    Supports either:
    1. An Orbit Node Daemon (lightweight agent on port 8888 with /status and /execute)
    2. A full local Orbit Backend space running on that machine (with /terminal/status and /terminal/execute)
    3. Remote tunnels (Cloudflare / ngrok / Tailscale)
    """
    def __init__(self, name: str, host: str, port: int = 8888):
        super().__init__(name, "remote_workstation", host, port)
        self.base_url = clean_node_url(host, port)

    async def get_status(self) -> dict:
        headers = {"Content-Type": "application/json"}
        try:
            async with httpx.AsyncClient(timeout=4.0) as client:
                # 1. Try Orbit backend space endpoint
                try:
                    resp = await client.get(f"{self.base_url}/terminal/status", headers=headers)
                    if resp.status_code == 200:
                        data = resp.json()
                        telem = data.get("telemetry", {})
                        return {
                            "status": "healthy",
                            "is_main": False,
                            "type": "orbit_backend",
                            "telemetry": {
                                "cpu": telem.get("cpu", 0),
                                "ram": telem.get("ram", 0),
                                "disk": telem.get("disk", 0),
                                "cwd": telem.get("cwd", "~")
                            }
                        }
                except Exception:
                    pass

                # 2. Try Node Daemon agent endpoint
                resp = await client.get(f"{self.base_url}/status", headers=headers)
                if resp.status_code == 200:
                    data = resp.json()
                    telem = data.get("telemetry", data)
                    return {
                        "status": "healthy",
                        "is_main": False,
                        "type": "node_daemon",
                        "telemetry": {
                            "cpu": telem.get("cpu", 0),
                            "ram": telem.get("ram", 0),
                            "disk": telem.get("disk", 0),
                            "cwd": telem.get("cwd", "~")
                        }
                    }
        except Exception as e:
            logger.debug(f"Status check failed for remote node '{self.name}': {e}")

        return {
            "status": "offline",
            "is_main": False,
            "telemetry": {"cpu": 0, "ram": 0, "disk": 0, "cwd": f"offline ({self.base_url})"}
        }

    async def execute(self, command: str) -> str:
        headers = {"Content-Type": "application/json"}
        payload = {"command": command}

        try:
            async with httpx.AsyncClient(timeout=25.0) as client:
                # 1. Try full Orbit backend endpoint
                try:
                    resp = await client.post(f"{self.base_url}/terminal/execute", json=payload, headers=headers)
                    if resp.status_code == 200:
                        data = resp.json()
                        return data.get("output", data.get("message", "Done."))
                except Exception:
                    pass

                # 2. Try Node Daemon agent endpoint
                resp = await client.post(f"{self.base_url}/execute", json=payload, headers=headers)
                if resp.status_code == 200:
                    data = resp.json()
                    return data.get("output", data.get("message", "Done."))

                return f"Remote node error (Status {resp.status_code}): {resp.text}"
        except Exception as e:
            return f"Remote node connection failed ({self.base_url}): {str(e)}\nMake sure your laptop backend or orbit_node_daemon is running and reachable."


class NodeClusterManager:
    def __init__(self):
        self.nodes: Dict[str, BaseNode] = {
            MAIN_NODE_NAME: LocalNode(MAIN_NODE_NAME)
        }
        self.active_node_name = MAIN_NODE_NAME
        self._load_persisted_nodes()

    def _load_persisted_nodes(self):
        try:
            if os.path.exists(NODES_PERSISTENCE_PATH):
                with open(NODES_PERSISTENCE_PATH, "r") as f:
                    saved = json.load(f)
                    for item in saved:
                        name = item.get("name")
                        host = item.get("host")
                        port = item.get("port", 8888)
                        if name and host and name not in self.nodes:
                            self.nodes[name] = RemoteAgentNode(name, host, port)
        except Exception as e:
            logger.warning(f"Could not load persisted cluster nodes: {e}")

    def _save_persisted_nodes(self):
        try:
            data = []
            for name, node in self.nodes.items():
                if isinstance(node, RemoteAgentNode):
                    data.append({
                        "name": node.name,
                        "host": node.host,
                        "port": node.port
                    })
            with open(NODES_PERSISTENCE_PATH, "w") as f:
                json.dump(data, f, indent=2)
        except Exception as e:
            logger.warning(f"Could not persist cluster nodes: {e}")

    def add_node(self, name: str, host: str, port: int = 8888):
        clean_name = name.strip()
        self.nodes[clean_name] = RemoteAgentNode(clean_name, host, port)
        self._save_persisted_nodes()

    def remove_node(self, name: str) -> bool:
        if name in self.nodes and name != MAIN_NODE_NAME and name != LEGACY_NODE_ALIAS:
            del self.nodes[name]
            if self.active_node_name == name:
                self.active_node_name = MAIN_NODE_NAME
            self._save_persisted_nodes()
            return True
        return False

    def select_node(self, name: str):
        # Resolve legacy alias
        target = MAIN_NODE_NAME if name == LEGACY_NODE_ALIAS else name
        if target in self.nodes:
            self.active_node_name = target

    def get_active(self) -> BaseNode:
        if self.active_node_name not in self.nodes:
            self.active_node_name = MAIN_NODE_NAME
        return self.nodes[self.active_node_name]

    def get_nodes_info(self) -> List[Dict[str, Any]]:
        info = []
        for name, node in self.nodes.items():
            info.append({
                "name": name,
                "is_main": (name == MAIN_NODE_NAME),
                "type": node.node_type,
                "host": node.host,
                "port": node.port,
                "is_active": (name == self.active_node_name)
            })
        return info


cluster_manager = NodeClusterManager()
