import logging
import os
from datetime import datetime, timedelta
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

job_status = {
    "process_date": None,
    "status": "IDLE",  # IDLE, RUNNING, COMPLETED, ERROR
    "error_message": None
}

# Global model instance to avoid reloading
logger.info("Loading embedding model (all-MiniLM-L6-v2)...")
model = SentenceTransformer('all-MiniLM-L6-v2')


def perform_clustering(date_str: str):
    global job_status
    job_status["status"] = "RUNNING"
    job_status["process_date"] = date_str
    job_status["error_message"] = None

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
            job_status["status"] = "COMPLETED"
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
        job_status["status"] = "COMPLETED"

    except Exception as e:
        logger.error(f"Error during clustering process: {e}")
        job_status["status"] = "ERROR"
        job_status["error_message"] = str(e)


@app.post("/cluster")
async def trigger_clustering(background_tasks: BackgroundTasks, date: Optional[str] = Query(None)):
    """Triggers the clustering process asynchronously."""
    if not date:
        date = datetime.now().strftime("%Y-%m-%d")

    if job_status["status"] == "RUNNING":
        return {"status": "already_running", "date": job_status["process_date"]}

    background_tasks.add_task(perform_clustering, date)
    return {"status": "started", "date": date}


@app.get("/status")
def get_status():
    """Returns the status of the last clustering job."""
    return job_status


@app.get("/health")
def health_check():
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
