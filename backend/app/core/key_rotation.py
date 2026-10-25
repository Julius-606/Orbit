# ==============================================================================
# FILE: backend/app/core/key_rotation.py
# VERSION: 1.0.0 | SYSTEM: Orbit Gemini API Key Rotation Engine
# ==============================================================================

import os
import threading
import logging
from typing import List, Optional
from app.core.config import settings

logger = logging.getLogger("OrbitKeyRotation")

class GeminiKeyRotationManager:
    """
    Manages rotation across multiple Gemini API keys specified in `GEMINI_API_KEYS`.
    Format: A single comma-separated string, e.g. "AIzaSyKey1...,AIzaSyKey2...,AIzaSyKey3..."
    """
    def __init__(self):
        self._lock = threading.Lock()
        self._keys: List[str] = []
        self._current_index = 0
        self.reload_keys()

    def reload_keys(self):
        with self._lock:
            # Settings loads backend/.env independently of the process working
            # directory. Reading only os.environ misses dotenv values because
            # pydantic-settings does not export them back into os.environ.
            raw_multi = settings.effective_gemini_keys
            if not raw_multi:
                raw_multi = os.getenv("GEMINI_API_KEYS", "") or os.getenv("GEMINI_API_KEY", "")
            keys: List[str] = []
            if raw_multi:
                # Comma-separated: AIzaSy...,AIzaSy...
                for part in raw_multi.split(","):
                    clean = part.strip().strip("'\"")
                    if clean and clean not in keys:
                        keys.append(clean)

            self._keys = keys
            self._current_index = 0

            if self._keys:
                logger.info(f"🔑 Gemini Key Rotation initialized with {len(self._keys)} active key(s).")
            else:
                logger.warning("⚠️ No Gemini API keys found in GEMINI_API_KEYS.")

    @property
    def key_count(self) -> int:
        with self._lock:
            return len(self._keys)

    def has_keys(self) -> bool:
        with self._lock:
            return len(self._keys) > 0

    def get_current_key(self) -> Optional[str]:
        with self._lock:
            if not self._keys:
                return None
            return self._keys[self._current_index % len(self._keys)]

    def rotate_to_next_key(self) -> Optional[str]:
        with self._lock:
            if not self._keys:
                return None
            old_index = self._current_index
            self._current_index = (self._current_index + 1) % len(self._keys)
            next_key = self._keys[self._current_index]
            logger.info(f"🔄 Rotated Gemini API key: Index {old_index} -> {self._current_index} of {len(self._keys)}")
            return next_key

    def get_all_keys(self) -> List[str]:
        with self._lock:
            return list(self._keys)


# Singleton instance
gemini_key_manager = GeminiKeyRotationManager()
