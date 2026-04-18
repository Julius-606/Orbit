################################################################################
# FILE: backend/app/db/init_db.py
# VERSION: 1.1.0 | SYSTEM: Neon DB Auto-Migration 🚀
################################################################################

import asyncio
from sqlalchemy import text
from app.db.session import async_session, engine
from app.models.study import Base, StudyTask, BrainRotLevel
import logging

logger = logging.getLogger("Orbit-Genesis")

async def ensure_columns_exist():
    """
    Ensures that Orbit v4.0.0 columns exist in the database.
    This handles migrations manually for environments like HF/Render where
    Alembic might not have run or where the DB was created before the schema update.
    """
    async with engine.begin() as conn:
        logger.info("Checking database schema for Orbit v4.0.0 updates...")

        # 1. Check for 'remarks' column
        result = await conn.execute(text("""
            SELECT column_name
            FROM information_schema.columns
            WHERE table_name='study_tasks' AND column_name='remarks';
        """))
        if not result.fetchone():
            logger.info("Adding missing 'remarks' column to 'study_tasks'...")
            await conn.execute(text("ALTER TABLE study_tasks ADD COLUMN remarks TEXT;"))

        # 2. Check for 'is_reminder' column
        result = await conn.execute(text("""
            SELECT column_name
            FROM information_schema.columns
            WHERE table_name='study_tasks' AND column_name='is_reminder';
        """))
        if not result.fetchone():
            logger.info("Adding missing 'is_reminder' column to 'study_tasks'...")
            await conn.execute(text("ALTER TABLE study_tasks ADD COLUMN is_reminder BOOLEAN DEFAULT FALSE;"))

        logger.info("Database schema check complete. Orbit is synchronized. 🛸")

async def init_models():
    """Creates the tables if they don't exist and ensures columns are correct."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    # Run the manual migration fix for existing tables
    await ensure_columns_exist()
    logger.info("Database tables forged and synchronized successfully. 🔨")

async def seed_data():
    """Drops some default tasks into the syllabus vault if it's empty."""
    async with async_session() as db:
        # Check if we already have tasks
        result = await db.execute(text("SELECT count(*) FROM study_tasks"))
        count = result.scalar()

        if count == 0:
            task1 = StudyTask(title="Master the Cardiac Cycle", subject="Cardiology", brain_rot_level=BrainRotLevel.COOKED)
            task2 = StudyTask(title="Review Prop Firm Drawdown Rules", subject="Forex", brain_rot_level=BrainRotLevel.CHILL)

            db.add(task1)
            db.add(task2)
            await db.commit()
            logger.info("Genesis data injected. Orbit is alive.")
        else:
            logger.info("Data already exists. Skipping seed.")

async def main():
    await init_models()
    await seed_data()

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    logger.info("Initiating Project Orbit Genesis Protocol...")
    asyncio.run(main())
