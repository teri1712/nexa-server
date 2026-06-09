import json
import logging
import os
import uuid
from datetime import datetime, timedelta
from pathlib import Path
from typing import Optional

import numpy as np
import pandas as pd
from dotenv import load_dotenv
from fastapi import FastAPI, BackgroundTasks, Query
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
from sqlalchemy import create_engine, text

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

app = FastAPI(title="Nexa FAQ Clustering Service")

# Database connection details
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")
NUM_CLUSTERS = int(os.getenv("NUM_CLUSTERS"))

DB_URL = f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

# Status persistence configuration
STATUS_DIR = Path(__file__).parent / "status_records"
STATUS_DIR.mkdir(parents=True, exist_ok=True)

# Generate a unique instance ID for this process
INSTANCE_ID = str(uuid.uuid4())[:8]
logger.info(f"Service instance started with ID: {INSTANCE_ID}")

def get_status_path(date_str: str) -> Path:
    return STATUS_DIR / f"{date_str}.json"

def save_status(date_str: str, status_data: dict):
    # Ensure instance_id is included in the saved data
    if "process_id" not in status_data:
        status_data["process_id"] = INSTANCE_ID
    with open(get_status_path(date_str), "w") as f:
        json.dump(status_data, f)

def load_status(date_str: str) -> dict:
    path = get_status_path(date_str)
    if path.exists():
        try:
            with open(path, "r") as f:
                data = json.load(f)
                
                # Check for hash mismatch: if RUNNING but process_id differs, it's orphaned
                if data.get("status") == "RUNNING" and data.get("process_id") != INSTANCE_ID:
                    logger.warning(f"Orphaned job detected for {date_str} (ID mismatch). Marking as IN_COMPLETED.")
                    data["status"] = "IN_COMPLETED"
                    data["error_message"] = "Process interrupted or crashed"
                    save_status(date_str, data)
                
                return data
        except Exception as e:
            logger.error(f"Error loading status for {date_str}: {e}")
    return {"process_date": date_str, "status": "IDLE", "error_message": None, "process_id": None}

def is_job_running_for_date(date_str: str) -> bool:
    """Checks if a job for the specific date is currently in RUNNING state for THIS instance."""
    status = load_status(date_str)
    return status.get("status") == "RUNNING" and status.get("process_id") == INSTANCE_ID

# Global model instance to avoid reloading
logger.info("Loading embedding model (all-MiniLM-L6-v2)...")
model = SentenceTransformer('all-MiniLM-L6-v2')


def perform_clustering(date_str: str):
    current_status = {
        "process_date": date_str,
        "status": "RUNNING",
        "error_message": None,
        "process_id": INSTANCE_ID
    }
    save_status(date_str, current_status)

    logger.info(f"Starting clustering for date: {date_str}")

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
            current_status["status"] = "COMPLETED"
            save_status(date_str, current_status)
            return

        raw_user_queries = df_queries['query'].tolist()
        embeddings = model.encode(raw_user_queries)

        num_samples = len(raw_user_queries)

        logger.info(f"Clustering {num_samples} queries into {NUM_CLUSTERS} groups...")
        kmeans = KMeans(n_clusters=NUM_CLUSTERS, random_state=42, n_init='auto')
        kmeans.fit(embeddings)

        df_queries['Cluster'] = kmeans.labels_

        with engine.begin() as conn:
            for cluster in range(NUM_CLUSTERS):
                cluster_data = df_queries[df_queries['Cluster'] == cluster]
                if cluster_data.empty:
                    continue

                queries_list = cluster_data['query'].tolist()
                cluster_center = kmeans.cluster_centers_[cluster]
                cluster_embeddings = embeddings[df_queries['Cluster'] == cluster]

                distances = np.linalg.norm(cluster_embeddings - cluster_center, axis=1)
                closest_index = np.argmin(distances)
                representative_question = queries_list[closest_index]

                insert_sql = text("""
                                  INSERT INTO faq (queries, question, created_at)
                                  VALUES (:queries, :question, :created_at)
                                  """)

                conn.execute(insert_sql, {
                    "queries": queries_list,
                    "question": representative_question,
                    "created_at": target_date
                })

        logger.info(f"Successfully completed clustering for {date_str}")
        current_status["status"] = "COMPLETED"
        save_status(date_str, current_status)

    except Exception as e:
        logger.error(f"Error during clustering process: {e}")
        current_status["status"] = "ERROR"
        current_status["error_message"] = str(e)
        save_status(date_str, current_status)


@app.post("/cluster")
async def trigger_clustering(background_tasks: BackgroundTasks, date: Optional[str] = Query(None)):
    """Triggers the clustering process asynchronously."""
    if not date:
        date = datetime.now().strftime("%Y-%m-%d")

    if is_job_running_for_date(date):
        return {"status": "already_running", "date": date}

    background_tasks.add_task(perform_clustering, date)
    return {"status": "started", "date": date}


@app.get("/status/{date}")
def get_status(date: str):
    """Returns the status of the clustering job for a specific date."""
    return load_status(date)


@app.get("/health")
def health_check():
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
