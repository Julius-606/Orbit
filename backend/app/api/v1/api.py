################################################################################
# FILE: backend/app/api/v1/api.py
# VERSION: 1.1.0 (SYLLABUS SYNC PATCH)
################################################################################

from fastapi import APIRouter
# We are importing the versioned routers to ensure data integrity
from app.api.v1 import forex, tasks
from app.routers import med_scholar # THE FIX: Importing the enhanced router

api_router = APIRouter()

# Mount the Forex Guardian 📈
api_router.include_router(forex.router, prefix="/forex", tags=["Risk Management"])

# Mount the Med-Scholar (The Dean) 🩺
# THE FIX: Pointing to the router that supports the full Kotlin Entity schema
api_router.include_router(med_scholar.router, tags=["Syllabus Vault"])

# Mount the Life Tasks 🌍
api_router.include_router(tasks.router, prefix="/tasks", tags=["Life Admin"])

# Vibe Check: All routes are now synchronized with the Android Entity structure. 🚀