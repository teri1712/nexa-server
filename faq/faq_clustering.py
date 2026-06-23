import argparse
import logging
import os
import sys
from datetime import datetime, timedelta
from dotenv import load_dotenv
import numpy as np
import pandas as pd
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
from sqlalchemy import create_engine, text

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("faq-clustering")

# Load environment variables
load_dotenv()

# Database connection details
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

# Configuration
DB_URL = f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"


def perform_clustering(date_str: str, num_clusters: int, num_representatives: int):
    logger.info(f"Starting clustering for date: {date_str}")

    try:
        engine = create_engine(DB_URL)
        target_date = datetime.strptime(date_str, "%Y-%m-%d").date()

        # Load data from postgres table user_query of last 1 month
        start_date = target_date - timedelta(days=30)

        query_sql = """
                    SELECT id, query
                    FROM user_query
                    WHERE created_at >= :start_date
                      AND query IS NOT NULL
                      AND query != ''
                    """

        logger.info(f"Fetching queries from {start_date} onwards...")
        df_queries = pd.read_sql(text(query_sql), engine, params={"start_date": start_date})

        if df_queries.empty:
            logger.info(f"No queries found since {start_date} to cluster.")
            return

        # Load embedding model
        logger.info("Loading embedding model (all-MiniLM-L6-v2)...")
        model = SentenceTransformer('all-MiniLM-L6-v2')

        raw_user_queries = df_queries['query'].tolist()
        embeddings = model.encode(raw_user_queries)

        num_samples = len(raw_user_queries)

        logger.info(f"Clustering {num_samples} queries into {num_clusters} groups...")
        kmeans = KMeans(n_clusters=min(num_clusters, num_samples), random_state=42, n_init='auto')
        kmeans.fit(embeddings)

        distances = kmeans.transform(embeddings)
        actual_clusters = len(np.unique(kmeans.labels_))

        with engine.begin() as conn:
            for cluster in range(actual_clusters):
                # Get the positional indices of the queries belonging to this cluster
                cluster_positions = [i for i, label in enumerate(kmeans.labels_) if label == cluster]
                if not cluster_positions:
                    continue

                # Sort positional indices by their distance to this cluster's centroid
                sorted_positions = sorted(cluster_positions, key=lambda pos: distances[pos, cluster])

                # Slice to get the top N representatives
                representative_positions = sorted_positions[:num_representatives]
                queries_list = [raw_user_queries[pos] for pos in representative_positions]

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

    except Exception as e:
        logger.error(f"Error during clustering process: {e}")
        sys.exit(1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Nexa FAQ Clustering")
    parser.add_argument(
        "--date",
        type=str,
        default=datetime.now().strftime("%Y-%m-%d"),
        help="Target date for clustering (format: YYYY-MM-DD). Defaults to today."
    )
    parser.add_argument(
        "--num-clusters",
        type=int,
        default=5,
        help="Number of clusters to generate. Defaults to 5."
    )
    parser.add_argument(
        "--num-representatives",
        type=int,
        default=5,
        help="Number of representative queries to keep per cluster. Defaults to 5."
    )
    args = parser.parse_args()
    perform_clustering(args.date, args.num_clusters, args.num_representatives)
