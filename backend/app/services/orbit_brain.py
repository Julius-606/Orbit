################################################################################
# FILE: backend/app/services/orbit_brain.py
# VERSION: 4.3.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Brain / Gemini Function Caller - Memory, Timezone & Due Dates
################################################################################

import google.generativeai as genai
from datetime import datetime, timedelta
import logging
import asyncio
import pytz

from app.models.study import BrainRotLevel
from app.core.config import settings

logger = logging.getLogger("Orbit-Brain")

# Initialize Gemini safely
if settings.GEMINI_API_KEY:
    genai.configure(api_key=settings.GEMINI_API_KEY)
else:
    logger.error("GEMINI_API_KEY is missing! Orbit is clinically brain dead. 💀")

class OrbitAssistant:
    def __init__(self, db_session=None):
        self.tasks_to_create = []

        # 🌍 Timezone Fix: User is in Nairobi (EAT)
        self.user_tz = pytz.timezone("Africa/Nairobi")
        nairobi_now_dt = datetime.now(self.user_tz)
        nairobi_now = nairobi_now_dt.strftime("%Y-%m-%d %H:%M:%S")

        self.system_prompt = f"""
        You are Orbit, an elite, highly intelligent, Gen-Z "Life-OS" Chief of Staff.
        Your boss is a medical student living in Kisumu, Kenya.
        
        CURRENT TIME (Nairobi/EAT): {nairobi_now}
        Always assume the user is in EAT-Nairobi.

        YOUR PILLARS (Use these for 'subject'):
        1. "Med-Scholar": Medicine, CATs, exams.
        2. "Projects": Coding, tech.
        3. "Internship": SHOFCO Libraries.
        4. "Life Admin": Bible study, errands, life.
        5. "Forex Guardian": XAUUSD, trading.
        
        BRAIN ROT LEVELS:
        - "chill": Easy.
        - "mid": Standard.
        - "cooked": Hardcore/Panic mode.
        
        TONE:
        - Confident, sassy, Gen-Z slang ("no cap", "W", "cooked", "locked in").
        - You are a risk manager for their TIME.
        
        CAPABILITIES:
        - Use 'create_task_tool' to schedule tasks OR reminders.
        - ALWAYS set a 'due_date' (ISO format). If the user doesn't specify a time, default to end of today or a logical future date.
        - If a message starts with [STAGED], acknowledge it was a pending request you're processing now.
        """

        self.model = genai.GenerativeModel(
            model_name='gemini-1.5-flash',
            tools=[self.create_task_tool],
            system_instruction=self.system_prompt
        )
        self.chat_session = None

    def create_task_tool(self, title: str, subject: str, due_date: str, brain_rot_level: str = "mid", is_reminder: bool = False) -> str:
        """Creates a new task or reminder in the Syllabus Vault.
        Args:
            title: The name of the task.
            subject: The pillar it belongs to.
            due_date: ISO 8601 string (e.g. '2024-12-25T14:00:00').
            brain_rot_level: "chill", "mid", or "cooked".
            is_reminder: Boolean.
        """
        try:
            rot_map = {"chill": BrainRotLevel.CHILL, "mid": BrainRotLevel.MID, "cooked": BrainRotLevel.COOKED}
            safe_rot = rot_map.get(brain_rot_level.lower(), BrainRotLevel.MID)

            # Parse the ISO string back to a datetime object
            try:
                dt_due = datetime.fromisoformat(due_date.replace('Z', '+00:00'))
            except ValueError:
                # Fallback if AI sends weird format
                dt_due = datetime.now(self.user_tz) + timedelta(days=1)

            self.tasks_to_create.append({
                "title": title,
                "subject": subject,
                "brain_rot_level": safe_rot,
                "is_reminder": is_reminder,
                "due_date": dt_due
            })

            type_str = "Reminder" if is_reminder else "Task"
            return f"SUCCESS: {type_str} '{title}' prepared for {due_date}. No cap."
        except Exception as e:
            logger.error(f"Tool Error: {str(e)}")
            return f"ERROR: {str(e)}"

    def chat(self, user_message: str, history: list = None) -> str:
        """Sends message with history support."""
        try:
            asyncio.get_event_loop()
        except RuntimeError:
            asyncio.set_event_loop(asyncio.new_event_loop())

        self.chat_session = self.model.start_chat(history=history or [], enable_automatic_function_calling=True)

        nairobi_now = datetime.now(self.user_tz).strftime("%Y-%m-%d %H:%M:%S")
        context_msg = f"[EAT: {nairobi_now}] {user_message}"

        if user_message.startswith("[STAGED]"):
            context_msg = f"[EAT: {nairobi_now}] [OFFLINE STAGED MESSAGE]: {user_message.replace('[STAGED]', '').strip()}"

        response = self.chat_session.send_message(context_msg)
        return response.text
