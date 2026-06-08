import json
import logging
import os
import subprocess
import uuid
from datetime import datetime

from fastapi import FastAPI, HTTPException, BackgroundTasks, UploadFile, File
from fastapi.responses import PlainTextResponse

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("graphrag-sidecar")

app = FastAPI(title="Nexa GraphRAG Sidecar")

# Configuration
GRAPHRAG_ROOT = os.getenv("GRAPHRAG_ROOT", "/app")
INPUT_DIR = os.path.join(GRAPHRAG_ROOT, "input")
LOGS_DIR = os.path.join(GRAPHRAG_ROOT, "logs", "indexing_status")

# Ensure directories exist
os.makedirs(INPUT_DIR, exist_ok=True)
os.makedirs(LOGS_DIR, exist_ok=True)


def update_status(run_id: str, updates: dict):
    """Helper to update or initialize the status file for a given run_id."""
    status_file = os.path.join(LOGS_DIR, f"{run_id}.json")
    data = {}
    if os.path.exists(status_file):
        try:
            with open(status_file, "r") as f:
                data = json.load(f)
        except json.JSONDecodeError:
            logger.warning(f"Failed to decode status file for {run_id}, starting fresh.")

    data.update(updates)
    with open(status_file, "w") as f:
        json.dump(data, f)
    return data


@app.get("/health")
def health():
    return {"status": "healthy"}


@app.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    try:
        file_path = os.path.join(INPUT_DIR, file.filename)
        with open(file_path, "wb") as buffer:
            content = await file.read()
            buffer.write(content)
        logger.info(f"File saved: {file_path}")
        return {"filename": file.filename, "status": "saved"}
    except Exception as e:
        logger.error(f"Upload failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/index")
async def trigger_indexing(requestId: str, background_tasks: BackgroundTasks):
    # Initialize status file
    update_status(requestId, {
        "status": "running",
        "run_id": requestId,
        "start_time": datetime.now().isoformat(),
        "message": "Starting indexing pipeline..."
    })

    background_tasks.add_task(run_indexing_task, requestId)
    return requestId


@app.get("/index/{id}/progress")
async def get_index_progress(id: str):
    status_file = os.path.join(LOGS_DIR, f"{id}.json")

    if not os.path.exists(status_file):
        raise HTTPException(status_code=404, detail="No indexing task found for this ID")

    with open(status_file, "r") as f:
        status_data = json.load(f)

    # Ensure all values in the map are strings to match Java Map<String, String>
    return {k: str(v) for k, v in status_data.items()}


def run_indexing_task(run_id: str):
    try:
        logger.info(f"Running indexing (RunID: {run_id})")
        result = subprocess.run(
            ["graphrag", "index", "--root", GRAPHRAG_ROOT],
            capture_output=True,
            text=True
        )

        status = "completed" if result.returncode == 0 else "failed"
        output = result.stdout if result.returncode == 0 else result.stderr

        update_status(run_id, {
            "status": status,
            "end_time": datetime.now().isoformat(),
            "message": output[-1000:],
            "return_code": str(result.returncode)
        })

        logger.info(f"Indexing {status} for {run_id}")
        logger.info(f"Indexing output: {output}")
    except Exception as e:
        logger.error(f"Background indexing failed: {str(e)}")
        update_status(run_id, {
            "status": "error",
            "end_time": datetime.now().isoformat(),
            "message": str(e)
        })


@app.get("/global")
async def global_query(query: str):
    return await run_query(query, "global")


@app.get("/local")
async def local_query(query: str):
    return await run_query(query, "local")


@app.get("/drift")
async def drift_query(query: str):
    result = await run_query(query, "drift")
    return PlainTextResponse(content=result)


async def run_query(query: str, method: str):
    logger.info(f"Running {method} query: {query}")
    result = subprocess.run(
        [
            "graphrag", "query",
            "--root", GRAPHRAG_ROOT,
            "--method", method,
            query
        ],
        capture_output=True,
        text=True
    )
    if result.returncode != 0:
        logger.error(f"Query failed: {result.stderr}")
        raise HTTPException(status_code=500, detail=result.stderr)

    return result.stdout.strip()


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
