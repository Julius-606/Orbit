#>>>--- START_FILE_BLOCK: backend/app/core/config.py
################################################################################
# FILE: backend/app/core/config.py
# VERSION: 5.0.0 | SYSTEM: Unified Orbit & DebateHub Config (Resilient Pydantic V2)
################################################################################

from typing import Optional
import os
from pathlib import Path

try:
    from pydantic_settings import BaseSettings, SettingsConfigDict
except ImportError:
    try:
        from pydantic import BaseSettings  # type: ignore
        SettingsConfigDict = None  # type: ignore
    except ImportError:
        # Fallback for environments where pydantic is not yet initialized
        class BaseSettings:  # type: ignore
            def __init__(self, **kwargs):
                for k, v in kwargs.items():
                    setattr(self, k, v)
        SettingsConfigDict = None  # type: ignore


class Settings(BaseSettings):
    PROJECT_NAME: str = "Project Orbit:"
    VERSION: str = "4.5.0"

    # =========================================================================
    # 🗄️ DATABASE SETTINGS
    # =========================================================================
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL",
        "postgresql+asyncpg://orbit_user:super_secret_password@localhost:5432/orbit_db"
    )
    REDIS_URL: Optional[str] = os.getenv("REDIS_URL", "redis://localhost:6379/0")

    # DebateHub Neon PostgreSQL Connection Strings
    DEBATEHUB_NEON_DATABASE_URL: Optional[str] = os.getenv("DEBATEHUB_NEON_DATABASE_URL")
    NEON_DATABASE_URL: Optional[str] = os.getenv("NEON_DATABASE_URL")

    # =========================================================================
    # 🛡️ SECURITY & AUTHENTICATION
    # =========================================================================
    # Primary secret key used by Orbit and Android app
    SECRET_KEY: str = os.getenv("SECRET_KEY", "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4")
    # Secondary / Alternative JWT & Internal API secrets
    JWT_SECRET_KEY: Optional[str] = os.getenv("JWT_SECRET_KEY")
    INTERNAL_API_KEY: Optional[str] = os.getenv("INTERNAL_API_KEY")
    ORBIT_SECRET_TOKEN: Optional[str] = os.getenv("ORBIT_SECRET_TOKEN")

    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7

    # =========================================================================
    # 🤖 AI / GEMINI KEYS
    # =========================================================================
    # Single key support
    GEMINI_API_KEY: Optional[str] = os.getenv("GEMINI_API_KEY")
    # Multi-key rotation support (comma-separated: AIzaSy...,AIzaSy...)
    GEMINI_API_KEYS: Optional[str] = os.getenv("GEMINI_API_KEYS")

    # =========================================================================
    # 📈 FOREX GUARDIAN (MT5)
    # =========================================================================
    MT5_LOGIN: Optional[int] = None
    MT5_PASSWORD: Optional[str] = None
    MT5_SERVER: Optional[str] = None

    # =========================================================================
    # 🌐 NETWORKING & SERVICE PORTS
    # =========================================================================
    PORT: int = int(os.getenv("PORT", "7860"))
    DEBATEHUB_PORT: int = int(os.getenv("DEBATEHUB_PORT", "3000"))
    DEBATEHUB_INTERNAL_URL: str = os.getenv("DEBATEHUB_INTERNAL_URL", "http://127.0.0.1:3000")
    VITE_BASE: str = os.getenv("VITE_BASE", "/debatehub/")
    DISABLE_HMR: bool = os.getenv("DISABLE_HMR", "false").lower() in ("true", "1", "yes")

    # Base URLs
    BASE_URL_HF: Optional[str] = os.getenv("BASE_URL_HF", "https://agent606-orbit.hf.space")
    BASE_URL_LOCAL: Optional[str] = os.getenv("BASE_URL_LOCAL", "http://localhost:7860")
    BASE_URL_NGROK: Optional[str] = os.getenv("BASE_URL_NGROK")

    # =========================================================================
    # 🚀 NEON ASYNC DATABASE URL RESOLVER
    # =========================================================================
    @property
    def async_database_url(self) -> str:
        """
        Neon adds wild URL parameters (like ?options=endpoint...) that make
        asyncpg throw channel_binding errors. We strip noise and ensure pure asyncpg driver.
        """
        url = self.DATABASE_URL
        if not url:
            return ""

        # FIX: Support both postgres:// and postgresql:// prefixes for asyncpg
        if url.startswith("postgres://"):
            url = url.replace("postgres://", "postgresql+asyncpg://", 1)
        elif url.startswith("postgresql://") and not url.startswith("postgresql+asyncpg://"):
            url = url.replace("postgresql://", "postgresql+asyncpg://", 1)
        
        # The surgical scalp: Cut off all query parameters Neon adds
        if "?" in url:
            url = url.split("?")[0]
            
        # Support Neon SSL while allowing local development without SSL errors
        if "sqlite" in url or "localhost" in url or "127.0.0.1" in url:
            return url
        return url + "?ssl=require"

    @property
    def effective_secret_key(self) -> str:
        """Returns the active secret key, preferring JWT_SECRET_KEY if explicitly set."""
        return self.JWT_SECRET_KEY or self.SECRET_KEY

    @property
    def effective_gemini_keys(self) -> str:
        """Returns formatted Gemini keys string, coalescing GEMINI_API_KEYS and GEMINI_API_KEY."""
        if self.GEMINI_API_KEYS and self.GEMINI_API_KEYS.strip():
            return self.GEMINI_API_KEYS.strip()
        if self.GEMINI_API_KEY and self.GEMINI_API_KEY.strip():
            return self.GEMINI_API_KEY.strip()
        return ""

    # =========================================================================
    # ⚙️ PYDANTIC V1 & V2 COMPATIBILITY (extra="ignore" PREVENTS CRASHES)
    # =========================================================================
    if SettingsConfigDict is not None:
        model_config = SettingsConfigDict(
            # Resolve the backend dotenv file from this module rather than the
            # process working directory. Uvicorn may be launched from either
            # the repository root or the backend directory.
            env_file=str(Path(__file__).resolve().parents[2] / ".env"),
            env_file_encoding="utf-8",
            case_sensitive=True,
            extra="ignore",  # Crucial: ignore unexpected extra fields in .env without throwing validation errors
        )
    else:
        class Config:
            env_file = str(Path(__file__).resolve().parents[2] / ".env")
            case_sensitive = True
            extra = "ignore"  # Pydantic v1 fallback to never crash on unexpected env variables


settings = Settings()

# Post-initialization synchronization for Gemini key rotation and secrets
if not settings.GEMINI_API_KEYS and settings.GEMINI_API_KEY:
    settings.GEMINI_API_KEYS = settings.GEMINI_API_KEY

# Ensure SECRET_KEY falls back to JWT_SECRET_KEY if JWT_SECRET_KEY is defined
if settings.JWT_SECRET_KEY and settings.SECRET_KEY == "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4":
    settings.SECRET_KEY = settings.JWT_SECRET_KEY

#<<<--- END_FILE_BLOCK: backend/app/core/config.py
