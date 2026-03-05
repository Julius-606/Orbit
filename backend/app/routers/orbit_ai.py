################################################################################
# FILE: backend/app/routers/orbit_ai.py
# VERSION: 4.0.1 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Voice / Chat Endpoint
################################################################################

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.services.orbit_brain import OrbitAssistant
import logging

logger = logging.getLogger("Orbit-Voice")

# 🚀 THE REBRAND: Goodbye Jarvis, Hello Orbit!
router = APIRouter(prefix="/api/v1/orbit", tags=["Orbit-AI"])

class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    reply: str
    status: str = "success"

@router.post("/converse", response_model=ChatResponse)
async def converse_with_orbit(request: ChatRequest, db: Session = Depends(get_db)):
    """The main neural link for talking to Orbit."""
    try:
        # Spin up the assistant with the current DB session
        assistant = OrbitAssistant(db_session=db)
        
        # Get the big brain response
        ai_reply = assistant.chat(request.message)
        
        return ChatResponse(reply=ai_reply)
    except Exception as e:
        logger.error(f"Orbit's brain crashed: {str(e)}")
        # We don't want a 500 error crashing the app like a blown prop firm account 💀
        raise HTTPException(status_code=500, detail=f"Orbit's brain crashed: {str(e)}")

@router.post("/seed", response_model=ChatResponse)
async def seed_tasks(db: Session = Depends(get_db)):
    """Secret endpoint to instantly populate your empty tracker."""
    assistant = OrbitAssistant(db_session=db)
    prompt = "My tracker is completely empty bro. I'm slacking. Assign me essential tasks right now based on my 4 pillars (Med, Projects, Internship, Life), and do not forget the Friday Bible study with Mom."
    
    try:
        ai_reply = assistant.chat(prompt)
        return ChatResponse(reply=ai_reply)
    except Exception as e:
        logger.error(f"Failed to seed tasks: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to secure the bag. Try again.")
