import asyncio
# GraphRAG CLI imports
import graphrag.cli.query as query_cli_module
import json
import logging
import multiprocessing
import os
import pandas as pd
from datetime import datetime
from fastapi import FastAPI, HTTPException, BackgroundTasks, UploadFile, File
from fastapi.responses import PlainTextResponse
from graphrag.config.enums import IndexingMethod
from pathlib import Path
from typing import Any

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


def indexing_worker(root_dir, run_id, logs_dir):
    """Worker process for indexing to avoid signal handler issues in threads."""
    try:
        # Re-import inside worker to ensure clean state
        import graphrag.cli.index as index_cli_module
        from graphrag.config.enums import IndexingMethod
        from pathlib import Path

        index_cli_module.index_cli(
            root_dir=Path(root_dir),
            method=IndexingMethod.Standard,
            verbose=True,
            cache=True,
            dry_run=False,
            skip_validation=False
        )

        status_file = os.path.join(logs_dir, f"{run_id}.json")
        try:
            with open(status_file, "r") as f:
                data = json.load(f)
        except:
            data = {}

        data.update({
            "status": "completed",
            "end_time": datetime.now().isoformat(),
            "message": "Indexing completed successfully."
        })

        with open(status_file, "w") as f:
            json.dump(data, f)

    except Exception as e:
        status_file = os.path.join(logs_dir, f"{run_id}.json")
        try:
            with open(status_file, "r") as f:
                data = json.load(f)
        except:
            data = {}

        data.update({
            "status": "failed",
            "end_time": datetime.now().isoformat(),
            "message": str(e)
        })
        with open(status_file, "w") as f:
            json.dump(data, f)


async def run_indexing_task(run_id: str):
    logger.info(f"Starting indexing process (RunID: {run_id})")
    process = multiprocessing.Process(
        target=indexing_worker,
        args=(GRAPHRAG_ROOT, run_id, LOGS_DIR)
    )
    process.start()


@app.get("/global")
async def global_query(query: str):
    return await run_query_task(query, "global")


@app.get("/local")
async def local_query(query: str):
    return await run_query_task(query, "local")


@app.get("/drift")
async def drift_query(query: str):
    result = await run_query_task(query, "drift")
    return PlainTextResponse(content=result)


async def run_query_task(query: str, method: str):
    logger.info(f"Running {method} query: {query}")

    def run_search():
        if method == "global":
            response, _ = query_cli_module.run_global_search(
                data_dir=None,
                root_dir=Path(GRAPHRAG_ROOT),
                community_level=2,
                dynamic_community_selection=False,
                response_type="plain text without any data references or citations",
                streaming=False,
                query=query,
                verbose=True
            )
        elif method == "local":
            response, _ = query_cli_module.run_local_search(
                data_dir=None,
                root_dir=Path(GRAPHRAG_ROOT),
                community_level=2,
                response_type="plain text without any data references or citations",
                streaming=False,
                query=query,
                verbose=True
            )
        elif method == "drift":
            response, _ = query_cli_module.run_drift_search(
                data_dir=None,
                root_dir=Path(GRAPHRAG_ROOT),
                community_level=2,
                response_type="plain text without any data references or citations",
                streaming=False,
                query=query,
                verbose=True
            )
        else:
            raise ValueError(f"Invalid method: {method}")
        return response

    try:
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(None, run_search)
        return response
    except Exception as e:
        logger.error(f"Query failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
