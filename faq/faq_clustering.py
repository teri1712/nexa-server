import json
import logging
import numpy as np
import os
import pandas as pd
import uuid
from datetime import datetime, timedelta
from dotenv import load_dotenv
from fastapi import FastAPI, BackgroundTasks, Query, HTTPException
from pathlib import Path
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
from sqlalchemy import create_engine, text
from typing import Optional

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("faq-clustering-sidecar")

# Load environment variables
load_dotenv()

app = FastAPI(title="Nexa FAQ Clustering Sidecar")

# Database connection details
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

# Configuration
NUM_CLUSTERS = int(os.getenv("NUM_CLUSTERS", 5))
DB_URL = f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

# Status persistence configuration
STATUS_DIR = Path(__file__).parent / "logs" / "clustering_status"
STATUS_DIR.mkdir(parents=True, exist_ok=True)

# Generate a unique instance ID for this process
INSTANCE_ID = str(uuid.uuid4())[:8]
logger.info(f"Service instance started with ID: {INSTANCE_ID}")


def update_status(run_id: str, updates: dict):
    """Helper to update or initialize the status file for a given run_id."""
    status_file = STATUS_DIR / f"{run_id}.json"
    data = {}
    if status_file.exists():
        try:
            with open(status_file, "r") as f:
                data = json.load(f)
        except json.JSONDecodeError:
            logger.warning(f"Failed to decode status file for {run_id}, starting fresh.")

    # Check for hash mismatch: if RUNNING but process_id differs, it's orphaned
    if data.get("status") == "RUNNING" and data.get("process_id") != INSTANCE_ID:
        logger.warning(f"Orphaned job detected for {run_id} (ID mismatch). Marking as IN_COMPLETED.")
        data["status"] = "IN_COMPLETED"
        data["error_message"] = "Process interrupted or crashed"

    data.update(updates)
    if "process_id" not in data:
        data["process_id"] = INSTANCE_ID

    with open(status_file, "w") as f:
        json.dump(data, f)
    return data


# Global model instance to avoid reloading
logger.info("Loading embedding model (all-MiniLM-L6-v2)...")
model = SentenceTransformer('all-MiniLM-L6-v2')


def perform_clustering(run_id: str, date_str: str):
    logger.info(f"Starting clustering for date: {date_str} (RunID: {run_id})")

    try:
        engine = create_engine(DB_URL)
        target_date = datetime.strptime(date_str, "%Y-%m-%d").date()

        # 1. Load data from postgres table user_query of last 1 month
        start_date = target_date - timedelta(days=30)

        query_sql = """
                    SELECT id, query
                    FROM user_query
                    WHERE created_at >= :start_date
                      AND query IS NOT NULL
                      AND query != '' \
                    """

        logger.info(f"Fetching queries from {start_date} onwards...")
        df_queries = pd.read_sql(text(query_sql), engine, params={"start_date": start_date})

        if df_queries.empty:
            logger.info(f"No queries found since {start_date} to cluster.")
            update_status(run_id, {
                "status": "COMPLETED",
                "end_time": datetime.now().isoformat(),
                "message": "No queries found to cluster"
            })
            return

        raw_user_queries = df_queries['query'].tolist()
        embeddings = model.encode(raw_user_queries)

        num_samples = len(raw_user_queries)

        logger.info(f"Clustering {num_samples} queries into {NUM_CLUSTERS} groups...")
        kmeans = KMeans(n_clusters=min(NUM_CLUSTERS, num_samples), random_state=42, n_init='auto')
        kmeans.fit(embeddings)

        df_queries['Cluster'] = kmeans.labels_

        actual_clusters = len(np.unique(kmeans.labels_))

        with engine.begin() as conn:
            for cluster in range(actual_clusters):
                cluster_data = df_queries[df_queries['Cluster'] == cluster]
                if cluster_data.empty:
                    continue

                queries_list = cluster_data['query'].tolist()

                insert_sql = text("""
                                  INSERT INTO faq (queries, question, created_at)
                                  VALUES (:queries, :question, :created_at)
                                  """)

                conn.execute(insert_sql, {
                    "queries": queries_list,
                    "question": None,
                    "created_at": target_date
                })

        logger.info(f"Successfully completed clustering for {date_str}")
        update_status(run_id, {
            "status": "COMPLETED",
            "end_time": datetime.now().isoformat(),
            "message": f"Clustered {num_samples} queries into {actual_clusters} groups"
        })

    except Exception as e:
        logger.error(f"Error during clustering process: {e}")
        update_status(run_id, {
            "status": "ERROR",
            "end_time": datetime.now().isoformat(),
            "error_message": str(e)
        })


@app.post("/cluster")
async def trigger_clustering(requestId: str, background_tasks: BackgroundTasks, date: Optional[str] = Query(None)):
    """Triggers the clustering process asynchronously."""
    if not date:
        date = datetime.now().strftime("%Y-%m-%d")

    update_status(requestId, {
        "status": "RUNNING",
        "start_time": datetime.now().isoformat(),
        "message": f"Starting clustering for {date}..."
    })

    background_tasks.add_task(perform_clustering, requestId, date)
    return {"status": "started", "requestId": requestId}


@app.get("/cluster/{id}/progress")
def get_cluster_progress(id: str):
    """Returns the progress of the clustering job for a specific ID."""
    status_file = STATUS_DIR / f"{id}.json"
    if not status_file.exists():
        raise HTTPException(status_code=404, detail="No clustering task found for this ID")

    with open(status_file, "r") as f:
        data = json.load(f)

    # Mimic the GraphRAG behavior for ID mismatch/orphaned jobs
    if data.get("status") == "RUNNING" and data.get("process_id") != INSTANCE_ID:
        data["status"] = "IN_COMPLETED"
        data["error_message"] = "Process interrupted or crashed"
        with open(status_file, "w") as f:
            json.dump(data, f)

    return {k: str(v) for k, v in data.items()}


@app.get("/health")
def health_check():
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
