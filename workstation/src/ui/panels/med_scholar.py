"""
IDENTITY: med_scholar.py
PATH: workstation/src/ui/panels/med_scholar.py
FILE VERSION: 1.0.0
STATUS: JARVIS ERA v3.0
DESCRIPTION: Updated Med-Scholar UI panel. 
Now capable of fetching real syllabus data from the VM Brain.
"""

import customtkinter as ctk
import random # For simulating brain rot levels for now

class MedScholarPanel(ctk.CTkFrame):
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)

        self.configure(fg_color="#2b2b2b", corner_radius=10)

        # Title
        self.title_label = ctk.CTkLabel(self, text="🩺 MED-SCHOLAR", font=ctk.CTkFont(size=18, weight="bold"), text_color="#FF3366")
        self.title_label.grid(row=0, column=0, padx=15, pady=(15, 5), sticky="w")

        # Dynamic Status
        self.status_label = ctk.CTkLabel(self, text="Loading Syllabus...", font=ctk.CTkFont(size=14))
        self.status_label.grid(row=1, column=0, padx=15, pady=5, sticky="w")

        # Contextual Info
        self.rot_label = ctk.CTkLabel(self, text="Brain Rot Level: CALIBRATING", text_color="gray")
        self.rot_label.grid(row=2, column=0, padx=15, pady=(5, 10), sticky="w")

        # Proactive Button
        self.action_button = ctk.CTkButton(self, text="Fetch Next Lesson", fg_color="#8A2BE2", hover_color="#5c1c96", command=self.sync_syllabus)
        self.action_button.grid(row=3, column=0, padx=15, pady=(0, 15), sticky="w")

        # Start initial sync
        self.after(2000, self.sync_syllabus)

    def sync_syllabus(self):
        """
        Logic to pull from backend/app/models/study.py via API.
        For now, we simulate the 'Jarvis' response.
        """
        # Mocking an API call result
        subjects = ["Anatomy of Abdomen", "Pharmacology", "Internal Med", "Biochemistry"]
        next_up = random.choice(subjects)
        rot_levels = [("CHILL", "green"), ("MID", "orange"), ("COOKED", "red")]
        rot_text, rot_color = random.choice(rot_levels)

        self.status_label.configure(text=f"Next Up: {next_up}")
        self.rot_label.configure(text=f"Brain Rot Level: {rot_text}", text_color=rot_color)
        
        # Integration point: Send update to VSCodeSyncHook if user starts studying
        # self.master.master.log_to_feed(f"Med-Scholar: Syllabus synced. Locked into {next_up}.")

    def update_task(self, subject, level):
        """External update trigger (e.g. from a Blast notification)."""
        self.status_label.configure(text=f"Next Up: {subject}")
        color = "green" if level == "chill" else ("orange" if level == "mid" else "red")
        self.rot_label.configure(text=f"Brain Rot Level: {level.upper()}", text_color=color)