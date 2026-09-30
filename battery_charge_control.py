#!/usr/bin/env python3
"""
Battery Charging Control - Disable laptop charging even when plugged in.
Supports Lenovo, Dell, and other manufacturers with charge limiting.
"""

import tkinter as tk
from tkinter import messagebox, ttk
import subprocess
import os
import sys
import threading


class BatteryChargeControl:
    def __init__(self, root):
        self.root = root
        self.root.title("Battery Charge Control")
        self.root.geometry("300x400")
        self.root.resizable(False, False)
        self.charging_disabled = False
        self.polling = False
        
        # Center window on screen
        self.root.update_idletasks()
        width = self.root.winfo_width()
        height = self.root.winfo_height()
        x = (self.root.winfo_screenwidth() // 2) - (width // 2)
        y = (self.root.winfo_screenheight() // 2) - (height // 2)
        self.root.geometry(f'{width}x{height}+{x}+{y}')
        
        self.root.configure(bg='#f0f0f0')
        self.root.protocol("WM_DELETE_WINDOW", self.on_closing)
        
        # Title
        title = tk.Label(
            self.root,
            text="Battery Charge Control",
            font=("Arial", 12, "bold"),
            bg='#f0f0f0'
        )
        title.pack(pady=10)
        
        # Status frame
        status_frame = tk.LabelFrame(
            self.root,
            text="Battery Status",
            bg='#f0f0f0',
            font=("Arial", 10)
        )
        status_frame.pack(padx=10, pady=10, fill=tk.BOTH, expand=True)
        
        # Battery percentage
        self.battery_label = tk.Label(
            status_frame,
            text="Charging: ---%",
            font=("Arial", 11, "bold"),
            bg='#f0f0f0'
        )
        self.battery_label.pack(pady=5)
        
        # Charging status
        self.status_label = tk.Label(
            status_frame,
            text="Status: Detecting...",
            font=("Arial", 10),
            bg='#f0f0f0'
        )
        self.status_label.pack(pady=5)
        
        # Control frame
        control_frame = tk.LabelFrame(
            self.root,
            text="Charging Control",
            bg='#f0f0f0',
            font=("Arial", 10)
        )
        control_frame.pack(padx=10, pady=10, fill=tk.BOTH, expand=True)
        
        # Disable charging button
        self.disable_btn = tk.Button(
            control_frame,
            text="DISABLE CHARGING",
            command=self.disable_charging,
            width=20,
            bg='#f44336',
            fg='white',
            font=("Arial", 10, "bold"),
            activebackground='#da190b'
        )
        self.disable_btn.pack(pady=8)
        
        # Enable charging button
        self.enable_btn = tk.Button(
            control_frame,
            text="ENABLE CHARGING",
            command=self.enable_charging,
            width=20,
            bg='#4CAF50',
            fg='white',
            font=("Arial", 10, "bold"),
            activebackground='#45a049',
            state=tk.DISABLED
        )
        self.enable_btn.pack(pady=8)
        
        # Charge limit spinbox
        limit_frame = tk.Frame(control_frame, bg='#f0f0f0')
        limit_frame.pack(pady=10)
        
        tk.Label(limit_frame, text="Charge Limit:", bg='#f0f0f0', font=("Arial", 9)).pack(side=tk.LEFT, padx=5)
        self.limit_var = tk.StringVar(value="80")
        limit_spin = tk.Spinbox(
            limit_frame,
            from_=20,
            to=100,
            textvariable=self.limit_var,
            width=5,
            font=("Arial", 10)
        )
        limit_spin.pack(side=tk.LEFT, padx=5)
        
        tk.Label(limit_frame, text="%", bg='#f0f0f0', font=("Arial", 9)).pack(side=tk.LEFT)
        
        # Set limit button
        set_limit_btn = tk.Button(
            control_frame,
            text="SET CHARGE LIMIT",
            command=self.set_charge_limit,
            width=20,
            bg='#2196F3',
            fg='white',
            font=("Arial", 9, "bold"),
            activebackground='#0b7dda'
        )
        set_limit_btn.pack(pady=8)
        
        # Info label
        self.info_label = tk.Label(
            self.root,
            text="Note: Charging control depends on your laptop manufacturer.",
            font=("Arial", 8),
            bg='#f0f0f0',
            fg='#666',
            wraplength=280,
            justify=tk.LEFT
        )
        self.info_label.pack(pady=5, padx=10)
        
        # Start battery monitoring
        self.update_battery_status()
    
    def get_battery_info(self):
        """Get current battery percentage and charging status."""
        try:
            # Use Windows battery report
            result = subprocess.run(
                ["powershell", "-Command", 
                 "Get-WmiObject -Class Win32_Battery | Select-Object -ExpandProperty EstimatedChargeRemaining"],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0 and result.stdout.strip():
                percentage = int(result.stdout.strip())
                return percentage
        except:
            pass
        
        try:
            # Fallback: Get battery status via wmic
            result = subprocess.run(
                ["wmic", "path", "win32_battery", "get", "estimatedchargeremaining"],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0:
                lines = result.stdout.strip().split('\n')
                if len(lines) > 1:
                    percentage = int(lines[1].strip())
                    return percentage
        except:
            pass
        
        return None
    
    def is_plugged_in(self):
        """Check if laptop is plugged in."""
        try:
            result = subprocess.run(
                ["powershell", "-Command",
                 "Get-WmiObject -Class Win32_SystemPowerStatus | Select-Object -ExpandProperty PowerLineStatus"],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0:
                status = int(result.stdout.strip())
                return status == 1  # 1 = AC power, 0 = battery
        except:
            pass
        return False
    
    def update_battery_status(self):
        """Update battery status display."""
        try:
            percentage = self.get_battery_info()
            plugged_in = self.is_plugged_in()
            
            if percentage is not None:
                self.battery_label.config(text=f"Battery: {percentage}%")
            
            if plugged_in:
                status_text = "Status: Plugged In (AC Power)"
                status_color = "#4CAF50"
            else:
                status_text = "Status: On Battery"
                status_color = "#FF9800"
            
            self.status_label.config(text=status_text, fg=status_color)
        except Exception as e:
            self.status_label.config(text="Status: Error reading", fg="#f44336")
        
        # Schedule next update
        if self.polling:
            self.root.after(2000, self.update_battery_status)
    
    def disable_charging(self):
        """Disable laptop charging."""
        self.polling = True
        success = False
        
        # Try Lenovo method (most common)
        if self.try_lenovo_disable():
            success = True
        # Try Dell method
        elif self.try_dell_disable():
            success = True
        # Try generic ACPI method
        elif self.try_acpi_disable():
            success = True
        else:
            messagebox.showwarning(
                "Limited Support",
                "Your laptop may not support direct charging control.\n\n"
                "Try:\n"
                "• Lenovo: Use 'Lenovo Vantage' app\n"
                "• Dell: Use 'Dell Power Manager' app\n"
                "• HP: Use 'HP Sure Admin' app\n"
                "• Generic: Unplug the charger cable"
            )
            return
        
        if success:
            self.charging_disabled = True
            self.disable_btn.config(state=tk.DISABLED)
            self.enable_btn.config(state=tk.NORMAL)
            messagebox.showinfo("Success", "Charging has been disabled!\nBattery will stop charging even if plugged in.")
    
    def enable_charging(self):
        """Enable laptop charging."""
        try:
            # Try to restore charging
            subprocess.run(
                ["powercfg", "/setacvalueindex", "SCHEME_CURRENT", "sub_battery", "batterychargelimit", "100"],
                capture_output=True,
                timeout=5
            )
            subprocess.run(["powercfg", "/setactive", "SCHEME_CURRENT"], capture_output=True, timeout=5)
        except:
            pass
        
        self.charging_disabled = False
        self.disable_btn.config(state=tk.NORMAL)
        self.enable_btn.config(state=tk.DISABLED)
        messagebox.showinfo("Enabled", "Charging has been enabled.")
    
    def try_lenovo_disable(self):
        """Try to disable charging on Lenovo laptops."""
        try:
            # Lenovo battery charging threshold
            subprocess.run(
                ["powercfg", "/setacvalueindex", "SCHEME_CURRENT", "sub_battery", "batterychargelimit", "1"],
                capture_output=True,
                timeout=5,
                check=False
            )
            subprocess.run(["powercfg", "/setactive", "SCHEME_CURRENT"], capture_output=True, timeout=5)
            return True
        except:
            return False
    
    def try_dell_disable(self):
        """Try to disable charging on Dell laptops."""
        try:
            # Dell battery management via WMI
            cmd = (
                "powershell -Command "
                "\"Get-WmiObject -Namespace root\\\\dcim\\\\sysman -Class DCIM_BatteryManagementSetting | "
                "Invoke-WmiMethod -Name SetAttributes -ArgumentList @($null, @{'ChargingEnabled'=$false})\""
            )
            subprocess.run(cmd, shell=True, capture_output=True, timeout=5, check=False)
            return True
        except:
            return False
    
    def try_acpi_disable(self):
        """Try generic ACPI method to disable charging."""
        try:
            subprocess.run(
                ["powercfg", "/setacvalueindex", "SCHEME_CURRENT", "sub_battery", "batterychargelimit", "0"],
                capture_output=True,
                timeout=5,
                check=False
            )
            subprocess.run(["powercfg", "/setactive", "SCHEME_CURRENT"], capture_output=True, timeout=5)
            return True
        except:
            return False
    
    def set_charge_limit(self):
        """Set charging limit to a specific percentage."""
        try:
            limit = int(self.limit_var.get())
            if limit < 20 or limit > 100:
                messagebox.showerror("Invalid", "Charge limit must be between 20% and 100%")
                return
            
            # Set via powercfg
            subprocess.run(
                ["powercfg", "/setacvalueindex", "SCHEME_CURRENT", "sub_battery", "batterychargelimit", str(limit)],
                capture_output=True,
                timeout=5,
                check=False
            )
            subprocess.run(["powercfg", "/setactive", "SCHEME_CURRENT"], capture_output=True, timeout=5)
            
            messagebox.showinfo("Success", f"Charging limit set to {limit}%")
        except Exception as e:
            messagebox.showerror("Error", f"Failed to set charge limit: {str(e)}")
    
    def on_closing(self):
        """Clean up on window close."""
        self.polling = False
        self.root.destroy()


def main():
    root = tk.Tk()
    app = BatteryChargeControl(root)
    root.mainloop()


if __name__ == "__main__":
    main()
