"""
IDENTITY: forex_guardian.py
PATH: workstation/src/ui/panels/forex_guardian.py
FILE VERSION: 1.0.1
STATUS: JARVIS ERA v3.0
DESCRIPTION: The risk management HUD for the Ubuntu Workstation.
Monitors MT5 positions and equity in real-time. 
Features a 'Panic Button' for emergency flatting of positions.
Dual-Blast notification logic.
"""

import customtkinter as ctk
import random # Placeholder for real MT5 bridge data integration

class ForexGuardianPanel(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)

        # UI Styling - Dark Mode Hacker Aesthetic
        self.configure(fg_color="#1e1e1e", corner_radius=10, border_width=1, border_color="#333333")

        # --- Header ---
        self.title_label = ctk.CTkLabel(
            self, 
            text="📉 FOREX GUARDIAN", 
            font=ctk.CTkFont(size=18, weight="bold"), 
            text_color="#00FFA3" # Neon Mint for that profit vibe
        )
        self.title_label.grid(row=0, column=0, padx=15, pady=(15, 5), sticky="w")

        # --- Market Status & Equity ---
        self.equity_label = ctk.CTkLabel(
            self, 
            text="Equity: $0.00", 
            font=ctk.CTkFont(size=16, weight="bold")
        )
        self.equity_label.grid(row=1, column=0, padx=15, pady=2, sticky="w")

        self.drawdown_label = ctk.CTkLabel(
            self, 
            text="DD: 0.00%", 
            font=ctk.CTkFont(family="Courier", size=12),
            text_color="gray"
        )
        self.drawdown_label.grid(row=2, column=0, padx=15, pady=2, sticky="w")

        # --- Active Trades List (Scrollable for scalability) ---
        self.trades_frame = ctk.CTkFrame(self, fg_color="#121212", corner_radius=5)
        self.trades_frame.grid(row=3, column=0, padx=10, pady=10, sticky="nsew")
        
        self.trades_info = ctk.CTkLabel(
            self.trades_frame, 
            text="NO OPEN POSITIONS", 
            font=ctk.CTkFont(size=11, slant="italic"),
            text_color="#555555"
        )
        self.trades_info.pack(pady=10, padx=10)

        # --- Risk Controls ---
        self.panic_button = ctk.CTkButton(
            self, 
            text="CLOSE ALL POSITIONS", 
            fg_color="#FF3131", 
            hover_color="#b32222",
            font=ctk.CTkFont(size=12, weight="bold"),
            command=self.emergency_flat
        )
        self.panic_button.grid(row=4, column=0, padx=15, pady=(0, 15), sticky="ew")

        # Initial data simulation loop
        self.update_market_data()

    def update_market_data(self):
        """
        Polls the MT5 Bridge for current stats.
        Connects to src/hooks/mt5_bridge.py via the VM Brain.
        """
        # Mocking data for the demo - replace with real API calls to Backend
        balance = 10500.00 # Syncing with your snapshot's default
        floating_pnl = random.uniform(-500.0, 800.0)
        equity = balance + floating_pnl
        drawdown = (abs(min(0, floating_pnl)) / balance) * 100

        # Update UI Elements
        self.equity_label.configure(
            text=f"Equity: ${equity:,.2f}",
            text_color="#00FFA3" if floating_pnl >= 0 else "#FF3131"
        )
        
        dd_color = "gray" if drawdown < 5 else ("orange" if drawdown < 10 else "#FF3131")
        self.drawdown_label.configure(text=f"DD: {drawdown:.2f}%", text_color=dd_color)

        # Dual-Blast Logic: If DD is spicy, notify the workstation feed
        if drawdown > 5.0:
            self.broadcast_risk_alert(f"HIGH DRAWDOWN DETECTED: {drawdown:.2f}% Check XAUUSD positions.")

        # Refresh loop (Every 2 seconds for high-frequency pairs like Gold)
        self.after(2000, self.update_market_data)

    def broadcast_risk_alert(self, message):
        """Sends an alert to the main workstation log and potentially the phone."""
        try:
            # Reaching up to the Main App log via parent reference
            # In production, this would also hit the Backend Blast service
            self.master.master.log_to_feed(f"🛑 [FOREX_GUARDIAN] {message}")
        except Exception:
            # Fallback if UI hierarchy is still being initialized
            print(f"DEBUG_RISK: {message}")

    def emergency_flat(self):
        """
        The 'JARVIS_PANIC' protocol.
        Sends a high-priority signal to the VM Brain to wipe all active MT5 trades.
        """
        self.broadcast_risk_alert("MANUAL PANIC TRIGGERED: Closing all positions...")
        
        # UI Feedback for user
        self.equity_label.configure(text_color="white")
        self.trades_info.configure(text="FLATTING ALL TRADES...", text_color="#FF3131")
        
        # This is where we'd call the backend:
        # requests.post(f"{VM_URL}/api/v1/forex/panic", headers={"Authorization": f"Bearer {TOKEN}"})
        
        # Logic to clear the trade view after a delay
        self.after(3000, lambda: self.trades_info.configure(text="NO OPEN POSITIONS", text_color="#555555"))