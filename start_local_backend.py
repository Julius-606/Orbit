#!/usr/bin/env python3
################################################################################
# FILE: start_local_backend.py
# IDENTITY: Local Orbit Backend Launcher
# PURPOSE: One-step launcher to run the full Orbit backend locally on your laptop.
################################################################################

import os
import sys
import socket
import platform
import subprocess


def get_local_ip() -> str:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip


def main():
    root_dir = os.path.dirname(os.path.abspath(__file__))
    backend_dir = os.path.join(root_dir, "backend")

    if not os.path.exists(backend_dir):
        print(f"❌ Error: Backend folder not found at {backend_dir}")
        sys.exit(1)

    os.chdir(backend_dir)
    sys.path.insert(0, backend_dir)

    local_ip = get_local_ip()
    port = 8000

    print("\n" + "=" * 65)
    print("🪐 STARTING PROJECT ORBIT LOCAL BACKEND")
    print("=" * 65)
    print(f"💻 Machine:     {platform.node()} ({platform.system()})")
    print(f"📡 Local IP:    {local_ip}")
    print(f"🔌 Port:        {port}")
    print(f"🌐 Local URL:   http://{local_ip}:{port}")
    print(f"📚 Swagger UI:  http://{local_ip}:{port}/docs")
    print("-" * 65)
    print("📱 HOW TO CONNECT YOUR PHONE TO THIS LAPTOP:")
    print("")
    print("Option 1 (Same Wi-Fi Network):")
    print("  1. In the Pocket Orbit Android app, go to Settings.")
    print("  2. In 'Backend Server / Space URL', enter:")
    print(f"     http://{local_ip}:{port}")
    print("  3. Tap 'Save & Reconnect'. Done! Your phone now commands this laptop.")
    print("")
    print("Option 2 (Connecting via Main Hugging Face Space from anywhere):")
    print("  1. In another terminal on your laptop, start a Cloudflare tunnel:")
    print(f"     cloudflared tunnel --url http://localhost:{port}")
    print("  2. Copy the resulting 'https://....trycloudflare.com' URL.")
    print("  3. In the Android app -> Terminal -> Cluster Nodes -> Add Node:")
    print("     - Name: My_Laptop")
    print("     - Host: https://your-tunnel-url.trycloudflare.com")
    print("     - Port: 443 (or default)")
    print("=" * 65 + "\n")

    try:
        import uvicorn
        uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)
    except ImportError:
        print("⚠️ uvicorn not installed in current python environment.")
        print("Running: pip install -r requirements.txt ...")
        subprocess.run([sys.executable, "-m", "pip", "install", "-r", "requirements.txt"])
        import uvicorn
        uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)


if __name__ == "__main__":
    main()
