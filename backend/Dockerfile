################################################################################
# FILE: backend/Dockerfile
# VERSION: 1.0.2 | SYSTEM: Hugging Face Spaces Optimization
################################################################################
#
# Changes:
# - Hugging Face Spaces expects the app to bind to port 7860 by default.
# - Switched the exposed port and CMD to 7860 for a zero-drawdown deployment.

FROM python:3.11-slim

WORKDIR /code

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install
COPY requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir -r /code/requirements.txt

# Set up a new user (Hugging Face strictly requires a non-root user with ID 1000)
RUN useradd -m -u 1000 orbituser
USER orbituser

# Set home to the user's home directory
ENV HOME=/home/orbituser \
    PATH=/home/orbituser/.local/bin:$PATH

WORKDIR $HOME/app

# Copy the rest of the code (chown ensures the new user owns the files)
COPY --chown=orbituser . $HOME/app

# Hugging Face default port
EXPOSE 7860

# Start command
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "7860"]
