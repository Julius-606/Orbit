# ==========================================
# IDENTITY: The To-Do List / Life Tasks API
# FILEPATH: backend/app/api/v1/tasks.py
# COMPONENT: Backend API Routes
# ROLE: Manages general tasks (SHOFCO, coding, buying groceries).
# VIBE: Because you can't pay for prop firm challenges if you forget to do your real-life work. 📝
# ==========================================

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List
from services.governor import governor

router = APIRouter()

# Schema (Fake DB for now, we'll hook it to Postgres later)
class LifeTask(BaseModel):
    id: int
    title: str
    category: str # e.g., "SHOFCO", "CODING", "LIFE"
    is_urgent: bool = False

# Temporary memory bank
db_mock = []

@router.post("/add")
async def add_task(task: LifeTask):
    db_mock.append(task)
    return {"status": "W", "message": f"Task '{task.title}' secured in the vault."}

@router.get("/current-vibe")
async def what_should_i_do():
    """
    Hits the Life-Governor to tell you what you should actually be doing right now.
    """
    advice = governor.get_current_recommendation()
    return {"governor_says": advice}

@router.get("/all", response_model=List[LifeTask])
async def get_all_tasks():
    if not db_mock:
        return []
    return db_mock