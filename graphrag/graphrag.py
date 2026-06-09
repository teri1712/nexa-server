import json
import logging
import os
import asyncio
from datetime import datetime
from typing import Any
from pathlib import Path

import pandas as pd
from fastapi import FastAPI, HTTPException, BackgroundTasks, UploadFile, File
from fastapi.responses import PlainTextResponse

# GraphRAG API imports
import graphrag.api as api
from graphrag.config.load_config import load_config
from graphrag.config.enums import IndexingMethod
from graphrag.storage.factory import StorageFactory
from graphrag.storage.tables.table_provider_factory import TableProviderFactory
from graphrag.data_model.data_reader import DataReader

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


async def run_indexing_task(run_id: str):
    try:
        logger.info(f"Running indexing (RunID: {run_id})")
        config = load_config(root_dir=GRAPHRAG_ROOT)

        # Call the indexing API
        outputs = await api.build_index(
            config=config,
            method=IndexingMethod.Standard,
            verbose=True
        )

        # Check for errors in workflows (PipelineRunResult has 'errors' as list[BaseException])
        all_errors = []
        for output in outputs:
            if output.errors:
                all_errors.extend(output.errors)

        if all_errors:
            status = "failed"
            message = f"Indexing failed with {len(all_errors)} errors. Last error: {str(all_errors[-1])}"
        else:
            status = "completed"
            message = "Indexing completed successfully."

        update_status(run_id, {
            "status": status,
            "end_time": datetime.now().isoformat(),
            "message": message
        })

        logger.info(f"Indexing {status} for {run_id}")
    except Exception as e:
        logger.error(f"Background indexing failed: {str(e)}")
        update_status(run_id, {
            "status": "error",
            "end_time": datetime.now().isoformat(),
            "message": str(e)
        })


async def _resolve_output_files(
    config: Any,
    output_list: list[str],
    optional_list: list[str] | None = None,
) -> dict[str, Any]:
    """Read indexing output files to a dataframe dict, with correct column types."""
    dataframe_dict = {}
    
    # StorageFactory and TableProviderFactory are used in 2.7.2
    storage_obj = StorageFactory.create_storage(config.output_storage.type, config.output_storage.model_dump())
    table_provider = TableProviderFactory.create_table_provider(config.table_provider.type, {"storage": storage_obj})
    
    reader = DataReader(table_provider)
    for name in output_list:
        df_value = await getattr(reader, name)()
        dataframe_dict[name] = df_value

    if optional_list:
        for optional_file in optional_list:
            # Check if optional file exists
            try:
                has_method = getattr(table_provider, "has", None)
                if has_method and await has_method(optional_file):
                    df_value = await getattr(reader, optional_file)()
                    dataframe_dict[optional_file] = df_value
                else:
                    dataframe_dict[optional_file] = None
            except Exception:
                dataframe_dict[optional_file] = None
                
    return dataframe_dict


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
    config = load_config(root_dir=GRAPHRAG_ROOT)

    if method == "global":
        dataframe_dict = await _resolve_output_files(
            config=config,
            output_list=["entities", "communities", "community_reports"]
        )
        response, _ = await api.global_search(
            config=config,
            entities=dataframe_dict["entities"],
            communities=dataframe_dict["communities"],
            community_reports=dataframe_dict["community_reports"],
            community_level=2,
            dynamic_community_selection=False,
            response_type="Multiple Paragraphs",
            query=query
        )
    elif method == "local":
        dataframe_dict = await _resolve_output_files(
            config=config,
            output_list=["communities", "community_reports", "text_units", "relationships", "entities"],
            optional_list=["covariates"]
        )
        response, _ = await api.local_search(
            config=config,
            entities=dataframe_dict["entities"],
            communities=dataframe_dict["communities"],
            community_reports=dataframe_dict["community_reports"],
            text_units=dataframe_dict["text_units"],
            relationships=dataframe_dict["relationships"],
            covariates=dataframe_dict.get("covariates"),
            community_level=2,
            response_type="Multiple Paragraphs",
            query=query
        )
    elif method == "drift":
        dataframe_dict = await _resolve_output_files(
            config=config,
            output_list=["communities", "community_reports", "text_units", "relationships", "entities"]
        )
        response, _ = await api.drift_search(
            config=config,
            entities=dataframe_dict["entities"],
            communities=dataframe_dict["communities"],
            community_reports=dataframe_dict["community_reports"],
            text_units=dataframe_dict["text_units"],
            relationships=dataframe_dict["relationships"],
            community_level=2,
            response_type="Multiple Paragraphs",
            query=query
        )
    else:
        raise HTTPException(status_code=400, detail=f"Invalid method: {method}")

    return response


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
