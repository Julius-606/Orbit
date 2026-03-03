# ==========================================
# IDENTITY: The Command Center / Ubuntu UI
# FILE VERSION: 3.1.0
# FILEPATH: workstation/main.py
# COMPONENT: Laptop Frontend (CustomTkinter)
# ROLE: The main dashboard you see when you open your laptop.
# VIBE: Looks like a hacker movie, acts like a strict Med School professor. 💻👨‍⚕️
# ==========================================


import customtkinter as ctk
from src.ui.panels.forex_guardian import ForexGuardianPanel
from src.ui.panels.med_scholar import MedScholarPanel
from src.hooks.vscode_sync import VSCodeSyncHook
import threading
import time

class OrbitWorkstation(ctk.CTk):
    def __init__(self):
        super().__init__()

        # --- Window Config ---
        self.title("ORBIT | COMMAND CENTER v3.0")
        self.geometry("1100x700")
        ctk.set_appearance_mode("dark")
        ctk.set_default_color_theme("blue")

        # --- Layout Grid ---
        self.grid_columnconfigure(0, weight=1) # Sidebar/Panels
        self.grid_columnconfigure(1, weight=3) # Main Feed
        self.grid_rowconfigure(0, weight=1)

        # --- Sidebar (Modular Panels) ---
        self.sidebar = ctk.CTkFrame(self, corner_radius=0, fg_color="#1a1a1a")
        self.sidebar.grid(row=0, column=0, sticky="nsew", padx=10, pady=10)
        
        # Heading
        self.logo_label = ctk.CTkLabel(self.sidebar, text="ORBIT HUD", font=ctk.CTkFont(size=24, weight="bold"))
        self.logo_label.pack(pady=20)

        # Forex Guardian Integration
        self.forex_panel = ForexGuardianPanel(self.sidebar)
        self.forex_panel.pack(fill="x", padx=10, pady=10)

        # Med-Scholar Integration
        self.med_panel = MedScholarPanel(self.sidebar)
        self.med_panel.pack(fill="x", padx=10, pady=10)

        # --- Main Feed ---
        self.main_feed_container = ctk.CTkFrame(self, corner_radius=15)
        self.main_feed_container.grid(row=0, column=1, sticky="nsew", padx=20, pady=20)
        
        self.feed_title = ctk.CTkLabel(self.main_feed_container, text="SYSTEM_BLAST_LOG", font=ctk.CTkFont(family="Courier", size=14))
        self.feed_title.pack(anchor="w", padx=20, pady=10)

        self.feed_text = ctk.CTkTextbox(self.main_feed_container, font=ctk.CTkFont(family="Courier", size=12))
        self.feed_text.pack(expand=True, fill="both", padx=20, pady=(0, 20))
        self.feed_text.insert("0.0", "[SYSTEM] Orbit Workstation Booted...\n[SYSTEM] Connecting to VM Brain...\n")

        # --- Hooks & Listeners ---
        self.vscode_hook = VSCodeSyncHook(callback=self.log_to_feed)
        
        # Start background threads
        threading.Thread(target=self.vscode_hook.start_monitoring, daemon=True).start()
        threading.Thread(target=self.update_ui_loop, daemon=True).start()

    def log_to_feed(self, message):
        """Update the main log with new info from hooks."""
        timestamp = time.strftime("%H:%M:%S")
        self.feed_text.insert("end", f"[{timestamp}] {message}\n")
        self.feed_text.see("end")

    def update_ui_loop(self):
        """Periodic UI updates for panels if they aren't event-driven yet."""
        while True:
            # Check for blast protocol messages here in the future
            time.sleep(5)

if __name__ == "__main__":
    app = OrbitWorkstation()
    app.mainloop()