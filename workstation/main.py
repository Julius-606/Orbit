# ==========================================
# IDENTITY: The Command Center / Ubuntu UI
# FILEPATH: workstation/main.py
# COMPONENT: Laptop Frontend (CustomTkinter)
# ROLE: The main dashboard you see when you open your laptop.
# VIBE: Looks like a hacker movie, acts like a strict Med School professor. 💻👨‍⚕️
# ==========================================

import customtkinter as ctk
import threading
import asyncio
import websockets
import json

# Set the vibe to dark mode, because light mode is for sociopaths.
ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")

class OrbitWorkstation(ctk.CTk):
    def __init__(self):
        super().__init__()

        self.title("Project Orbit: Jarvis Protocol v3.0")
        self.geometry("1000x600")

        # --- Sidebar ---
        self.sidebar_frame = ctk.CTkFrame(self, width=200, corner_radius=0)
        self.sidebar_frame.grid(row=0, column=0, sticky="nsew")
        
        self.logo_label = ctk.CTkLabel(self.sidebar_frame, text="🪐 ORBIT", font=ctk.CTkFont(size=20, weight="bold"))
        self.logo_label.grid(row=0, column=0, padx=20, pady=(20, 10))

        # --- Main Feed ---
        self.main_feed = ctk.CTkTextbox(self, width=750, height=500)
        self.main_feed.grid(row=0, column=1, padx=20, pady=20)
        self.main_feed.insert("0.0", "System Initialized. Waiting for Blast Protocol...\n")
        self.main_feed.configure(state="disabled")

        # Start the background WebSocket listener to catch VM blasts
        threading.Thread(target=self.start_blast_listener, daemon=True).start()

    def log_message(self, message):
        self.main_feed.configure(state="normal")
        self.main_feed.insert("end", f"> {message}\n")
        self.main_feed.configure(state="disabled")
        self.main_feed.yview("end") # Auto-scroll to bottom

    def start_blast_listener(self):
        """Runs the WebSocket client in a separate thread so the UI doesn't freeze"""
        async def listen():
            uri = "ws://localhost:8000/ws/blast" # Assuming VM is local for now
            try:
                async with websockets.connect(uri) as websocket:
                    self.log_message("Connected to Orbit Core. Let's get this bread.")
                    while True:
                        response = await websocket.recv()
                        data = json.loads(response)
                        
                        if data.get("type") == "TRADE_UPDATE":
                            pnl = data['data']['pnl']
                            self.log_message(f"[FOREX ALERT]: {data['data']['pair']} - {pnl}. {data['data']['message']}")
            except Exception as e:
                self.log_message(f"Connection lost. Is the Docker VM running? Error: {e}")

        asyncio.run(listen())

if __name__ == "__main__":
    app = OrbitWorkstation()
    app.mainloop()