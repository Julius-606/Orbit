################################################################################
#FILE: backend/app/services/llm.py
#VERSION: 1.0.1 | SYSTEM: Jarvis Protocol
################################################################################

import google.generativeai as genai
# 🚀 FIX: Added 'app.' prefix
from app.core.key_rotation import gemini_key_manager
import logging

logger = logging.getLogger("Orbit-Speak")

class OrbitSpeak:
    def __init__(self):
        if gemini_key_manager.has_keys():
            self.model = None
            logger.info("Orbit-Speak initialized. Gemini is online and ready to judge you.")
        else:
            self.model = None
            logger.warning("No Gemini API key found. Orbit is currently mute. 🤐")

    async def generate_response(self, prompt: str, context: str = "chilling") -> str:
        if not gemini_key_manager.has_keys():
            return "Bro, you forgot to give me my API key. Check the .env file or GEMINI_API_KEYS variable."

        system_prompt = f"""
        You are Orbit, an advanced Life-OS assistant. 
        Current user context: {context}.
        Keep your responses concise, slightly Gen-Z, and highly pragmatic. 
        If the user is over-leveraging in Forex, tell them to touch grass.
        If they are avoiding Med School studying, roast them.
        """

        full_prompt = f"{system_prompt}\nUser says: {prompt}"

        for _ in range(max(1, gemini_key_manager.key_count)):
            key = gemini_key_manager.get_current_key()
            if not key:
                break
            try:
                genai.configure(api_key=key)
                response = genai.GenerativeModel('gemini-3.8-flash').generate_content(full_prompt)
                return response.text
            except Exception as e:
                logger.warning("Gemini key failed; rotating to the next key: %s", e)
                gemini_key_manager.rotate_to_next_key()
        return "My brain is fried right now (all Gemini keys failed). Ask me later."

orbit_brain = OrbitSpeak()