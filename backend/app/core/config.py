################################################################################
# FILE: backend/app/core/config.py
# VERSION: 1.1.1 | SYSTEM: Neon DB Auto-Correct
################################################################################

from pydantic_settings import BaseSettings
from typing import Optional
import os

class Settings(BaseSettings):
    PROJECT_NAME: str = "Project Orbit: Jarvis Protocol"
    VERSION: str = "3.1.0"

    # DB Settings - Using localhost as default for your HP Pavilion
    DATABASE_URL: str = "postgresql+asyncpg://orbit_user:super_secret_password@localhost:5432/orbit_db"
    REDIS_URL: str = "redis://localhost:6379/0"

    # 🛡️ SECURITY (Pulled from .env)
    SECRET_KEY: str = "placeholder_vibe_check_key"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    # Agent APIs
    GEMINI_API_KEY: Optional[str] = None

    # Forex Guardian (MT5)
    MT5_LOGIN: Optional[int] = None
    MT5_PASSWORD: Optional[str] = None
    MT5_SERVER: Optional[str] = None

    @property
    def async_database_url(self) -> str:
        """
        🚀 THE FIX: Neon provides a standard 'postgresql://' URL, but our
        async engine requires 'postgresql+asyncpg://' to avoid crashing on startup!
        This silently patches the URL if needed. Zero drawdown.
        """
        if self.DATABASE_URL and self.DATABASE_URL.startswith("postgresql://"):
            return self.DATABASE_URL.replace("postgresql://", "postgresql+asyncpg://", 1)
        return self.DATABASE_URL

    class Config:
        # This tells Pydantic to look for a file named .env in the backend folder
        env_file = ".env"
        case_sensitive = True

settings = Settings()
