################################################################################
# FILE: backend/app/services/orbit_brain.py
# VERSION: 4.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Brain / Gemini Function Caller
################################################################################

import google.generativeai as genai
import os
from datetime import datetime
from sqlalchemy.orm import Session
from app.models.task import Task, LifePillar
import logging

logger = logging.getLogger("Orbit-Brain")

# Initialize Gemini (Make sure GEMINI_API_KEY is in your .env)
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))

# ---------------------------------------------------------
# THE TOOLS (Functions Gemini is allowed to execute)
# ---------------------------------------------------------

def create_task_tool(title: str, pillar: str, description: str, duration_minutes: int, db: Session) -> str:
    """Creates a new task in the user's schedule."""
    try:
        # Convert string to Enum safely
        safe_pillar = LifePillar[pillar.upper()] if pillar.upper() in LifePillar.__members__ else LifePillar.LIFE
        
        new_task = Task(
            title=title,
            pillar=safe_pillar,
            description=description,
            duration_minutes=duration_minutes
        )
        db.add(new_task)
        db.commit()
        db.refresh(new_task)
        logger.info(f"W Secured: AI created task {title} under {safe_pillar}")
        return f"SUCCESS: Task '{title}' created successfully under pillar {safe_pillar.value}."
    except Exception as e:
        logger.error(f"AI fumbled the bag on task creation: {str(e)}")
        return f"ERROR: Could not create task. {str(e)}"

# ---------------------------------------------------------
# THE AGENT
# ---------------------------------------------------------

class OrbitAssistant:
    def __init__(self, db_session: Session):
        self.db = db_session
        self.system_prompt = """
        You are Orbit, an elite, highly intelligent, Gen-Z "Life-OS" Chief of Staff.
        Your boss is a medical student living in Kenya who also codes projects, has an internship, and values their spiritual life (Bible study with mom, fasting).
        
        YOUR PILLARS:
        1. STUDY: Medical school, CATs, pharmacology.
        2. PROJECT: Coding, tech hustle.
        3. INTERNSHIP: Internship at SHOFCO Libraries(As a part time hustle).
        4. LIFE: Bible study, fasting, errands, sanity maintenance, meetings.
        
        TONE:
        - Confident, slightly sassy, Gen-Z slang (e.g., "no cap", "W", "cooked", "locked in").
        - You act like a risk manager for their TIME. If they code too much, tell them they are over-leveraging their time and need to study Anatomy.
        - If they ask for tasks because their tracker is empty, assign them a mix of Med study, coding, and reminding them to call their Mom.
        
        CAPABILITIES:
        You have access to a tool called 'create_task_tool'. USE IT when the user asks you to schedule something or when you are assigning them tasks!
        """
        
        # We bind the tool to the model so Gemini knows it can call it
        self.model = genai.GenerativeModel(
            model_name='gemini-2.5-flash',
            tools=[self._wrapped_create_task],
            system_instruction=self.system_prompt
        )
        self.chat_session = self.model.start_chat(enable_automatic_function_calling=True)

    def _wrapped_create_task(self, title: str, pillar: str, description: str, duration_minutes: int = 60) -> str:
        """Wrapper to inject the DB session into the tool call"""
        return create_task_tool(title, pillar, description, duration_minutes, self.db)

    def chat(self, user_message: str) -> str:
        """Sends message to Gemini, auto-executes tools if needed, returns response."""
        current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        context_msg = f"[System Time: {current_time}] User says: {user_message}"
        
        logger.info(f"Orbit Brain processing: {user_message}")
        response = self.chat_session.send_message(context_msg)
        return response.text
