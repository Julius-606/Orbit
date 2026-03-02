import os
import re
from datetime import datetime

# ==========================================
# IDENTITY: The Architect / The Scribe
# FILEPATH: orbit_bundler.py
# COMPONENT: Project Infrastructure
# ROLE: The "Big Bang" script. Compiles all project files into a single context 
#       snapshot for AI interaction.
# VERSION: 1.0.2
# SYSTEM VERSION: Backend v3.0.0
# VIBE: The historian of your Life-OS. It remembers everything so you don't have to.
# ==========================================

# --- CONFIGURATION ---
# The name of our massive source of truth
OUTPUT_FILENAME = "ORBIT_SNAPSHOT.txt"

# Folders to scan. Since we are in the root /Projects/Orbit/, we point to the main dirs.
# We include '.' to catch files in the root (like this one).
PROJECT_DIRECTORIES = ["backend", "android", "Pocket_Orbit", "workstation", "infrastructure", "."]

# Extensions we actually care about (no junk allowed in the room)
ALLOWED_EXTENSIONS = {
    '.py', '.kt', '.xml', '.yml', '.yaml', '.txt', 
    '.md', '.sql', '.gradle', '.kts', '.ini', '.conf', '.dockerfile'
}

# Folders to ignore (keep the noise out)
IGNORE_DIRS = {
    '.git', '__pycache__', 'build', '.gradle', 'node_modules', 
    '.venv', 'venv', '.idea', 'bin', 'obj', 'outputs', 'intermediates'
}

AI_INSTRUCTIONS = f"""
================================================================================
SYSTEM INSTRUCTIONS FOR PROJECT ORBIT (THE JARVIS PROTOCOL)
================================================================================
DATE: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
STATUS: VERSION 3.0 (JARVIS ERA)



INSTRUCTIONS FOR THE AI ASSISTANT:
1. SINGLE SOURCE OF TRUTH: This file contains the latest version of all files 
   in the Orbit project. Treat this as the primary context for all decisions.
2. EDIT PROTOCOL: Every time you make an edit, suggest a change, or generate 
   new code, you MUST:
   - Explicitly list which files have been modified.
   - Increment the VERSION number (Major.Minor.Patch) in the IDENTITY header.
   - If it's a core component, update the SYSTEM VERSION (e.g., Backend v3.x.x).
3. PATHING INTEGRITY: Do not change the directory structure or file paths 
   unless it is strictly necessary for the architecture. 
4. CONTEXT AWARENESS: Remember that this is a distributed system (VM Brain, 
   Android Pocket, Ubuntu Workstation). Ensure all logic stays synchronized.
5. NO ARCHITECTURAL HALLUCINATIONS: If a file isn't in this snapshot, it 
   either doesn't exist, or is intentionally left outdue to confidenciality.
   If you create one, define its full path immediately.



================================================================================
FILE MANIFEST & CONTENT BEGINS BELOW
================================================================================
"""

def extract_versions(content):
    """
    Extracts versioning data from the IDENTITY block using regex.
    Returns (file_version, system_version)
    """
    # Look for file version: VERSION: 1.2.3
    f_match = re.search(r"VERSION:\s*(\d+\.\d+\.\d+)", content)
    file_version = f_match.group(1) if f_match else "1.0.0"
    
    # Look for system version: SYSTEM VERSION: Backend v3.0.0
    s_match = re.search(r"SYSTEM VERSION:\s*([\w\s]+v\d+\.\d+\.\d+)", content)
    system_version = s_match.group(1) if s_match else "N/A"
    
    return file_version, system_version

def bundle_project():
    print(f"🚀 Initializing Orbit Bundler... Target: {OUTPUT_FILENAME}")
    file_count = 0
    bundled_paths = set() # Avoid double-bundling
    
    # Get the absolute path of the root to avoid issues
    root_dir = os.getcwd()

    with open(OUTPUT_FILENAME, "w", encoding="utf-8") as snapshot:
        # Write the AI Instructions at the top
        snapshot.write(AI_INSTRUCTIONS.strip() + "\n\n")
        
        # Walk through the directories defined in configuration
        for target_dir in PROJECT_DIRECTORIES:
            target_path = os.path.join(root_dir, target_dir)
            
            if not os.path.exists(target_path):
                # Don't spam warnings for common variations (e.g., android vs Pocket_Orbit)
                continue
                
            for root, dirs, files in os.walk(target_path):
                # Prune ignored directories in-place
                dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
                
                for file in files:
                    # Skip the output file itself and the script if it's already bundled
                    if file == OUTPUT_FILENAME:
                        continue
                        
                    ext = os.path.splitext(file)[1].lower()
                    if file.lower() == "dockerfile":
                        ext = ".dockerfile"
                        
                    if ext in ALLOWED_EXTENSIONS or file == "Dockerfile":
                        full_path = os.path.join(root, file)
                        # Create a clean relative path from the project root
                        rel_path = os.path.relpath(full_path, root_dir)
                        
                        if rel_path in bundled_paths:
                            continue
                            
                        try:
                            with open(full_path, "r", encoding="utf-8", errors="ignore") as f:
                                content = f.read()
                            
                            f_version, s_version = extract_versions(content)
                                
                            # Write File Header with Advanced Version Control
                            snapshot.write(f"\n{'#'*80}\n")
                            snapshot.write(f"FILE: {rel_path}\n")
                            snapshot.write(f"IDENTITY: {file}\n")
                            snapshot.write(f"FILE VERSION: {f_version}\n")
                            snapshot.write(f"SYSTEM VERSION: {s_version}\n")
                            snapshot.write(f"{'#'*80}\n\n")
                            
                            # Write Content
                            snapshot.write(content)
                            snapshot.write("\n\n")
                            
                            bundled_paths.add(rel_path)
                            file_count += 1
                            print(f"✅ Bundled [{f_version}]: {rel_path}")
                        except Exception as e:
                            print(f"❌ Error reading {rel_path}: {e}")

    print(f"\n✨ Mission Accomplished! {file_count} files bundled into {OUTPUT_FILENAME}")
    print("📈 Pathing fixed. Versions locked. We're back in the game.")

if __name__ == "__main__":
    bundle_project()
