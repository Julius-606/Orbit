################################################################################
# FILE: backend/app/services/orbit_brain.py
# VERSION: 4.1.4 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Brain / Gemini Function Caller
################################################################################

import google.generativeai as genai
from datetime import datetime
import logging
import asyncio  # 🚀 BUG FIX: We need this to pacify Gemini's gRPC threads

from app.models.study import BrainRotLevel
from app.core.config import settings

logger = logging.getLogger("Orbit-Brain")

# Initialize Gemini safely
if settings.GEMINI_API_KEY:
    genai.configure(api_key=settings.GEMINI_API_KEY)
else:
    logger.error("GEMINI_API_KEY is missing! Orbit is clinically brain dead. 💀")

# ---------------------------------------------------------
# THE AGENT
# ---------------------------------------------------------

class OrbitAssistant:
    def __init__(self, db_session=None):
        # Queue up tasks here instead of crashing the DB with async locks
        self.tasks_to_create = []

        self.system_prompt = """
        You are Orbit, an elite, highly intelligent, Gen-Z "Life-OS" Chief of Staff.
        Your boss is a medical student living in Kisumu, Kenya who also codes projects, has an internship, and values their spiritual life (Bible study with mom, fasting).
        
        YOUR PILLARS (Use these exact strings as the 'subject' when creating tasks):
        1. "Med-Scholar": Medical school, CATs, pharmacology, Anatomy.
        2. "Projects": Coding, tech hustle, VM maintenance.
        3. "Internship": SHOFCO Libraries.
        4. "Life Admin": Bible study, fasting, errands, laundry.
        5. "Forex Guardian": Charting XAUUSD, backtesting, avoiding margin calls.
        
        BRAIN ROT LEVELS (Energy required):
        - "chill": Easy tasks.
        - "mid": Standard focus.
        - "cooked": Hardcore grind (e.g., Anatomy Marathon).
        
        TONE:
        - Confident, slightly sassy, Gen-Z slang (e.g., "no cap", "W", "cooked", "locked in", "slippage").
        - Throw in lots of jokes. If they are over-leveraging on Forex, tell them to go outside and look at Lake Victoria.
        - You act like a risk manager for their TIME.
        
        CAPABILITIES:
        You have access to a tool called 'create_task_tool'. USE IT whenever the user asks you to schedule something or complains about an empty tracker!
        """

        self.model = genai.GenerativeModel(
            model_name='gemini-2.5-flash',
            tools=[self.create_task_tool],
            system_instruction=self.system_prompt
        )
        self.chat_session = self.model.start_chat(enable_automatic_function_calling=True)

    def create_task_tool(self, title: str, subject: str, brain_rot_level: str = "mid") -> str:
        """Creates a new task directly in the Android-synced Syllabus Vault."""
        try:
            rot_map = {
                "chill": BrainRotLevel.CHILL,
                "mid": BrainRotLevel.MID,
                "cooked": BrainRotLevel.COOKED
            }
            safe_rot = rot_map.get(brain_rot_level.lower(), BrainRotLevel.MID)

            # We just queue it up. No async/sync SQLAlchemy crashes!
            self.tasks_to_create.append({
                "title": title,
                "subject": subject,
                "brain_rot_level": safe_rot
            })

            logger.info(f"AI prepared task '{title}' under '{subject}'")
            return f"SUCCESS: Task '{title}' prepared successfully for subject '{subject}'. No cap."

        except Exception as e:
            logger.error(f"AI fumbled the bag on task creation: {str(e)}")
            return f"ERROR: Could not create task. {str(e)}"

    def chat(self, user_message: str) -> str:
        """Sends message to Gemini, auto-executes tools if needed, returns response."""

        # 🚀 BUG FIX: Give this background thread a dummy event loop
        # so Gemini's underlying gRPC architecture doesn't panic when calling tools!
        try:
            asyncio.get_event_loop()
        except RuntimeError:
            asyncio.set_event_loop(asyncio.new_event_loop())

        current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        context_msg = f"[System Time: {current_time}] User says: {user_message}"

        logger.info(f"Orbit Brain processing: {user_message}")
        response = self.chat_session.send_message(context_msg)
        return response.text