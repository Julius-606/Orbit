################################################################################
#FILE: backend/app/services/med_scan.py
#VERSION: 1.0.1 | SYSTEM: Jarvis Protocol
################################################################################

import logging
import google.generativeai as genai
# 🚀 FIX: Added 'app.' prefix
from app.core.key_rotation import gemini_key_manager

logger = logging.getLogger("Med-Scan")

class RadiologistEngine:
    def __init__(self):
        if gemini_key_manager.has_keys():
            self.model = True
            logger.info("Med-Scan OCR initialized. Ready to decode hieroglyphics.")
        else:
            self.model = None
            logger.warning("No Gemini API key. Good luck reading that prescription yourself.")

    async def scan_chart(self, image_bytes: bytes) -> dict:
        if not self.model:
            return {"status": "error", "message": "GEMINI_API_KEYS is missing. Cannot process image."}

        logger.info("Processing new medical chart... Hold tight.")

        prompt = """
        You are an expert radiologist, medical scribe, and top-tier Med School professor.
        Read this patient chart, ECG, or medical notes image.
        Extract the key information (Symptoms, Diagnosis, Plan, Pathophysiology) and format it cleanly using Markdown.
        If the handwriting is absolutely cooked and illegible, just do your best and flag it for manual review.
        """

        for _ in range(max(1, gemini_key_manager.key_count)):
            key = gemini_key_manager.get_current_key()
            if not key:
                break
            try:
                genai.configure(api_key=key)
                response = genai.GenerativeModel('gemini-2.5-flash').generate_content([prompt, image_bytes])
                return {"status": "success", "extracted_data": response.text}
            except Exception as e:
                logger.warning("Med-Scan key failed; rotating: %s", e)
                gemini_key_manager.rotate_to_next_key()
        return {"status": "error", "message": "All Gemini keys failed to decode the chart."}

radiologist = RadiologistEngine()