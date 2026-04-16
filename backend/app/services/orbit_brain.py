################################################################################
# FILE: backend/app/services/orbit_brain.py
# VERSION: 5.2.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Brain / Gemini GenAI SDK - Model & Key Rotation
################################################################################

from google import genai
from google.genai import types
from datetime import datetime, timedelta
import logging
import asyncio
import pytz
import random

from app.models.study import BrainRotLevel
from app.core.config import settings

logger = logging.getLogger("Orbit-Brain")

class OrbitAssistant:
    # 🎯 FIX: 'gemini-2.5-flash' doesn't exist (yet). Using '2.0'.
    # 🎯 FIX: 'gemini-flash-latest' often 404s depending on the API version.
    # Standardizing to these IDs which are guaranteed to work with the new SDK.
    MODELS = ["gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-flash-latest"]

    def __init__(self, db_session=None):
        self.tasks_to_create = []
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
        - "chill": Easy tasks.
        - "mid": Standard work.
        - "cooked": High stress/Panic mode.
        
        TONE:
        - Confident, sassy, Gen-Z slang ("no cap", "W", "cooked", "locked in").
        - You are a risk manager for their TIME.
        
        CAPABILITIES:
        - Use 'create_task_tool' to schedule tasks OR reminders.
        - ALWAYS set a 'due_date' (ISO format).
        - If the user asks for something "today", use the CURRENT DATE: {nairobi_now_dt.date()}.
        """

        # Model & Key Rotation Logic
        self.api_keys = settings.get_all_api_keys()
        if not self.api_keys:
            logger.error("NO API KEYS FOUND! Orbit is clinically brain dead. 💀")
            raise ValueError("Missing GEMINI_API_KEY")

        # Pick a random key and model from the rotation pool
        self.selected_key = random.choice(self.api_keys)
        self.selected_model = random.choice(self.MODELS)

        logger.info(f"Orbit Brain active: Using model {self.selected_model} with key {self.selected_key[:8]}...")

        self.client = genai.Client(api_key=self.selected_key)

        # Tools configuration
        self.tools = [
            types.Tool(
                function_declarations=[
                    types.FunctionDeclaration(
                        name="create_task_tool",
                        description="Creates a new task or reminder in the Syllabus Vault.",
                        parameters=types.Schema(
                            type="OBJECT",
                            properties={
                                "title": types.Schema(type="STRING", description="The name of the task."),
                                "subject": types.Schema(type="STRING", description="The pillar it belongs to."),
                                "due_date": types.Schema(type="STRING", description="ISO 8601 string (e.g. '2024-12-25T14:00:00')."),
                                "remarks": types.Schema(type="STRING", description="Optional details or notes."),
                                "brain_rot_level": types.Schema(type="STRING", description="'chill', 'mid', or 'cooked'."),
                                "is_reminder": types.Schema(type="BOOLEAN", description="Whether it's a reminder.")
                            },
                            required=["title", "subject", "due_date"]
                        )
                    )
                ]
            )
        ]

    def create_task_tool(self, title: str, subject: str, due_date: str, remarks: str = None, brain_rot_level: str = "mid", is_reminder: bool = False) -> str:
        """Internal handler for task creation."""
        try:
            rot_map = {"chill": BrainRotLevel.CHILL, "mid": BrainRotLevel.MID, "cooked": BrainRotLevel.COOKED}
            safe_rot = rot_map.get(brain_rot_level.lower(), BrainRotLevel.MID)

            try:
                dt_due = datetime.fromisoformat(due_date.replace('Z', '+00:00'))
            except ValueError:
                dt_due = datetime.now(self.user_tz) + timedelta(hours=2)

            self.tasks_to_create.append({
                "title": title,
                "subject": subject,
                "brain_rot_level": safe_rot,
                "is_reminder": is_reminder,
                "due_date": dt_due,
                "remarks": remarks
            })

            type_str = "Reminder" if is_reminder else "Task"
            return f"SUCCESS: {type_str} '{title}' set for {due_date}. Remarks: {remarks}. W."
        except Exception as e:
            logger.error(f"Tool Error: {str(e)}")
            return f"ERROR: {str(e)}"

    async def chat(self, user_message: str, history: list = None) -> str:
        """Sends message with the new SDK."""

        formatted_history = []
        if history:
            for h in history:
                formatted_history.append(types.Content(role=h["role"], parts=[types.Part(text=p) for p in h["parts"]]))

        chat = self.client.chats.create(
            model=self.selected_model,
            config=types.GenerateContentConfig(
                system_instruction=self.system_prompt,
                tools=self.tools,
                automatic_function_calling=types.AutomaticFunctionCallingConfig(disable=False)
            ),
            history=formatted_history
        )

        response = await asyncio.to_thread(chat.send_message, user_message)

        # Process any tool calls
        for part in response.candidates[0].content.parts:
            if part.function_call:
                if part.function_call.name == "create_task_tool":
                    args = part.function_call.args
                    self.create_task_tool(**args)

        return response.text
