# ==========================================
# IDENTITY: The Vault / System Configuration
# FILEPATH: backend/app/core/config.py
# COMPONENT: Backend Core
# ROLE: Holds all the keys to the kingdom. 
# VIBE: Keep this out of version control, or someone in Russia is trading your account. 💀
# ==========================================

from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    PROJECT_NAME: str = "Project Orbit: Jarvis Protocol"
    VERSION: str = "3.0"
    
    # DB Settings

    DATABASE_URL: str = "postgresql+asyncpg://orbit_user:super_secret_password@localhost:5432/orbit_db"
    REDIS_URL: str = "redis://localhost:6379/0"
    
    # Security
    SECRET_KEY: str = "generate_a_massive_random_string_here_for_aes256"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7 # 1 week offline cache max
    
    # Agent APIs
    GEMINI_API_KEY: Optional[str] = None
    
    # Forex Guardian (MT5)
    MT5_LOGIN: Optional[int] = None
    MT5_PASSWORD: Optional[str] = None
    MT5_SERVER: Optional[str] = None

    class Config:
        env_file = ".env" # Loads from the shadow realm

# Instantiate the settings so the whole app can vibe with it
settings = Settings()