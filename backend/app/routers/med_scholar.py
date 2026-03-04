################################################################################
#FILE: backend/app/routers/med_scholar.py
#VERSION: 3.1.3 | SYSTEM: Jarvis Protocol
################################################################################

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List, Dict, Any

# 🚀 FIX 1: Path fixes! Using app.db.session instead of database, and app.models.study
from app.db.session import get_db
from app.models.study import StudyTask

router = APIRouter(prefix="/api/v1/study/tasks", tags=["med-scholar"])

@router.get("/pending")
async def get_pending_tasks(db: AsyncSession = Depends(get_db)) -> List[Dict[str, Any]]:
    """
    Fetches pending study tasks.
    🚀 FIX 2: Upgraded to AsyncSession! The old synchronous db.query()
    would have crashed your async engine. Let's secure this bag properly.
    """
    result = await db.execute(select(StudyTask).where(StudyTask.completed == False))
    tasks = result.scalars().all()

    if not tasks:
        return []

    return [{
        "id": t.id,
        "title": t.title,
        "subject": t.subject,
        "brainRotLevel": getattr(t, 'brain_rot_level', 'LOW'),
        "isCompleted": getattr(t, 'completed', False),
        "dueDate": t.due_date.isoformat() if getattr(t, 'due_date', None) else None
    } for t in tasks]