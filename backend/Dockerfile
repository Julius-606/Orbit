################################################################################
# FILE: backend/Dockerfile
# VERSION: 2.0.0 | SYSTEM: Hugging Face Spaces Multi-Backend (Orbit & DebateHub)
################################################################################
#
# Changes:
# - Added Node.js 20 LTS for DebateHub Express/Vite backend.
# - Preserved Python 3.11 for Orbit Life-OS FastAPI backend.
# - Hugging Face Space exposes port 7860:
#   * Orbit Dashboard & Swagger UI: / & /docs
#   * DebateHub Backend & App: /DebateHub (proxied from port 3000)
# - Independent DB secrets: DATABASE_URL (Orbit) and DEBATEHUB_NEON_DATABASE_URL (DebateHub).

FROM python:3.11-slim

WORKDIR /code

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install system dependencies + Node.js 20
RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
    gcc \
    curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir -r /code/requirements.txt

# Set up a new user (Hugging Face strictly requires a non-root user with ID 1000)
RUN useradd -m -u 1000 orbituser

# Set home to the user's home directory
ENV HOME=/home/orbituser \
    PATH=/home/orbituser/.local/bin:$PATH

WORKDIR $HOME/app

# Copy the entire workspace into user home
COPY --chown=orbituser:orbituser . $HOME/app

USER orbituser

# Install DebateHub dependencies if present
RUN if [ -d "$HOME/app/DebateHub" ]; then \
        cd $HOME/app/DebateHub && \
        (npm install --production=false || npm install --legacy-peer-deps || true); \
    fi

# Hugging Face default port
EXPOSE 7860

# Start both backends via runner script
CMD ["bash", "run_monorepo.sh"]
