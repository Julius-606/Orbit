# ==========================================
# IDENTITY: The Spaceship / VM Container
# FILEPATH: backend/Dockerfile
# COMPONENT: Infrastructure
# ROLE: Packages the whole Python backend into an unkillable container.
# VIBE: If this builds successfully on the first try, you should buy a lottery ticket. 🎟️
# ==========================================

# Using a slim version so it doesn't eat all your RAM
FROM python:3.11-slim

# Set environment variables so Python doesn't buffer text and crash quietly
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# Create the working directory
WORKDIR /app

# Install system dependencies (needed for compiling some Python packages)
RUN apt-get update \
    && apt-get install -y gcc libpq-dev \
    && apt-get clean

# Copy the dependency matrix and install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the actual brain into the container
COPY ./app /app

# Expose the FastAPI port
EXPOSE 8000

# Start the engine! Uvicorn handles the FastAPI requests
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]