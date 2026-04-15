################################################################################
# FILE: backend/app/routers/orbit_ai.py
# VERSION: 4.1.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Voice / Chat Endpoint - Dementia Fix Applied
################################################################################

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.session import get_db
from app.models.study import StudyTask
from app.services.orbit_brain import OrbitAssistant
import asyncio
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
    """The main neural link for talking to Orbit. Now with memory!"""
    try:
        assistant = OrbitAssistant(db_session=db)

        user_msg = request.message
        is_staged = user_msg.startswith("[STAGED]")

        # Inject context for staged messages
        if is_staged:
            logger.info("Processing [STAGED] message from offline sync.")
            # We'll let the AI know it's a late processing

        # Run the AI chat with history
        ai_reply = await asyncio.to_thread(
            assistant.chat,
            user_msg,
            history=[{"role": h.role, "parts": [h.content]} for h in request.history] if request.history else []
        )

        # Handle task creation from AI tools
        if hasattr(assistant, 'tasks_to_create') and assistant.tasks_to_create:
            for task_data in assistant.tasks_to_create:
                new_task = StudyTask(
                    title=task_data["title"],
                    subject=task_data["subject"],
                    brain_rot_level=task_data["brain_rot_level"],
                    is_reminder=task_data.get("is_reminder", False)
                )
                db.add(new_task)

            await db.commit()
            logger.info(f"W Secured: Committed {len(assistant.tasks_to_create)} tasks! 🎯")

        return ChatResponse(reply=ai_reply)

    except Exception as e:
        logger.error(f"Orbit's brain crashed: {str(e)}")
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"Orbit's brain crashed: {str(e)}")
