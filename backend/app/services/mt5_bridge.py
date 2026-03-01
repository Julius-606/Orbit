# ==========================================
# IDENTITY: The Broker Handshake / MT5 Bridge
# FILEPATH: backend/app/services/mt5_bridge.py
# COMPONENT: Financial Integration
# ROLE: Logs into your MetaTrader 5 terminal to stalk your trades.
# VIBE: "Let's see if bro is over-leveraged on Gold again." 📉👀
# ==========================================

import MetaTrader5 as mt5
from core.config import settings
import logging
import pandas as pd

logger = logging.getLogger("MT5-Bridge")

class MT5Engine:
    def __init__(self):
        self.connected = False

    def connect(self):
        if not mt5.initialize():
            logger.error(f"MT5 Init failed. Error code: {mt5.last_error()}")
            return False

        # Login to the broker (Hopefully a funded prop firm account 💰)
        authorized = mt5.login(
            settings.MT5_LOGIN, 
            password=settings.MT5_PASSWORD, 
            server=settings.MT5_SERVER
        )
        
        if authorized:
            self.connected = True
            logger.info(f"MT5 Connected successfully to {settings.MT5_SERVER}. Let's get this bread.")
            return True
        else:
            logger.error(f"Failed to connect to MT5. Did you blow the account? Code: {mt5.last_error()}")
            return False

    def check_exposure(self):
        """
        Checks if you are risking too much of the account.
        """
        if not self.connected:
            self.connect()

        account_info = mt5.account_info()
        if account_info is None:
            return {"status": "error", "message": "Could not retrieve account info."}

        margin_level = account_info.margin_level
        equity = account_info.equity

        # If margin level drops below 300%, we sound the alarm
        if margin_level > 0 and margin_level < 300.0:
            return {"status": "DANGER", "message": "Margin Level critically low. Close some positions bro!"}
        
        return {"status": "CHILL", "equity": equity, "margin_level": margin_level}

# Global instance to import into the Heartbeat Worker
mt5_engine = MT5Engine()