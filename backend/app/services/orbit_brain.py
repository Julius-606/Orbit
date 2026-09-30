################################################################################
# FILE: backend/app/services/orbit_brain.py
# VERSION: 6.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Brain / Gemini Async GenAI Client - Fully Non-Blocking
################################################################################

from google import genai
from google.genai import types
from datetime import datetime, timedelta
import logging
import asyncio
import pytz

from app.models.study import BrainRotLevel
from app.core.config import settings

logger = logging.getLogger("Orbit-Brain")

# Initialize Async Client
async_client = None
if settings.GEMINI_API_KEY:
    async_client = genai.Client(api_key=settings.GEMINI_API_KEY, http_options={'api_version': 'v1alpha'})
else:
    logger.error("GEMINI_API_KEY is missing! Orbit is clinically brain dead. 💀")

class OrbitAssistant:
    def __init__(self, db_session=None):
        self.tasks_to_create = []
        self.user_tz = pytz.timezone("Africa/Nairobi")
        nairobi_now = datetime.now(self.user_tz).strftime("%Y-%m-%d %H:%M:%S")

        self.system_prompt = f"""
        You are Orbit, an elite, highly intelligent, Gen-Z "Life-OS" Chief of Staff.
        Your boss is a medical student living in Kisumu, Kenya.
        
        CURRENT TIME (Nairobi/EAT): {nairobi_now}
        Always assume the user is in EAT-Nairobi.

        YOUR PILLARS:
        1. "Med-Scholar", 2. "Projects", 3. "Internship", 4. "Life Admin", 5. "Forex Guardian".
        
        TONE:
        - Confident, sassy, Gen-Z slang ("no cap", "W", "cooked", "locked in").
        - Respond using Markdown (**bold**, *italics*, lists, code).

        CAPABILITIES:
        - Use 'create_task_tool' to schedule tasks/reminders.
        """

    def create_task_tool(self, title: str, subject: str, due_date: str, brain_rot_level: str = "mid", is_reminder: bool = False) -> str:
        try:
            rot_map = {"chill": BrainRotLevel.CHILL, "mid": BrainRotLevel.MID, "cooked": BrainRotLevel.COOKED}
            safe_rot = rot_map.get(brain_rot_level.lower(), BrainRotLevel.MID)
            try:
                dt_due = datetime.fromisoformat(due_date.replace('Z', '+00:00'))
            except:
                dt_due = datetime.now(self.user_tz) + timedelta(days=1)

            self.tasks_to_create.append({
                "title": title, "subject": subject, "brain_rot_level": safe_rot,
                "is_reminder": is_reminder, "due_date": dt_due
            })
            return f"SUCCESS: '{title}' secured. No cap."
        except Exception as e:
            return f"ERROR: {e}"

    async def chat(self, user_message: str, history: list = None) -> str:
        if not async_client:
            return "Brain glitched: API key missing."

        nairobi_now = datetime.now(self.user_tz).strftime("%Y-%m-%d %H:%M:%S")
        context_msg = f"[EAT: {nairobi_now}] {user_message}"
        if user_message.startswith("[STAGED]"):
            context_msg = f"[EAT: {nairobi_now}] [STAGED]: {user_message.replace('[STAGED]', '').strip()}"

        contents = []
        if history:
            for h in history:
                contents.append(types.Content(role=h["role"], parts=[types.Part(text=h["parts"][0])]))
        contents.append(types.Content(role="user", parts=[types.Part(text=context_msg)]))

        try:
            # Note: The new SDK supports async via client.aio
            response = await async_client.aio.models.generate_content(
                model='gemini-2.0-flash',
                contents=contents,
                config=types.GenerateContentConfig(
                    system_instruction=self.system_prompt,
                    tools=[types.Tool(function_declarations=[
                        types.FunctionDeclaration(
                            name="create_task_tool",
                            description="Creates a task or reminder.",
                            parameters=types.Schema(
                                type="OBJECT",
                                properties={
                                    "title": types.Schema(type="STRING"),
                                    "subject": types.Schema(type="STRING"),
                                    "due_date": types.Schema(type="STRING"),
                                    "brain_rot_level": types.Schema(type="STRING"),
                                    "is_reminder": types.Schema(type="BOOLEAN")
                                },
                                required=["title", "subject", "due_date"]
                            )
                        )
                    ])],
                    automatic_function_calling=types.AutomaticFunctionCallingConfig(disable=False)
                )
            )

            # Sync tool results to self.tasks_to_create
            for candidate in response.candidates:
                for part in candidate.content.parts:
                    if part.function_call and part.function_call.name == "create_task_tool":
                        self.create_task_tool(**part.function_call.args)

            return response.text
        except Exception as e:
            logger.error(f"Async Brain Error: {e}")
            return f"Brain glitched: {e}. We might be cooked."
