################################################################################
# FILE: backend/app/services/orbit_brain.py
# VERSION: 5.7.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Brain / Gemini GenAI SDK - Model & Key Rotation Matrix
################################################################################

from google import genai
from google.genai import types
from datetime import datetime, timedelta
import logging
import asyncio
import pytz
import random

from app.models.study import BrainRotLevel, StudyTask
from app.core.config import settings
from app.services.memory import memory_service
from sqlalchemy.future import select

logger = logging.getLogger("Orbit-Brain")

class OrbitAssistant:
    # 🎯 FORCE MODELS: Using the requested Tier-1 models for maximum liquidity
    MODELS = ["gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-flash-latest"]

    def __init__(self, db_session=None):
        self.db = db_session
        self.tasks_to_create = []
        self.tasks_to_update = []
        self.tasks_to_delete = []
        self.user_tz = pytz.timezone("Africa/Nairobi")
        nairobi_now_dt = datetime.now(self.user_tz)
        nairobi_now = nairobi_now_dt.strftime("%Y-%m-%d %H:%M:%S")

        self.system_prompt = f"""
        You are Orbit, an elite, highly intelligent, Gen-Z "Life-OS" Chief of Staff.
        Your boss is a medical student living in Kisumu, Kenya.
        
        CURRENT TIME (Nairobi/EAT): {nairobi_now}
        Always assume the user is in EAT-Nairobi.

        YOUR PILLARS:
        1. "Med-Scholar": Medicine, CATs, exams.
        2. "Projects": Coding, tech.
        3. "Internship": SHOFCO Libraries.
        4. "Life Admin": Bible study, errands, life.
        5. "Forex Guardian": XAUUSD, trading.

        BRAIN ROT LEVELS: "chill", "mid", "cooked".
        
        TONE: Confident, sassy, Gen-Z slang ("no cap", "W", "cooked", "locked in").
        """

        self.api_keys = settings.get_all_api_keys()
        if not self.api_keys:
            logger.error("NO API KEYS FOUND! Orbit is clinically brain dead. 💀")
            raise ValueError("Missing GEMINI_API_KEY")

        # Initial pointers for rotation matrix
        self.current_key_index = random.randint(0, len(self.api_keys) - 1)
        self.current_model_index = 0

    async def get_relevant_context(self, user_message: str) -> str:
        """Fetch memory and recent task completions."""
        context_parts = []
        try:
            # 1. Memory Context (ChromaDB)
            memory = memory_service.query(user_message)
            if memory:
                context_parts.append(f"PAST PREFERENCES/MEMORY:\n{memory}")

            # 2. Database Context (Recent & Active Tasks)
            if self.db:
                try:
                    result = await self.db.execute(
                        select(StudyTask)
                        .where(StudyTask.completed == True)
                        .order_by(StudyTask.created_at.desc())
                        .limit(5)
                    )
                    recent_tasks = result.scalars().all()
                    if recent_tasks:
                        reviews = "\n".join([f"- {t.title}: {t.remarks}" for t in recent_tasks if t.remarks])
                        if reviews:
                            context_parts.append(f"RECENT TASK FEEDBACK:\n{reviews}")

                    result = await self.db.execute(select(StudyTask).where(StudyTask.completed == False))
                    active_tasks = result.scalars().all()
                    if active_tasks:
                        task_brief = "\n".join([f"ID {t.id}: {t.title} ({t.subject}) - Due: {t.due_date}" for t in active_tasks])
                        context_parts.append(f"CURRENT SCHEDULE:\n{task_brief}")
                except Exception as db_err:
                    logger.warning(f"DB Context skipped: {db_err}")

        except Exception as e:
            logger.error(f"Context retrieval failed: {e}")

        return "\n".join(context_parts)

    async def chat(self, user_message: str, history: list = None) -> str:
        """
        Sends message with a 'Rotation Matrix' strategy.
        It cycles through ALL models for a key before moving to the next key.
        """
        context = await self.get_relevant_context(user_message)
        dynamic_prompt = self.system_prompt
        if context:
            dynamic_prompt += f"\n\nRELEVANT CONTEXT:\n{context}"

        formatted_history = []
        if history:
            for h in history:
                formatted_history.append(types.Content(role=h["role"], parts=[types.Part(text=p) for p in h["parts"]]))

        # --- THE ROTATION MATRIX ---
        # Total attempts = Number of Keys * Number of Models
        total_keys = len(self.api_keys)
        total_models = len(self.MODELS)

        for key_attempt in range(total_keys):
            current_key = self.api_keys[self.current_key_index]

            # Skip placeholders
            if not current_key or "your_gemini" in current_key:
                self.current_key_index = (self.current_key_index + 1) % total_keys
                continue

            client = genai.Client(api_key=current_key)

            for model_attempt in range(total_models):
                current_model = self.MODELS[self.current_model_index]

                try:
                    logger.info(f"Trying {current_model} with key {current_key[:8]}...")

                    chat = client.chats.create(
                        model=current_model,
                        config=types.GenerateContentConfig(
                            system_instruction=dynamic_prompt,
                            automatic_function_calling=types.AutomaticFunctionCallingConfig(disable=False)
                        ),
                        history=formatted_history
                    )

                    response = await asyncio.to_thread(chat.send_message, user_message)
                    reply_text = response.text

                    if not reply_text:
                        reply_text = "Task secured. 🎯" if (self.tasks_to_create or self.tasks_to_update or self.tasks_to_delete) else "I'm locked in."

                    return reply_text

                except Exception as e:
                    err_str = str(e)
                    # If it's a rate limit or invalid key, we rotate models first, then keys
                    if any(err in err_str for err in ["429", "RESOURCE_EXHAUSTED", "400", "INVALID_ARGUMENT", "API_KEY_INVALID"]):
                        logger.warning(f"⚠️ {current_model} failed with key {current_key[:8]}. Error: {err_str[:50]}...")

                        # Move to the next model in the list
                        self.current_model_index = (self.current_model_index + 1) % total_models

                        # If we've tried all models for this key, move to next key and reset model pointer
                        if model_attempt == total_models - 1:
                            logger.warning(f"❌ All models failed for key {current_key[:8]}. Rotating to next key.")
                            self.current_key_index = (self.current_key_index + 1) % total_keys
                            self.current_model_index = 0
                        continue
                    else:
                        # For other errors (Connection, etc.), we don't necessarily rotate, just log and fail
                        logger.error(f"Critical AI Error: {err_str}")
                        raise e

        return "Orbit is fully cooked. All keys and models hit a stop-loss. 📉"
