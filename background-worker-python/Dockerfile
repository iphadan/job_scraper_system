FROM python:3.11-slim

WORKDIR /app

# Install dependencies required for the Postgres database engine driver
RUN apt-get update && apt-get install -y \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Run Python in unbuffered mode so logs appear immediately in Docker Compose
CMD ["python", "-u", "worker.py"]