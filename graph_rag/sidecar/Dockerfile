FROM python:3.12-slim AS builder

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

FROM python:3.12-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy installed packages from builder
COPY --from=builder /install /usr/local

# Copy application code, prompts, and settings
COPY sidecar/sidecar.py .
COPY settings.yaml .
COPY prompts/ ./prompts/

# Create necessary directories
RUN mkdir -p input output config cache

# Set environment variables
ENV GRAPHRAG_ROOT=/app
ENV PYTHONUNBUFFERED=1

# Expose the port
EXPOSE 8000

# Start the application
CMD ["uvicorn", "sidecar:app", "--host", "0.0.0.0", "--port", "8000"]
