#!/usr/bin/env python3
"""
Simple GUI application to disconnect/sleep the laptop from power input.
Creates a small window with buttons to control power states.
"""

import tkinter as tk
from tkinter import messagebox
import os
import sys


class PowerControlApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Power Control")
        self.root.geometry("200x150")
        self.root.resizable(False, False)
        
        # Center window on screen
        self.root.update_idletasks()
        width = self.root.winfo_width()
        height = self.root.winfo_height()
        x = (self.root.winfo_screenwidth() // 2) - (width // 2)
        y = (self.root.winfo_screenheight() // 2) - (height // 2)
        self.root.geometry(f'{width}x{height}+{x}+{y}')
        
        # Style
        self.root.configure(bg='#f0f0f0')
        
        # Title label
        title = tk.Label(
            self.root, 
            text="Power Control", 
            font=("Arial", 12, "bold"),
            bg='#f0f0f0'
        )
        title.pack(pady=10)
        
        # Sleep button
        sleep_btn = tk.Button(
            self.root,
            text="Sleep",
            command=self.sleep_laptop,
            width=15,
            bg='#4CAF50',
            fg='white',
            font=("Arial", 10, "bold"),
            activebackground='#45a049'
        )
        sleep_btn.pack(pady=5)
        
        # Hibernate button
        hibernate_btn = tk.Button(
            self.root,
            text="Hibernate",
            command=self.hibernate_laptop,
            width=15,
            bg='#2196F3',
            fg='white',
            font=("Arial", 10, "bold"),
            activebackground='#0b7dda'
        )
        hibernate_btn.pack(pady=5)
        
        # Exit button
        exit_btn = tk.Button(
            self.root,
            text="Exit",
            command=self.root.quit,
            width=15,
            bg='#f44336',
            fg='white',
            font=("Arial", 10, "bold"),
            activebackground='#da190b'
        )
        exit_btn.pack(pady=5)
    
    def sleep_laptop(self):
        """Put the laptop into sleep mode."""
        try:
            os.system("rundll32.exe powrprof.dll,SetSuspendState 0,1,0")
            self.root.destroy()
        except Exception as e:
            messagebox.showerror("Error", f"Failed to sleep: {str(e)}")
    
    def hibernate_laptop(self):
        """Put the laptop into hibernation mode."""
        try:
            os.system("shutdown /h")
            self.root.destroy()
        except Exception as e:
            messagebox.showerror("Error", f"Failed to hibernate: {str(e)}")


def main():
    root = tk.Tk()
    app = PowerControlApp(root)
    root.mainloop()


if __name__ == "__main__":
    main()
