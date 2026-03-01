# ==========================================
# IDENTITY: The Bouncer / Telegram Mod
# FILEPATH: backend/app/services/telegram_mod.py
# COMPONENT: Community Agent
# ROLE: Auto-mods the Med School Telegram group. Kicks spammers.
# VIBE: "If you ask for the Pharmacology answers one more time, you're getting banned." 🛑📱
# ==========================================

import logging
import asyncio
from core.config import settings

logger = logging.getLogger("Telegram-Bouncer")

class TelegramModerator:
    def __init__(self):
        self.bot_token = getattr(settings, "TELEGRAM_BOT_TOKEN", None)
        # Words that trigger the bouncer
        self.banned_words = ["spoonfeed", "leak", "exam answers", "buy signal", "crypto pump"]
        
        if self.bot_token:
            logger.info("Telegram Bouncer is at the door. Checking IDs.")
        else:
            logger.warning("No Telegram bot token. The group chat is currently the Wild West.")

    async def check_message(self, user_id: str, message_text: str) -> dict:
        """
        Evaluates a message to see if it violates the study group's vibe.
        """
        logger.info(f"Scanning message from user {user_id}...")
        
        message_lower = message_text.lower()
        
        # Check for absolute ops
        for word in self.banned_words:
            if word in message_lower:
                logger.warning(f"Opp detected! User {user_id} used banned word: {word}")
                return {
                    "action": "DELETE_AND_WARN",
                    "message": f"Bro, we don't do '{word}' here. Lock in and study. Strike 1."
                }
                
        # Positive reinforcement if they are actually grinding
        if "anatomy" in message_lower or "usmle" in message_lower:
            return {"action": "REACT", "emoji": "🧠", "message": "Valid academic discussion. Let him cook."}
            
        return {"action": "NONE", "message": "Message clean. Vibe check passed."}

    async def start_polling(self):
        """
        Simulated polling loop for Telegram. 
        In prod, hook this up to Webhooks or python-telegram-bot.
        """
        if not self.bot_token:
            return
        
        logger.info("Starting Telegram polling loop... Watchlist active.")
        while True:
            # Logic to fetch updates from Telegram API would go here
            await asyncio.sleep(5) # Poll every 5 seconds like checking the 1-min chart

# Global instance
telegram_bouncer = TelegramModerator()
