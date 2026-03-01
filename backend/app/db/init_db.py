# ==========================================
# IDENTITY: The Big Bang / DB Seed Script
# FILEPATH: backend/app/db/init_db.py
# COMPONENT: Database Initialization
# ROLE: Fills your fresh database with some starting data so it isn't completely empty.
# VIBE: Creating the universe in 7 days, but we're doing it in 0.5 seconds. 🌌
# ==========================================

import asyncio
from db.session import async_session, engine
from models.study import Base, StudyTask
import logging

logger = logging.getLogger("Orbit-Genesis")

async def init_models():
    """
    Creates the tables if Alembic hasn't done it yet.
    """
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("Database tables forged successfully. 🔨")

async def seed_data():
    """Drops some default tasks into the syllabus vault."""
    async with async_session() as db:
        task1 = StudyTask(title="Master the Cardiac Cycle", subject="Cardiology", brain_rot_level="cooked")
        task2 = StudyTask(title="Review Prop Firm Drawdown Rules", subject="Forex", brain_rot_level="chill")
        
        db.add(task1)
        db.add(task2)
        await db.commit()
        logger.info("Genesis data injected. Orbit is alive.")

if __name__ == "__main__":
    logger.info("Initiating Project Orbit Genesis Protocol...")
    asyncio.run(init_models())
    asyncio.run(seed_data())