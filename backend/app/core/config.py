#>>>--- START_FILE_BLOCK: backend/app/core/config.py
################################################################################
# FILE: backend/app/core/config.py
# VERSION: 1.1.2 | SYSTEM: Neon DB Auto-Correct V2 (Anti-Slippage)
################################################################################

from pydantic_settings import BaseSettings
from typing import Optional
import os

class Settings(BaseSettings):
    PROJECT_NAME: str = "Project Orbit: Jarvis Protocol"
    VERSION: str = "3.1.0"

    # DB Settings - Default to localhost for local testing
    DATABASE_URL: str = os.getenv("DATABASE_URL", "postgresql+asyncpg://orbit_user:super_secret_password@localhost:5432/orbit_db")
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://localhost:6379/0")

    # 🛡️ SECURITY (The Bouncer's VIP List)
    # Defaults to the exact key your Android app uses so it doesn't get blocked!
    SECRET_KEY: str = os.getenv("SECRET_KEY", "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    # Agent APIs
    GEMINI_API_KEY: Optional[str] = os.getenv("GEMINI_API_KEY")

    # Forex Guardian (MT5)
    MT5_LOGIN: Optional[int] = None
    MT5_PASSWORD: Optional[str] = None
    MT5_SERVER: Optional[str] = None

    @property
    def async_database_url(self) -> str:
        """
        🚀 THE ULTIMATE NEON FIX: 
        Neon tacks on wild URL parameters (like ?options=endpoint...) that make
        asyncpg throw a 'channel_binding' tantrum. We strip the noise and keep it pure.
        """
        url = self.DATABASE_URL
        if url and url.startswith("postgresql://"):
            url = url.replace("postgresql://", "postgresql+asyncpg://", 1)
        
        # The surgical scalp: Cut off all query parameters Neon adds
        if url and "?" in url:
            url = url.split("?")[0]
            
        # Force standard SSL so the connection is encrypted without confusing asyncpg
        return url + "?ssl=require" if url else ""

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()

#<<<--- END_FILE_BLOCK: backend/app/core/config.py
