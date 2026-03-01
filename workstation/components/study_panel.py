# ==========================================
# IDENTITY: The Syllabus Tracker / Study Panel
# FILEPATH: workstation/components/study_panel.py
# COMPONENT: Workstation UI Component
# ROLE: The CustomTkinter widget that tells you what Med topics you are avoiding.
# VIBE: "Stop looking at the 1-minute chart and open the Pharmacology book." 🩺📚
# ==========================================

import customtkinter as ctk

class MedScholarPanel(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        
        self.configure(fg_color="#1a1a1a", corner_radius=10)

        self.title_label = ctk.CTkLabel(self, text="🩺 Med-Scholar", font=ctk.CTkFont(size=18, weight="bold"), text_color="#FF3366")
        self.title_label.grid(row=0, column=0, padx=15, pady=(15, 5), sticky="w")

        self.status_label = ctk.CTkLabel(self, text="Next Up: Pharmacology", font=ctk.CTkFont(size=16))
        self.status_label.grid(row=1, column=0, padx=15, pady=5, sticky="w")

        self.rot_label = ctk.CTkLabel(self, text="Brain Rot Level: MID", text_color="orange")
        self.rot_label.grid(row=2, column=0, padx=15, pady=(5, 15), sticky="w")

        self.action_button = ctk.CTkButton(self, text="Mark Completed", fg_color="#8A2BE2", hover_color="#5c1c96")
        self.action_button.grid(row=3, column=0, padx=15, pady=(0, 15), sticky="w")

    def update_task(self, subject, level):
        self.status_label.configure(text=f"Next Up: {subject}")
        
        color = "green" if level == "chill" else ("orange" if level == "mid" else "red")
        self.rot_label.configure(text=f"Brain Rot Level: {level.upper()}", text_color=color)