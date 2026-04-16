################################################################################
# FILE: backend/app/core/config.py
# VERSION: 1.1.3 | SYSTEM: Neon DB Auto-Correct V2 (Anti-Slippage)
################################################################################

from pydantic_settings import BaseSettings
from typing import Optional, List
import os

class Settings(BaseSettings):
    PROJECT_NAME: str = "Project Orbit: Jarvis Protocol"
    VERSION: str = "3.1.0"

    # DB Settings - Default to localhost for local testing
    DATABASE_URL: str = os.getenv("DATABASE_URL", "postgresql+asyncpg://orbit_user:super_secret_password@localhost:5432/orbit_db")
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://localhost:6379/0")

    # 🛡️ SECURITY (The Bouncer's VIP List)
    SECRET_KEY: str = os.getenv("SECRET_KEY", "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    # Agent APIs
    GEMINI_API_KEY: Optional[str] = os.getenv("GEMINI_API_KEY")
    # Support for multiple keys (comma separated)
    GEMINI_API_KEYS: List[str] = [
        k.strip() for k in os.getenv("GEMINI_API_KEYS", "").split(",") if k.strip()
    ]

    # Forex Guardian (MT5)
    MT5_LOGIN: Optional[int] = None
    MT5_PASSWORD: Optional[str] = None
    MT5_SERVER: Optional[str] = None

    @property
    def async_database_url(self) -> str:
        """🚀 THE ULTIMATE NEON FIX"""
        url = self.DATABASE_URL
        if url and url.startswith("postgresql://"):
            url = url.replace("postgresql://", "postgresql+asyncpg://", 1)
        
        if url and "?" in url:
            url = url.split("?")[0]
            
        return url + "?ssl=require" if url else ""

    def get_all_api_keys(self) -> List[str]:
        keys = self.GEMINI_API_KEYS.copy()
        if self.GEMINI_API_KEY and self.GEMINI_API_KEY not in keys:
            keys.append(self.GEMINI_API_KEY)
        return keys

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
