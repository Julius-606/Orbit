import os
import time

def create_structure():
    print("🚀 INIT: Firing up the Jarvis Protocol (v3.0)...")
    time.sleep(10)
    print("💼 Checking Forex charts... Nah, we coding today. No cap.")
    time.sleep(10)
    
    # The Map: Directories and Files
    # Directories ending with '/' will only create the folder.
    # Anything else will create the folder path AND a blank file.
    structure = [
        # 1. THE BRAIN (Backend / VM)
        "backend/app/main.py",
        "backend/app/api/v1/auth.py",
        "backend/app/api/v1/medicine.py",
        "backend/app/api/v1/forex.py",
        "backend/app/api/v1/life.py",
        "backend/app/agents/med_scholar/",
        "backend/app/agents/forex_guardian/",
        "backend/app/agents/life_governor/",
        "backend/app/core/notifications.py",
        "backend/app/core/config.py",
        "backend/app/core/security.py",
        "backend/app/db/session.py",
        "backend/app/db/models.py",
        "backend/app/db/redis_cache.py",
        "backend/app/schemas/",
        "backend/app/utils/",
        "backend/migrations/",
        "backend/tests/",
        "backend/Dockerfile",
        "backend/requirements.txt",

        # 2. POCKET ORBIT (Android App)
        "mobile/app/src/main/java/com/orbit/ui/theme/",
        "mobile/app/src/main/java/com/orbit/ui/home/",
        "mobile/app/src/main/java/com/orbit/ui/medicine/",
        "mobile/app/src/main/java/com/orbit/ui/trading/",
        "mobile/app/src/main/java/com/orbit/data/local/AppDatabase.kt",
        "mobile/app/src/main/java/com/orbit/data/local/daos/",
        "mobile/app/src/main/java/com/orbit/data/remote/OrbitApiService.kt",
        "mobile/app/src/main/java/com/orbit/data/repository/TaskRepository.kt",
        "mobile/app/src/main/java/com/orbit/di/",
        "mobile/app/src/main/java/com/orbit/model/",
        "mobile/app/src/main/java/com/orbit/workers/",

        # 3. THE FORGE (Workstation Client)
        "workstation/src/main.py",
        "workstation/src/hooks/vscode_sync.py",
        "workstation/src/hooks/mt5_bridge.py",
        "workstation/src/ui/",
        "workstation/config/",
        "workstation/scripts/",

        # 4. THE DEPLOYMENT (VM Operations)
        "infrastructure/docker-compose.yml",
        "infrastructure/nginx/",
        "infrastructure/.env.example"
    ]

    for item in structure:
        # Check if it's meant to be just a directory
        if item.endswith("/"):
            os.makedirs(item, exist_ok=True)
            print(f"📁 Created folder: {item}")
        else:
            # It's a file, so we need the directory path first
            dir_name = os.path.dirname(item)
            if dir_name:
                os.makedirs(dir_name, exist_ok=True)
            
            # Touch the file to keep it blank
            with open(item, 'w') as f:
                pass
            print(f"📄 Touched file:  {item}")

    print("\n" + "="*50)
    print("🎀 RIBBON CUT! THE JARVIS ERA HAS BEGUN! 🎀")
    print("="*50)
    print("Orbit Repo Structure successfully generated.")
    print("Time to secure the bag, pass Medicine, and code this beast!")
    print("WAGMI. 📈✨")

if __name__ == "__main__":
    create_structure()