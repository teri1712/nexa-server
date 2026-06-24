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

# Copy installed packages from builder
COPY --from=builder /install /usr/local

# Copy application code, prompts, and settings
COPY index/index.py .
COPY settings.yaml .
COPY prompts/ ./prompts/

# Create necessary directories
RUN mkdir -p input output config cache

# Set environment variables
ENV GRAPHRAG_ROOT=/app
ENV PYTHONUNBUFFERED=1

# Start the batch indexing job
CMD ["python", "index.py"]
