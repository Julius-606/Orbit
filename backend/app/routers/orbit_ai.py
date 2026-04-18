################################################################################
# FILE: backend/app/routers/orbit_ai.py
# VERSION: 4.4.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Voice / Chat Endpoint - Task Management & Memory Execution
################################################################################

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.db.session import get_db
from app.models.study import StudyTask, BrainRotLevel
from app.services.orbit_brain import OrbitAssistant
import logging

logger = logging.getLogger("Orbit-Voice")

router = APIRouter(prefix="/orbit", tags=["Orbit-AI"])

class ChatMessage(BaseModel):
    role: str # "user" or "model"
    content: str

class ChatRequest(BaseModel):
    message: str
    history: Optional[List[ChatMessage]] = []

class ChatResponse(BaseModel):
    reply: str
    status: str = "success"

@router.post("/converse", response_model=ChatResponse)
async def converse_with_orbit(request: ChatRequest, db: AsyncSession = Depends(get_db)):
    """The main neural link for talking to Orbit. Now with memory and schedule control!"""
    try:
        assistant = OrbitAssistant(db_session=db)

        user_msg = request.message

        # Run the AI chat with history
        ai_reply = await assistant.chat(
            user_msg,
            history=[{"role": h.role, "parts": [h.content]} for h in request.history] if request.history else []
        )

        # 1. Handle Task Creation
        if assistant.tasks_to_create:
            for task_data in assistant.tasks_to_create:
                new_task = StudyTask(
                    title=task_data["title"],
                    subject=task_data["subject"],
                    due_date=task_data.get("due_date"),
                    brain_rot_level=task_data["brain_rot_level"],
                    is_reminder=task_data.get("is_reminder", False),
                    remarks=task_data.get("remarks")
                )
                db.add(new_task)
            logger.info(f"W Secured: Created {len(assistant.tasks_to_create)} tasks! 🎯")

        # 2. Handle Task Updates
        if assistant.tasks_to_update:
            for update_data in assistant.tasks_to_update:
                task_id = update_data["task_id"]
                updates = update_data["updates"]

                result = await db.execute(select(StudyTask).where(StudyTask.id == task_id))
                task = result.scalars().first()
                if task:
                    for key, value in updates.items():
                        if key == "brain_rot_level" and value:
                            rot_map = {"chill": BrainRotLevel.CHILL, "mid": BrainRotLevel.MID, "cooked": BrainRotLevel.COOKED}
                            setattr(task, key, rot_map.get(value.lower(), BrainRotLevel.MID))
                        elif hasattr(task, key):
                            setattr(task, key, value)
            logger.info(f"Orbit updated {len(assistant.tasks_to_update)} tasks.")

        # 3. Handle Task Deletions
        if assistant.tasks_to_delete:
            for task_id in assistant.tasks_to_delete:
                result = await db.execute(select(StudyTask).where(StudyTask.id == task_id))
                task = result.scalars().first()
                if task:
                    await db.delete(task)
            logger.info(f"Orbit deleted {len(assistant.tasks_to_delete)} tasks.")

        await db.commit()
        return ChatResponse(reply=ai_reply)

    except Exception as e:
        logger.error(f"Orbit's brain crashed: {str(e)}")
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"Orbit's brain crashed: {str(e)}")
