# ==========================================
# IDENTITY: The Bloomberg Terminal / Forex Panel
# FILEPATH: workstation/components/forex_panel.py
# COMPONENT: Workstation UI Component
# ROLE: The CustomTkinter widget that strictly displays your MT5 stats.
# VIBE: Green numbers only. Red numbers mean we touch grass. 📈🌿
# ==========================================

import customtkinter as ctk

class ForexGuardianPanel(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        
        # Give it a nice dark gray background
        self.configure(fg_color="#1a1a1a", corner_radius=10)

        # Title
        self.title_label = ctk.CTkLabel(self, text="📈 Forex Guardian", font=ctk.CTkFont(size=18, weight="bold"), text_color="#00FFCC")
        self.title_label.grid(row=0, column=0, padx=15, pady=(15, 5), sticky="w")

        # Equity Display
        self.equity_label = ctk.CTkLabel(self, text="Equity: $10,500.00", font=ctk.CTkFont(size=24, weight="bold"))
        self.equity_label.grid(row=1, column=0, padx=15, pady=5, sticky="w")

        # Margin Level Warning
        self.margin_label = ctk.CTkLabel(self, text="Margin Level: 1200% (Safe)", text_color="gray")
        self.margin_label.grid(row=2, column=0, padx=15, pady=5, sticky="w")

        # Active Pair
        self.pair_label = ctk.CTkLabel(self, text="Monitoring: XAUUSD", text_color="#8A2BE2")
        self.pair_label.grid(row=3, column=0, padx=15, pady=(5, 15), sticky="w")

    def update_stats(self, equity, margin_level, pair):
        """Called when a WebSockets blast comes in."""
        self.equity_label.configure(text=f"Equity: ${equity}")
        
        if margin_level < 300:
            self.margin_label.configure(text=f"Margin Level: {margin_level}% (DANGER)", text_color="#FF3366")
        else:
            self.margin_label.configure(text=f"Margin Level: {margin_level}% (Safe)", text_color="gray")
            
        self.pair_label.configure(text=f"Monitoring: {pair}")