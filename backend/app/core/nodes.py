################################################################################
# FILE: backend/app/core/nodes.py
# VERSION: 1.0.0 | SYSTEM: Orbit Decentralized Node Framework
# IDENTITY: Multi-node workstation routing and agent monitoring management
################################################################################

import os
import httpx
import logging

logger = logging.getLogger("OrbitNodes")

class BaseNode:
    def __init__(self, name: str, node_type: str = "remote"):
        self.name = name
        self.node_type = node_type

    async def get_status(self) -> dict:
        raise NotImplementedError

    async def execute(self, command: str) -> dict:
        raise NotImplementedError

class HuggingFaceSpaceNode(BaseNode):
    def __init__(self, name: str, space_url: str):
        super().__init__(name, "huggingface")
        self.space_url = space_url.rstrip("/")

    async def get_status(self) -> dict:
        try:
            async with httpx.AsyncClient(timeout=4.0) as client:
                # Call local /health endpoint or space state
                resp = await client.get(f"{self.space_url}/health")
                if resp.status_code == 200:
                    return {
                        "status": "healthy",
                        "telemetry": resp.json().get("telemetry", {"cpu": 12.5, "ram": 45.0, "disk": 22.1, "cwd": "/workspace"})
                    }
                return {"status": f"unhealthy (status {resp.status_code})", "telemetry": {"cpu": 0, "ram": 0, "disk": 0, "cwd": "unknown"}}
        except Exception as e:
            return {"status": f"offline: {str(e)}", "telemetry": {"cpu": 0, "ram": 0, "disk": 0, "cwd": "unknown"}}

    async def execute(self, command: str) -> dict:
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                resp = await client.post(f"{self.space_url}/api/v1/terminal/execute", json={"command": command})
                return resp.json()
        except Exception as e:
            return {"status": "error", "message": str(e)}

class RemoteAgentNode(BaseNode):
    def __init__(self, name: str, host: str, port: int = 8888):
        super().__init__(name, "agent")
        self.host = host
        self.port = port

    async def get_status(self) -> dict:
        try:
            async with httpx.AsyncClient(timeout=3.0) as client:
                resp = await client.get(f"http://{self.host}:{self.port}/status")
                return resp.json()
        except Exception as e:
            return {"status": "offline", "telemetry": {"cpu": 0, "ram": 0, "disk": 0, "cwd": "unknown"}}

    async def execute(self, command: str) -> dict:
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                resp = await client.post(f"http://{self.host}:{self.port}/execute", json={"command": command})
                return resp.json()
        except Exception as e:
            return {"status": "error", "message": str(e)}

class NodeClusterManager:
    def __init__(self):
        self.nodes = {
            "HF_Space_Node": HuggingFaceSpaceNode("HF_Space_Node", "https://huggingface.co/spaces/Julius-606/ai_terminal_pilot")
        }
        self.active_node_name = "HF_Space_Node"

    def add_node(self, name: str, host: str, port: int = 8888):
        self.nodes[name] = RemoteAgentNode(name, host, port)

    def select_node(self, name: str):
        if name in self.nodes:
            self.active_node_name = name

    def get_active(self) -> BaseNode:
        return self.nodes[self.active_node_name]

cluster_manager = NodeClusterManager()
