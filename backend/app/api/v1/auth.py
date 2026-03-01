# ==========================================
# IDENTITY: The Ticket Booth / Auth API
# FILEPATH: backend/app/api/v1/auth.py
# COMPONENT: Backend API Routes
# ROLE: Hands out the access tokens so your devices can talk to the VM.
# VIBE: "Here's your VIP wristband, don't lose it." 🎫
# ==========================================

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from core.config import settings

router = APIRouter()

@router.post("/token")
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    """
    Your laptop/phone sends a username and password here. 
    If it matches the Vault, we give them a token.
    """
    # Hardcoded for now. We are the only user of Orbit!
    if form_data.username != "orbit_admin" or form_data.password != "admin_password":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password. Are you an opp?",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # Return the secret key as the token (in V4 we will use real JWTs)
    return {"access_token": settings.SECRET_KEY, "token_type": "bearer"}