# ==========================================
# IDENTITY: The Voice / Orbit-Speak Engine
# FILEPATH: backend/app/services/llm.py
# COMPONENT: AI Integration (Gemini)
# ROLE: Gives Orbit its personality and reasoning.
# VIBE: Basically your sassy AI mentor who roasts your trading history. 🔥
# ==========================================

import google.generativeai as genai
from core.config import settings
import logging

logger = logging.getLogger("Orbit-Speak")

class OrbitSpeak:
    def __init__(self):
        if settings.GEMINI_API_KEY:
            genai.configure(api_key=settings.GEMINI_API_KEY)
            # Using Flash because we need speed like a 1-minute scalp
            self.model = genai.GenerativeModel('gemini-2.5-flash')
            logger.info("Orbit-Speak initialized. Gemini is online and ready to judge you.")
        else:
            self.model = None
            logger.warning("No Gemini API key found. Orbit is currently mute. 🤐")

    async def generate_response(self, prompt: str, context: str = "chilling") -> str:
        """
        Feeds your prompt to Gemini with system instructions to act like your personal Jarvis.
        """
        if not self.model:
            return "Bro, you forgot to give me my API key. Check the .env file."

        system_prompt = f"""
        You are Orbit, an advanced Life-OS assistant. 
        Current user context: {context}.
        Keep your responses concise, slightly Gen-Z, and highly pragmatic. 
        If the user is over-leveraging in Forex, tell them to touch grass.
        If they are avoiding Med School studying, roast them.
        """
        
        full_prompt = f"{system_prompt}\nUser says: {prompt}"
        
        try:
            response = self.model.generate_content(full_prompt)
            return response.text
        except Exception as e:
            logger.error(f"Gemini API hit a stop loss: {e}")
            return "My brain is fried right now (API Error). Ask me later."

# Global instance
orbit_brain = OrbitSpeak()