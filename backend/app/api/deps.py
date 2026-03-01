# ==========================================
# IDENTITY: The API Bouncer / Dependencies
# FILEPATH: backend/app/api/deps.py
# COMPONENT: Security
# ROLE: Checks the VIP list before letting a device talk to the API.
# VIBE: "Let me see some ID before I let you touch the Forex Guardian." 🛑🛂
# ==========================================

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from core.config import settings
import logging

logger = logging.getLogger("API-Bouncer")

# We use a simple token check for now. Your laptop and phone will send this token.
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

async def verify_device_access(token: str = Depends(oauth2_scheme)):
    """
    If someone finds your server IP, this stops them from randomly adding
    fake tasks or closing your MT5 trades.
    """
    # In a real setup, you'd decode a JWT here. For now, we check against a static secret.
    if token != settings.SECRET_KEY:
        logger.warning("Intruder alert! Someone tried to breach Orbit with a fake token.")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Opps detected. Access denied.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return True
