# ==========================================
# IDENTITY: The Switchboard / Main API Router
# FILEPATH: backend/app/api/v1/api.py
# COMPONENT: Backend Routing
# ROLE: Glues all your mini-APIs (Forex, Med, Tasks) into one massive brain.
# VIBE: The air traffic controller making sure your trades and study habits don't crash into each other. ✈️💥
# ==========================================

from fastapi import APIRouter
from api.v1 import forex, med_scholar, tasks

api_router = APIRouter()

# Mount the Forex Guardian
api_router.include_router(forex.router, prefix="/forex", tags=["Risk Management"])

# Mount the Med-Scholar (Dean)
api_router.include_router(med_scholar.router, prefix="/study", tags=["Syllabus Vault"])

# Mount the Life Tasks
api_router.include_router(tasks.router, prefix="/tasks", tags=["Life Admin"])

# Now you just hit /api/v1/forex/alert or /api/v1/study/tasks 
# Clean, modular, and W architecture.