################################################################################
# FILE: backend/app/models/task.py
# VERSION: 4.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
# IDENTITY: The Vault / Database Schema
################################################################################

from sqlalchemy import Column, Integer, String, Boolean, DateTime, Enum
from sqlalchemy.sql import func
import enum
from app.db.session import Base

class LifePillar(str, enum.Enum):
    STUDY = "STUDY"           # Med School, CATs, Pharma
    PROJECT = "PROJECT"       # Coding, SHOFCO
    INTERNSHIP = "INTERNSHIP" # Work hours, deliverables
    LIFE = "LIFE"             # Bible study, Fasting, Social

class Task(Base):
    __tablename__ = "orbit_tasks"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True, nullable=False)
    pillar = Column(Enum(LifePillar), nullable=False, default=LifePillar.LIFE)
    description = Column(String, nullable=True)
    
    # AI scheduling fields
    due_date = Column(DateTime(timezone=True), nullable=True)
    duration_minutes = Column(Integer, default=60)
    
    # Status
    is_completed = Column(Boolean, default=False)
    is_recurring = Column(Boolean, default=False) # For that Tuesday/Friday Bible Study!
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "pillar": self.pillar.value,
            "description": self.description,
            "due_date": self.due_date.isoformat() if self.due_date else None,
            "duration_minutes": self.duration_minutes,
            "is_completed": self.is_completed,
            "is_recurring": self.is_recurring
        }
