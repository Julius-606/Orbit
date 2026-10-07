---
title: Orbit & DebateHub Multi-Backend
emoji: 🪐
colorFrom: purple
colorTo: indigo
sdk: docker
app_port: 7860
---

# Orbit & DebateHub: Unified Multi-Backend Space 🪐 🏛️

This Hugging Face Space hosts two independent, isolated backends within a single container:

### 1. Project Orbit (Chief-of-Staff & Life-OS)
- **Engine:** FastAPI / Uvicorn (Python 3.11)
- **Public Dashboard:** Accessible at `/` (redirects to `/docs` for Swagger UI)
- **Core APIs:** `/api/v1/orbit/...`, `/api/v1/study/...`, `/api/v1/forex/...`, `/api/v1/tasks/...`
- **Database Secret:** `DATABASE_URL` / `ORBIT_DATABASE_URL` (PostgreSQL / Neon)

### 2. Project DebateHub (Debate Management & Real-Time Sync)
- **Engine:** Express & React / Vite (Node.js 20)
- **Public Gateway:** Accessible at `/DebateHub`
- **APIs & Live SSE:** `/DebateHub/api/...` and `/DebateHub/api/live/stream`
- **Database Secret:** `DEBATEHUB_NEON_DATABASE_URL` (Independent Neon DB instance)

Both projects run as separate processes with independent database connections, while sharing the Hugging Face Spaces port `7860`.
