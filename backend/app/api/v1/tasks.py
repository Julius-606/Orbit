################################################################################
# FILE: backend/app/api/v1/tasks.py
# VERSION: 3.1.7 | SYSTEM: Orbit Protocol
# IDENTITY: Task Management / Life Admin Router
################################################################################

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from typing import List, Optional
import logging

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.db.session import get_db
from app.models.study import StudyTask

from app.services.governor import governor

logger = logging.getLogger("orbit_tasks")
router = APIRouter()

class TaskCreatePayload(BaseModel):
    title: str
    type: str = "Life Admin"
    status: str = "pending"

@router.get("/", status_code=status.HTTP_200_OK)
async def get_all_tasks(db: AsyncSession = Depends(get_db)):
    """Fetch all active tasks."""
    try:
        current_vibe = governor.get_current_recommendation() if hasattr(governor, 'get_current_recommendation') else "Grind Mode Activated"
        logger.info(f"Tasks requested. Current Governor Vibe: {current_vibe}")

        # 🚀 BUG FIX: Actually fetching from PostgreSQL instead of hardcoded data
        result = await db.execute(select(StudyTask))
        tasks = result.scalars().all()

        task_list = [
            {
                "id": t.id,
                "title": t.title,
                "type": t.subject,
                "status": "completed" if getattr(t, 'completed', False) else "pending"
            } for t in tasks
        ]

        return {
            "status": "bullish 📈",
            "message": "Tasks loaded successfully. Let's secure this bag.",
            "governor_status": current_vibe,
            "data": task_list
        }
    except Exception as e:
        logger.error(f"Task fetch liquidated: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to fetch tasks. The VM is acting mid.")

@router.get("/pending", status_code=status.HTTP_200_OK)
async def get_pending_tasks(db: AsyncSession = Depends(get_db)):
    """Fetch only the pending tasks."""
    try:
        logger.info("Mobile hit /pending. Serving the pending bag.")

        # 🚀 BUG FIX: Actually filtering the DB for pending tasks
        result = await db.execute(select(StudyTask).where(StudyTask.completed == False))
        tasks = result.scalars().all()

        task_list = [
            {
                "id": t.id,
                "title": t.title,
                "type": t.subject,
                "status": "pending"
            } for t in tasks
        ]

        return {
            "status": "bullish 📈",
            "message": "Pending tasks secured. No cap.",
            "data": task_list
        }
    except Exception as e:
        logger.error(f"Pending tasks fetch liquidated: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to fetch pending tasks. Stop loss hit.")

@router.post("/", status_code=status.HTTP_201_CREATED)
async def create_task(task_data: TaskCreatePayload, db: AsyncSession = Depends(get_db)):
    """Inject a new task into the Brain. 🧠"""
    try:
        logger.info(f"New task incoming: {task_data.title}")
        new_task = StudyTask(
            title=task_data.title,
            subject=task_data.type
        )
        db.add(new_task)
        await db.commit()

        return {
            "status": "success",
            "message": "Task injected into Orbit. No cap. 🎯",
            "data": task_data.dict()
        }
    except Exception as e:
        logger.error(f"Failed to create task: {str(e)}")
        await db.rollback()
        raise HTTPException(status_code=400, detail="Task creation failed. Check your payload, bro.")

@router.delete("/{task_id}", status_code=status.HTTP_200_OK)
async def delete_task(task_id: int, db: AsyncSession = Depends(get_db)):
    """Wipe a task from existence once it's completed. Take profit hit! 💰"""
    try:
        logger.info(f"Attempting to delete task ID: {task_id}")
        result = await db.execute(select(StudyTask).where(StudyTask.id == task_id))
        task = result.scalars().first()
        if task:
            await db.delete(task)
            await db.commit()
            return {
                "status": "deleted",
                "message": f"Task {task_id} has been wiped. TP hit, bag secured. ✅"
            }
        return {"status": "error", "message": "Task not found."}
    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=500, detail="Error deleting task.")