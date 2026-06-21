import asyncio
import logging
import os
from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.responses import PlainTextResponse
import graphrag.cli.query as query_cli_module
from pathlib import Path

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("graphrag-sidecar")

app = FastAPI(title="Nexa GraphRAG Sidecar")

# Configuration
GRAPHRAG_ROOT = os.getenv("GRAPHRAG_ROOT", "/app")
INPUT_DIR = os.path.join(GRAPHRAG_ROOT, "input")

# Ensure input directory exists for uploads
os.makedirs(INPUT_DIR, exist_ok=True)

@app.get("/health")
def health():
    return {"status": "healthy"}

@app.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    try:
        file_path = os.path.join(INPUT_DIR, file.filename)
        with open(file_path, "wb") as buffer:
            while True:
                chunk = await file.read(1024 * 1024)  # 1MB chunks
                if not chunk:
                    break
                buffer.write(chunk)
        logger.info(f"File saved: {file_path}")
        return {"filename": file.filename, "status": "saved"}
    except Exception as e:
        logger.error(f"Upload failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/files/{filename}")
def delete_file(filename: str):
    file_path = os.path.join(INPUT_DIR, filename)
    if os.path.exists(file_path):
        os.remove(file_path)
    return {"filename": filename, "status": "deleted"}

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
                response_type="Multiple Paragraphs",
                streaming=False,
                query=query,
                verbose=True
            )
        elif method == "local":
            response, _ = query_cli_module.run_local_search(
                data_dir=None,
                root_dir=Path(GRAPHRAG_ROOT),
                community_level=2,
                response_type="Multiple Paragraphs",
                streaming=False,
                query=query,
                verbose=True
            )
        elif method == "drift":
            response, _ = query_cli_module.run_drift_search(
                data_dir=None,
                root_dir=Path(GRAPHRAG_ROOT),
                community_level=2,
                response_type="Multiple Paragraphs",
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
