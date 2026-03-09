################################################################################
# FILE: backend/app/core/config.py
# VERSION: 1.2.0 | SYSTEM: Configuration Settings
################################################################################

import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # ==========================================
    # 🪐 SYSTEM SETTINGS
    # ==========================================
    PROJECT_NAME: str = "Orbit Brain API"
    VERSION: str = "3.1.0"
    API_V1_STR: str = "/api/v1"
    
    # ==========================================
    # 📈 DATABASE CONFIG (NEON POSTGRES)
    # ==========================================
    # Default fallback just in case the .env gets liquidated
    DATABASE_URL: str = os.getenv("DATABASE_URL", "postgresql://user:password@localhost:5432/orbit_db")
    
    # ==========================================
    # 🤖 AI / EXTERNAL APIS
    # ==========================================
    GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY", "your-gemini-key")
    
    # ==========================================
    # 🧠 CATE & FOREX GUARDIAN CONFIG
    # ==========================================
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://localhost:6379")
    MT5_ACCOUNT: str = os.getenv("MT5_ACCOUNT", "")
    MT5_PASSWORD: str = os.getenv("MT5_PASSWORD", "")
    MT5_SERVER: str = os.getenv("MT5_SERVER", "")

    @property
    def async_database_url(self) -> str:
        """
        🚀 THE FIX: Neon provides a standard 'postgresql://' URL, but our
        async engine requires 'postgresql+asyncpg://' to avoid crashing on startup!
        This silently patches the URL if needed. Zero drawdown.
        """
        url = self.DATABASE_URL
        if url and url.startswith("postgresql://"):
            url = url.replace("postgresql://", "postgresql+asyncpg://", 1)
        
        # 🛑 THE SSL FIX: asyncpg hates 'sslmode=require', it prefers 'ssl=require'
        if "sslmode=" in url:
            url = url.replace("sslmode=", "ssl=")
            
        return url

    class Config:
        case_sensitive = True
        env_file = ".env"

# Instantiate the settings so the whole app can import it natively
settings = Settings()
