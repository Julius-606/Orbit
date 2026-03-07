################################################################################
# FILE: backend/app/routers/orbit_ai.py
# VERSION: 4.0.3 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Voice / Chat Endpoint
################################################################################

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.session import get_db
from app.models.study import StudyTask
from app.services.orbit_brain import OrbitAssistant
import asyncio
import logging

logger = logging.getLogger("Orbit-Voice")

router = APIRouter(prefix="/orbit", tags=["Orbit-AI"])

class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    reply: str
    status: str = "success"

@router.post("/converse", response_model=ChatResponse)
async def converse_with_orbit(request: ChatRequest, db: AsyncSession = Depends(get_db)):
    """The main neural link for talking to Orbit."""
    try:
        # 🚀 BUG FIX: Pass the DB session explicitly just in case old code expects it!
        assistant = OrbitAssistant(db_session=db)

        # Run the AI chat in a separate thread so it doesn't freeze FastAPI
        ai_reply = await asyncio.to_thread(assistant.chat, request.message)

        # 🚀 BUG FIX: Safely insert all the tasks Gemini queued up!
        if hasattr(assistant, 'tasks_to_create') and assistant.tasks_to_create:
            for task_data in assistant.tasks_to_create:
                new_task = StudyTask(
                    title=task_data["title"],
                    subject=task_data["subject"],
                    brain_rot_level=task_data["brain_rot_level"]
                )
                db.add(new_task)

            # Commit exactly once, safely in the main event loop
            await db.commit()
            logger.info(f"W Secured: Committed {len(assistant.tasks_to_create)} new tasks to DB! 🎯")

        return ChatResponse(reply=ai_reply)

    except Exception as e:
        logger.error(f"Orbit's brain crashed: {str(e)}")
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"Orbit's brain crashed: {str(e)}")

@router.post("/seed", response_model=ChatResponse)
async def seed_tasks(db: AsyncSession = Depends(get_db)):
    """Secret endpoint to instantly populate your empty tracker."""
    try:
        # 🚀 BUG FIX: Pass the DB session explicitly!
        assistant = OrbitAssistant(db_session=db)
        prompt = "My tracker is completely empty bro. I'm slacking. Assign me essential tasks right now based on my 4 pillars (Med, Projects, Internship, Life), and do not forget the Friday Bible study with Mom."

        ai_reply = await asyncio.to_thread(assistant.chat, prompt)

        if hasattr(assistant, 'tasks_to_create') and assistant.tasks_to_create:
            for task_data in assistant.tasks_to_create:
                new_task = StudyTask(
                    title=task_data["title"],
                    subject=task_data["subject"],
                    brain_rot_level=task_data["brain_rot_level"]
                )
                db.add(new_task)
            await db.commit()

        return ChatResponse(reply=ai_reply)
    except Exception as e:
        logger.error(f"Failed to seed tasks: {str(e)}")
        await db.rollback()
        raise HTTPException(status_code=500, detail="Failed to secure the bag. Try again.")