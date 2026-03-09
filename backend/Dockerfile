################################################################################
# FILE: backend/Dockerfile
# VERSION: 1.0.1 | SYSTEM: Render Deployment Optimization
################################################################################
#
# Changes:
# - Added a non-root user for security (Render likes this).
# - Updated CMD to use the dynamic $PORT variable provided by Render.
# - If $PORT isn't set (e.g., local testing), it defaults to 8000.

FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Prevent Python from writing .pyc files and enable unbuffered output
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install system dependencies (needed for Postgres and compiling certain packages)
RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install them
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the entire backend directory into the container
COPY . .

# Create a non-root user to run the app (Security Best Practice)
RUN adduser --disabled-password --gecos '' orbituser
USER orbituser

# Expose port (Documentation purposes, Render ignores this and uses its own)
EXPOSE 8000

# Start command utilizing the dynamic port binding
# Using shell form so the environment variable is evaluated
CMD uvicorn app.main:app --host 0.0.0.0 --port ${PORT:-8000}