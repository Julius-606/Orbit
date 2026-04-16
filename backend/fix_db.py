import asyncio
from sqlalchemy import text
from app.db.session import engine

async def run_migration():
    async with engine.begin() as conn:
        print("Checking database columns...")
        # Check if remarks column exists
        result = await conn.execute(text("""
            SELECT column_name
            FROM information_schema.columns
            WHERE table_name='study_tasks' AND column_name='remarks';
        """))
        exists = result.fetchone()

        if not exists:
            print("Adding 'remarks' column to 'study_tasks'...")
            await conn.execute(text("ALTER TABLE study_tasks ADD COLUMN remarks TEXT;"))
            print("Column 'remarks' added.")
        else:
            print("Column 'remarks' already exists.")

        # Also check for is_reminder
        result = await conn.execute(text("""
            SELECT column_name
            FROM information_schema.columns
            WHERE table_name='study_tasks' AND column_name='is_reminder';
        """))
        exists = result.fetchone()

        if not exists:
            print("Adding 'is_reminder' column to 'study_tasks'...")
            await conn.execute(text("ALTER TABLE study_tasks ADD COLUMN is_reminder BOOLEAN DEFAULT FALSE;"))
            print("Column 'is_reminder' added.")
        else:
            print("Column 'is_reminder' already exists.")

if __name__ == "__main__":
    asyncio.run(run_migration())
