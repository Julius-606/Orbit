################################################################################
# FILE: backend/app/core/config.py
# VERSION: 1.1.0 (SECURITY UPGRADE)
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
    # If .env is missing, it falls back to this placeholder (bad for prod!)
    SECRET_KEY: str = "placeholder_vibe_check_key"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    # Agent APIs
    GEMINI_API_KEY: Optional[str] = None

    # Forex Guardian (MT5)
    MT5_LOGIN: Optional[int] = None
    MT5_PASSWORD: Optional[str] = None
    MT5_SERVER: Optional[str] = None

    class Config:
        # This tells Pydantic to look for a file named .env in the backend folder
        env_file = ".env"
        case_sensitive = True

settings = Settings()