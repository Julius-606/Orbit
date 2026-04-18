################################################################################
# FILE: backend/app/db/session.py
# VERSION: 1.0.4 | SYSTEM: Neon DB Resilience Protocol
################################################################################

from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker

from app.core.config import settings
import logging

logger = logging.getLogger("Orbit-DB")

# Create the Async Engine - Optimized for long-running AI requests
# 🚀 THE FIX: Added pool_pre_ping and pool_recycle to handle idle timeouts
# during long model downloads.
engine = create_async_engine(
    settings.async_database_url,
    echo=False, 
    future=True,
    pool_pre_ping=True,      # Checks if connection is alive before using it
    pool_recycle=300,        # Recycles connections every 5 minutes
    pool_size=5,             # Limit connections to save Neon resources
    max_overflow=10          # Allow some burst overflow
)

# The Session Factory
async_session = sessionmaker(
    engine, class_=AsyncSession, expire_on_commit=False
)

async def get_db():
    """
    Dependency to inject the DB session into our routes.
    Yields a session and safely closes it when the request is done.
    """
    async with async_session() as session:
        try:
            yield session
        except Exception as e:
            # If the DB hits a stop loss, we roll back so we don't blow the account
            logger.error(f"Database just hit a stop loss: {e}")
            await session.rollback()
            raise
        finally:
            await session.close()
