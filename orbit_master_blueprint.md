======================================
FILE: 		Projects/Orbit/orbit_master_blueprint.md
VERSION:	 1.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
IDENTITY: 	The Master Implementation Roadmap
VIBE: 		Step-by-step guide to building a Tier-1
Life-OS without blowing the account. 📈🧱
======================================
ASSISTANT ORBIT
A place to scribble your ideas before they go mainstream.

🪐 The Vision
Orbit isn't just a task manager; it’s a Life-OS. It’s the secretary that knows your Medicine syllabus, the mentor that knows your SHOFCO projects, and the risk manager that watches your Forex charts while you sleep. We are moving from "reactive" to "proactive." Orbit won't wait for you to ask; it will tell you what’s up.
🧠 The Core Philosophy
VM First (The Brain): The server is the single source of truth. All heavy lifting—LLM processing, Forex monitoring, and schedule optimization—happens here.
Dual-Device Sync: Notifications are a "blast" protocol. If a trade hits SL or a lecture ends, both the Ubuntu Workstation and the Android Pocket Orbit light up. No cap.
Proactive Intelligence: Use the Context-Aware Trigger Engine (CATE) to check in on progress (e.g., "Yo, did that Pharma lecture have homework?").
Offline-First Clients: Phone and Laptop cache 3 days of data. Even if the internet is acting mid, the grind continues.
🛠️ System Pillars
Orbit-Speak: Context-aware voice interactions.
Forex Guardian: 24/7 VM-based trading monitor (MT5 integration).
Med-Scholar: Syllabus-aware study planning.
Life-Governor: Chronotype-based scheduling (Internal Medicine when you're fresh, coding when you're locked in).
Status: Architecture Phase (The "Paper" Era) Current Version: 3.0 (Jarvis Era)

Overview
Hi there. Remember project orbit? We are going to give it it's biggest update. Its going to evolve from AI learning tool, to an assistant, to a full-on SIRI, we are building our own version of 'Jarvis'. We are still in the planning phase so I need Ideas, lots of them and your contribution.

The concept
Thus programme will be my personal assistant, or in other words, my secretary. Unlike the typical ‘Tasks’ application,  it will have my syllabus (So it doesn't just tell me to study, it also tells me what to study), it will be kept up to date with my projects (So it doesn't just tell me ‘Youre free to work on your project,’ It knows ‘what updates are pending, ‘Today, on project X, we will be doing ‘abc’), making far more personal than your average task planner apps. This means it will need access to my calendar events. On top of that, it should know

Here’s what I have planned for its setup:

The home
A server, for it to be online, this will be its home. For this I want the VM I'm using to do this. The server is where all the decisions are made. If there is an added task that needs to be fitted in, it connects to an AI through an API, the AI is the one in charge of my schedule. If from my phone iI added a task (two case scenario) :
A. I didn't know what time to fit in a task, the server inquires from the AI, and the task is fitted in.
B. I fixed the task to a certain time so the server reanalyses the schedule adjusting the times and task.

The server will not just set the schedule, through the AI, the program should optimize this schedule for optimum productivity. Eg. Adjusting my time table so that after studying Internal Medicine for Heart diseases, a rather challenging unit, after a period where Im expected to be fresh. OR Let's say I have got an assignment that just came in and is due by two days, the program doesn't just place it anywhere, it makes it as ‘Urgent and gets to know what would be the best time for that task, in case there was a task that was replaced it knows when to reschedule. 

Android app
Pocket friendly version. This one knows all my basic stuff when to wake me up, when I sleep, when I take a dump 😅. Apart from those basics, the app will need to be able to connect to the server, it syncs, with any change that is universe, gets to speed. To see if the schedule has changed
The program has the schedule to my entire day. When a time comes to do something, eg: 
2100h - Time to touch on Anatomy of the Abdomen, Were looking at the innervation before we sleep.”
2230h - Wow, we gotthrough those nerves in a bleeze, but now it's time to rest. Think about your day.
(Very important: We want the app to be able to download cache from the server, so even if I go offline the phone and laptop still knows my schedule for like three days ahead. )

Laptop Interphase
Since this is where all the action is done. It has my notes, it's where I code. 


PROJECT ORBIT: THE JARVIS PROTOCOL - SYSTEM MANIFESTO (v3.0)
Status: Technical Deep-Dive / Proactive Intelligence Blueprint Architecture: Distributed "Always-On" Life-OS with Proactive Engagement Primary Objective: Synchronized balance across Medicine, SHOFCO, and Projects via proactive voice, web, and mobile interfaces.
1. THE "HEARTBEAT" ARCHITECTURE (VM & Sync)
The VM is the single source of truth. Every device (Phone/Laptop) is a satellite that pushes and pulls state from the Orbit-Core.
Data Exchange Protocol (The Harmony)
The Message Bus: We use WebSockets for real-time updates (e.g., a Forex "Take Profit" hit) and REST (FastAPI) for CRUD operations (e.g., adding a new Med note).
The Sync Logic: * Local State: The Android app stores every change in Room (SQLite) immediately.
The Reconciler: A background service checks for is_synced = false flags.
The Handshake:
Client: POST /sync {task_id, payload, timestamp}
VM: Checks last_updated. If VM is newer, it sends a 409 Conflict. If Client is newer, VM updates Postgres and broadcasts.
2. THE PROACTIVE ENGAGEMENT LAYER (Orbit-Speak)
Orbit doesn't just respond; it initiates. This is governed by the Context-Aware Trigger Engine (CATE).
A. The Interactive Trigger Logic
Orbit monitors your location (GPS), current task status, and schedule transitions to trigger "Check-ins."
The "Class-End" Trigger: When Orbit detects a "Pharmacology" class has ended (via time or GPS exit from the lecture hall), it triggers a voice prompt:
"Geri, that lecture just wrapped. Did the Pharmacology lecturer leave an assignment we need to bake into the schedule?"
The "Work-Vibe" Check: 2 hours into a SHOFCO shift:
"Yo Julius, how's the Library shift coming along? Any unexpected tasks, or are we still on track for our 5 PM Forex session?"
The "Wake-Up" Hype: Linked to the Alarm Manager:
"Morning Julius! The markets are looking spicy today. Let's get that blood running with 20 sit-ups before we dive into the Anatomy of the Abdomen. No cap, let's get it!"
(Feel free to add more triggers)
3. THE VOICE INTERACTION ENGINE (Orbit-Hear)
The NLP Pipeline:
VAD (Voice Activity Detection): The Android app detects you're talking.
STT (Speech-to-Text): Orbit uses OpenAI Whisper (on the VM) or Google STT (local) to transcribe.
Intent Parsing: The text is sent to the VM's LLM Agent (Gemini 2.5 Flash).
Action Execution: LLM returns JSON: {"action": "ADD_TASK", "content": "Pharma Assignment", "deadline": "2024-05-20"}.
Confirmation: Orbit speaks back: "Bet. I've added the assignment and carved out time tomorrow morning to crush it."
4. THE ANDROID "GUARDIAN" (Always-On Service)
Foreground Service: Keeps a persistent notification. Ensures Orbit isn't "killed" by the OS.
Proactive Audio Channel: Orbit uses a low-latency audio stream to "talk" even if the screen is off, using the Text-to-Speech (TTS) engine on the phone to save VM bandwidth for complex logic.
The Alarm Manager: Uses ExactAlarm API for millisecond-perfect timing for meds, trades, and wake-ups.
5. CORE CODE LOGIC (The "Gears")
VM: Proactive Question Logic (Python)
async def check_for_proactive_triggers(user_id: str):
    current_task = get_active_task(user_id)
    if current_task.type == "LECTURE" and is_just_finished(current_task):
        payload = {
            "type": "VOICE_PROMPT",
            "message": f"Did the {current_task.subject} lecturer leave an assignment?",
            "expected_intent": "TASK_CREATION"
        }
        await send_to_device(user_id, payload)

6. THE INTERFACE (The Dashboard)
A. The "Past & Future" Timeline
Upward Scroll: "The Archive." Past events, P&L, and quiz scores.
Downward Scroll: "The Horizon." Upcoming ward rounds, Forex news (NFP), and SHOFCO deadlines.
Interactive Prompts: Proactive questions appear as "Chat Bubbles" on the timeline if you miss the voice prompt.
7. SECURITY & ENCRYPTION
End-to-End: All voice data is encrypted via AES-256.
The Vault: API keys for MetaTrader 5 and Gemini are stored in the VM's .env.

8. 📈 FOREX GUARDIAN: THE "BAG" PROTECTOR
Since the bots live on the VM, the Guardian logic is now decentralized from the laptop to the "Always-On" server.
🎯 Primary Functions
MT5 Surveillance: Constant connection to MetaTrader 5 via the Python API.
Psych-Check: If your heart rate (via future smartwatch integration) is too high, the Guardian locks your "Close Trade" button to prevent revenge trading.
Risk Audit: Calculates your total exposure. If you're over-leveraged, Orbit will literally tell you through your speakers: "Julius, we're over-leveraged on GJ. Close a position, don't be a hero."
🔔 The "Dual-Blast" Notification Logic
We use a centralized notification dispatcher on the VM.
Logic Flow (Pseudo-Code)
async def broadcast_alert(user_id, message, priority="HIGH"):
    # 1. Update Database
    await log_notification(user_id, message)
    
    # 2. Blast to Android (Firebase Cloud Messaging)
    await send_fcm_push(user_id, message)
    
    # 3. Blast to Ubuntu (WebSocket)
    # The workstation client stays connected to ws://orbit-core/ws
    await websocket_manager.send_to_user(user_id, {
        "type": "DESKTOP_NOTIFICATION",
        "content": message,
        "urgency": priority
    })

🚨 Notification Scenarios
Event
Voice Prompt (Phone)
Desktop Toast (Ubuntu)
TP Hit
"Bag secured! Check the P&L."
💹 PROFIT: +$XX.XX
SL Hit
"Unlucky, we'll get 'em next time."
🛑 STOP LOSS: Trade Closed
News Alert
"NFP in 30 minutes. Watch the volatility."
⚠️ HIGH IMPACT NEWS


Popping up the hood
To see every task currently secured in the "Jarvis" vault, you have four distinct methods depending on whether you want a clean UI, raw data, or a mobile view.
1. Via Backend Swagger UI (Web Browser)
This is the most visual way to interact with your "Life-OS".
The URL: Open your browser and go to https://untropic-rozanne-noncomprehendingly.ngrok-free.dev/docs (or http://127.0.0.1:8000/docs if you are on your laptop).
The Steps:
Scroll down to the Syllabus Vault section.
Click on the GET /api/v1/study/tasks/pending endpoint.
Click "Try it out" and then "Execute".
The Result: You will see a JSON list of all uncompleted tasks, including the "Anatomy" one you just added.
2. Via Terminal (Backend API Call)
If you want to feel like a real dev on your HP Pavilion, use a curl command to hit the API directly.
The Command:
Bash
curl -X 'GET' 'http://127.0.0.1:8000/api/v1/study/tasks/pending' -H 'accept: application/json'


Pro Tip: To make the output readable (pretty-print), pipe it to Python's JSON tool:
Bash
curl -X 'GET' 'http://127.0.0.1:8000/api/v1/study/tasks/pending' | python3 -m json.tool


3. Via the Android App (Pocket Orbit)
This is for when you're at the clinic or library and need to check your queue.
The Setup: Ensure your RetrofitClient.kt is updated with your Ngrok URL as we discussed.
The Action:
Open Pocket Orbit on your phone.
Tap the Refresh icon (the circular arrow) in the top right corner.
The Result: The DashboardViewModel will trigger a sync, and the "Med-Scholar Queue" will populate with your tasks.
4. See the "Raw Running" (Terminal Database Logs)
If you want to see the "Matrix" actually processing the data in real-time as it hits the database, watch your uvicorn logs.
Where to look: Go to the terminal window where you ran uvicorn main:app --host 0.0.0.0 --port 8000 --reload.
What you'll see: Every time you refresh the app or hit the Swagger UI, you'll see a log line like this: INFO: 127.0.0.1:XXXXX - "GET /api/v1/study/tasks/pending HTTP/1.1" 200 OK.


THE CAMBARIAN REVOLUTION



THE RULE OF ENGAGEMENT
Every single step must result in a compilable, bug-free, usable application. If a feature breaks the build, we roll back, debug, and try again. No half-finished branches allowed.
🟢 PHASE 1: UI / UX QUALITY OF LIFE (The Frontend Scalps)
Goal: Make the current app feel buttery smooth and satisfying without touching complex background logic.
Step 1.1: The Sync Button (✅ COMPLETED)
What: Manual IconButton in the TopAppBar to refresh the Tracker view.
Step 1.2: Pull-to-Refresh & Swipe-to-Complete
What: Replace the manual button with native Compose pullRefresh. Add SwipeToDismiss on task cards (swipe right to complete, swipe left to ignore/snooze).
Why: Better dopamine hit after finishing Anatomy.
Step 1.3: Brain-Rot Sorting & The Graveyard
What: Sort tasks by priority (🔥 Cooked, 😐 Mid, 🧊 Chill). Add a toggle at the top of the Tracker to view "Completed Tasks" (The Graveyard) so you can see your daily Ws.
🟡 PHASE 2: THE VAULT (Offline-First & Safaricom Hedge)
Goal: Set up the local Room Database so the app survives network drops in Thika.
Step 2.1: Chat Persistence (Room DB)
What: Create ChatEntity and ChatDao. Build the Chat Screen UI so your conversation with Orbit is saved locally. Orbit stops having amnesia.
Step 2.2: Offline Task Caching
What: Add a sync_pending boolean to StudyTaskEntity. When you mark a task done offline, Room updates locally. When Wi-Fi connects, it silently pushes the update to the VM.
Step 2.3: The Message Staging Area
What: If you send Orbit a message offline, a bottom sheet asks to "Stage or Ignore." Staged messages are saved with a timestamp. Upon reconnection, a background worker fires them to the API and triggers a push notification with the AI's response.
🟠 PHASE 3: THE SNIPER (Zero-Slippage & AI Awareness)
Goal: Give Orbit strict control over your time and contextual awareness of your schedule.
Step 3.1: Zero-Slippage Notifications
What: Implement Android AlarmManager with setExactAndAllowWhileIdle(). If Pathology is at 2 PM, the alarm fires at exactly 2:00:00 PM. High-priority intents. No missed entries.
Step 3.2: API Hedging (Key Roulette)
What: Build an App Settings screen to input multiple AI API keys. If the primary key hits a rate limit (429), it silently fails over to the next key.
Step 3.3: Telescope Context Injection
What: Write a helper function that reads Room DB and appends your schedule to the hidden System Prompt. Today gets minute-by-minute resolution. Tomorrow onwards gets a daily summary.
🔴 PHASE 4: THE SURGEON (Function Calling & VM Automation)
Goal: Allow Orbit to actively manage your life, not just talk about it.
Step 4.1: VM Automated Limit Orders (Backend)
What: Python Cron jobs on the Ubuntu VM to handle RecurringTasks (Laundry, Bible Study). The VM generates them and pushes them to your phone automatically.
Cant we make the cronjob logic to be on the app, where it will be able to alert me whether online or offline, for better efficiency?
Step 4.2: AI Function Calling (Tool Use)
What: Map JSON schema functions so Orbit can execute update_schedule(), add_task(), or reschedule_block() directly into your Room DB based on natural language chat.
Step 4.3: Yap-to-Task (Audio Journaling)
What: Add a mic button to the Chat UI. Use Android's SpeechRecognizer (or a Whisper API) to turn your post-ward-round yaps into text, which Orbit then parses into actionable tasks.
🟣 PHASE 5: THE RISK MANAGER (Stop-Losses & Biology Hacks)
Goal: Prevent burnout, overtrading, and adapt to your biological glitches.
Step 5.1: Task Stop-Loss System
What: A strict timer on active tasks. If you exceed the allocated time by 15 mins, Orbit fires an aggressive notification: "Stop-Loss hit. Drop it. Moving to next subject."
Step 5.2: The Midnight Spawner Protocol
What: If the app detects a screen unlock between 1 AM and 4 AM, it overrides the home screen with "Vampire Mode" (Dark red UI, suggests light flashcards or checking the Asian session).
Step 5.3: The Red Folder Protocol
What: If an exam/CAT is detected within 48 hours, Orbit auto-purges low-priority tasks (laundry, casual coding) and strictly enforces revision blocks.
🌌 PHASE 6: THE HEDGE FUND (Cross-Device Burnout Monitor)
Goal: The final boss. Connecting the phone, the laptop, and the gamification engine.
Step 6.1: Android Usage Stats Service
What: Implement Android UsageStatsManager to track what apps you are actively using (e.g., MT5, TikTok, Chrome) to measure mental drain.
Step 6.2: Workstation Sync & Mental Margin Call
What: Sync laptop screentime with phone screentime via the VM. If your total "Heavy Cognitive Load" hits the daily limit, Orbit locks the schedule and enforces a mandatory touch-grass break.
Step 6.3: Prop Firm Gamification
What: Implement a weekly "P&L" score. Good execution = high score. Missing tasks = drawdown. End the week in profit to unlock a 4-hour guilt-free "Take Profit" block.
================================================================================

