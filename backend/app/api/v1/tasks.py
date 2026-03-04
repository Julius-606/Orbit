################################################################################
# FILE: backend/app/api/v1/tasks.py
# VERSION: 3.1.5 | SYSTEM: Jarvis Protocol
################################################################################
# IDENTITY: Task Management / Med-Scholar Router
# ROLE: Handles the CRUD for tasks, syllabus sync, and governor checks
# VIBE: Keeping the grind organized so we don't fail Clinicals or blow accounts.
################################################################################

from fastapi import APIRouter, Depends, HTTPException, status
from typing import List, Optional
import logging

# 🚀 THE BIG FIX: We slap 'app.' on every internal import!
# Python's trendline is fixed. We are now respecting the absolute path from /backend.
from app.services.governor import governor

# 🛑 THE NEW FIX: We commented out these imports!
# We haven't actually created the 'Life Admin' schemas and models yet.
# Trying to import them right now is like trying to withdraw profit from a demo account. 😭
# from sqlalchemy.orm import Session
# from app.db.session import get_db
# from app.schemas.task import TaskCreate, TaskResponse, TaskUpdate
# from app.models.task import Task

logger = logging.getLogger("orbit_tasks")
router = APIRouter()

@router.get("/", status_code=status.HTTP_200_OK)
async def get_all_tasks():
    """
    Fetch all active tasks.
    The Life-Governor checks if you should be studying Med or trading Forex. 📈
    """
    try:
        # The Governor doing a vibe check on your current chronotype/schedule
        current_vibe = governor.check_schedule() if hasattr(governor, 'check_schedule') else "Grind Mode Activated"
        logger.info(f"Tasks requested. Current Governor Vibe: {current_vibe}")

        # ⚠️ DB HOOKUP: When you connect Postgres, it looks like this:
        # tasks = db.query(Task).all()
        # return tasks

        return {
            "status": "bullish 📈",
            "message": "Tasks loaded successfully. Let's secure this bag.",
            "governor_status": current_vibe,
            "data": [
                {"id": 1, "title": "Review Pharma Flashcards", "type": "Med-Scholar", "status": "pending"},
                {"id": 2, "title": "Check XAUUSD 1H Order Block", "type": "Forex Guardian", "status": "active"},
                {"id": 3, "title": "Sync SHOFCO Community Data", "type": "Ops", "status": "pending"}
            ]
        }
    except Exception as e:
        logger.error(f"Task fetch liquidated: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch tasks. The VM is acting mid."
        )

@router.get("/pending", status_code=status.HTTP_200_OK)
async def get_pending_tasks():
    """
    Fetch only the pending tasks.
    The phone is hitting this endpoint hard like resistance on the 4H chart.
    """
    try:
        logger.info("Mobile just hit the /pending endpoint. Serving the pending bag.")

        # ⚠️ DB HOOKUP: When you connect Postgres, it looks like this:
        # tasks = db.query(Task).filter(Task.status == "pending").all()
        # return tasks

        return {
            "status": "bullish 📈",
            "message": "Pending tasks secured. No cap.",
            "data": [
                {"id": 1, "title": "Review Pharma Flashcards", "type": "Med-Scholar", "status": "pending"},
                {"id": 3, "title": "Sync SHOFCO Community Data", "type": "Ops", "status": "pending"}
            ]
        }
    except Exception as e:
        logger.error(f"Pending tasks fetch liquidated: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to fetch pending tasks. Stop loss hit."
        )

@router.post("/", status_code=status.HTTP_201_CREATED)
async def create_task(task_data: dict):
    """
    Inject a new task into the Brain. 🧠
    """
    try:
        logger.info(f"New task incoming: {task_data.get('title', 'Unknown Task')}")

        # ⚠️ DB HOOKUP:
        # new_task = Task(**task_data.dict())
        # db.add(new_task)
        # db.commit()
        # db.refresh(new_task)

        return {
            "status": "success",
            "message": "Task injected into Orbit. No cap. 🎯",
            "data": task_data
        }
    except Exception as e:
        logger.error(f"Failed to create task: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Task creation failed. Check your payload, bro."
        )

@router.delete("/{task_id}", status_code=status.HTTP_200_OK)
async def delete_task(task_id: int):
    """
    Wipe a task from existence once it's completed. Take profit hit! 💰
    """
    logger.info(f"Attempting to delete task ID: {task_id}")

    # ⚠️ DB HOOKUP:
    # task = db.query(Task).filter(Task.id == task_id).first()
    # if not task:
    #     raise HTTPException(status_code=404, detail="Task ghosted us. Not found.")
    # db.delete(task)
    # db.commit()

    return {
        "status": "deleted",
        "message": f"Task {task_id} has been wiped. TP hit, bag secured. ✅"
    }