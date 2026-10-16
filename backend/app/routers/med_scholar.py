################################################################################
#FILE: backend/app/routers/med_scholar.py
#VERSION: 3.1.5 | SYSTEM: Jarvis Protocol
################################################################################

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List, Dict, Any
import logging

from app.db.session import get_db
from app.models.study import StudyTask

logger = logging.getLogger("Orbit-MedScholar")

router = APIRouter(prefix="/study/tasks", tags=["med-scholar"])

@router.get("/pending")
async def get_pending_tasks(db: AsyncSession = Depends(get_db)) -> List[Dict[str, Any]]:
    """Fetches pending study tasks."""
    result = await db.execute(select(StudyTask).where(StudyTask.completed == False))
    tasks = result.scalars().all()

    if not tasks:
        return []

    return [{
        "id": t.id,
        "title": t.title,
        "subject": t.subject,
        "brainRotLevel": getattr(t, 'brain_rot_level', 'LOW').name if hasattr(getattr(t, 'brain_rot_level', 'LOW'), 'name') else str(getattr(t, 'brain_rot_level', 'LOW')).split('.')[-1],
        "isCompleted": getattr(t, 'completed', False),
        "isReminder": getattr(t, 'is_reminder', False),
        "remarks": getattr(t, 'remarks', None),
        "dueDate": t.due_date.isoformat() if getattr(t, 'due_date', None) else None
    } for t in tasks]

@router.post("/", status_code=status.HTTP_201_CREATED)
async def create_study_task(payload: Dict[str, Any], db: AsyncSession = Depends(get_db)):
    """Manual interface task/reminder injector for mobile & assistant."""
    from app.models.study import BrainRotLevel
    from datetime import datetime
    try:
        title = payload.get("title", "Study Task")
        subject = payload.get("subject", "General Med")
        raw_rot = payload.get("brainRotLevel", payload.get("brain_rot_level", "chill"))
        rot_map = {"chill": BrainRotLevel.CHILL, "mid": BrainRotLevel.MID, "cooked": BrainRotLevel.COOKED}
        safe_rot = rot_map.get(str(raw_rot).lower(), BrainRotLevel.MID)

        due_date = None
        raw_date = payload.get("dueDate", payload.get("due_date"))
        if raw_date:
            try:
                due_date = datetime.fromisoformat(str(raw_date).replace("Z", "+00:00"))
            except Exception:
                due_date = None

        new_task = StudyTask(
            title=title,
            subject=subject,
            brain_rot_level=safe_rot,
            is_reminder=bool(payload.get("isReminder", payload.get("is_reminder", False))),
            remarks=payload.get("remarks"),
            due_date=due_date
        )
        db.add(new_task)
        await db.commit()
        await db.refresh(new_task)
        return {
            "status": "success",
            "message": "Task secured in Orbit.",
            "task": {
                "id": new_task.id,
                "title": new_task.title,
                "subject": new_task.subject,
                "brainRotLevel": safe_rot.name,
                "isReminder": new_task.is_reminder,
                "dueDate": new_task.due_date.isoformat() if new_task.due_date else None
            }
        }
    except Exception as e:
        logger.error(f"Task injection error: {e}")
        await db.rollback()
        raise HTTPException(status_code=400, detail=f"Failed to create task: {e}")

# 🔥 THE FIX: The VM endpoint to actually receive the completion signal and update Postgres!
@router.put("/{task_id}/complete", status_code=status.HTTP_200_OK)
async def complete_task(task_id: int, db: AsyncSession = Depends(get_db)):
    """Marks a task as completed in the Matrix."""
    result = await db.execute(select(StudyTask).where(StudyTask.id == task_id))
    task = result.scalars().first()

    if not task:
        logger.warning(f"Attempted to complete ghost task ID: {task_id}")
        raise HTTPException(status_code=404, detail="Task not found. Bro is hallucinating.")

    task.completed = True
    await db.commit()

    logger.info(f"W Secured! Task '{task.title}' marked as completed. Let's go! 🚀")

    # Ideally, we trigger a Blast Protocol here to update the Workstation UI too!
    return {"status": "success", "message": "Bag secured. Task wiped."}