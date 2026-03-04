# ################################################################################
# FILE: backend/app/db/session.py
# VERSION: 1.0.2 | SYSTEM: Jarvis Protocol
# ################################################################################

# ==========================================
# IDENTITY: The Memory Card / DB Session
# FILEPATH: backend/app/db/session.py
# COMPONENT: Database Connection
# ROLE: Hooks up FastAPI to Postgres.
# VIBE: Holding onto data tighter than you hold onto a losing trade hoping it reverses. 😭
# ==========================================

from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker

# 🚀 THE FIX: Slapped the 'app.' prefix right here so Python doesn't get liquidated.
from app.core.config import settings
import logging

logger = logging.getLogger("Orbit-DB")

# Create the Async Engine - We don't block the event loop here.
# Fast execution, just like a 1-minute scalp.
engine = create_async_engine(
    settings.DATABASE_URL,
    echo=False, # Set to True if you want to see raw SQL and get a headache
    future=True
)

# The Session Factory
async_session = sessionmaker(
    engine, class_=AsyncSession, expire_on_commit=False
)

async def get_db():
    """
    Dependency to inject the DB session into our routes.
    Yields a session and safely closes it when the request is done.
    No connection leaks on my watch! 🛑
    """
    async with async_session() as session:
        try:
            yield session
        except Exception as e:
            logger.error(f"Database just hit a stop loss: {e}")
            await session.rollback()
            raise
        finally:
            await session.close()