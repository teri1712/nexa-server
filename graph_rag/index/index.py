import logging
import os
from pathlib import Path
import graphrag.cli.index as index_cli_module
from graphrag.config.enums import IndexingMethod

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("graphrag-index-job")

GRAPHRAG_ROOT = os.getenv("GRAPHRAG_ROOT", "/app")

def main():
    logger.info("Starting indexing job...")
    try:
        index_cli_module.index_cli(
            root_dir=Path(GRAPHRAG_ROOT),
            method=IndexingMethod.Standard,
            verbose=True,
            cache=True,
            dry_run=False,
            skip_validation=False
        )
        logger.info("Indexing completed successfully.")
    except Exception as e:
        logger.error(f"Indexing job failed: {str(e)}")
        raise e

if __name__ == "__main__":
    main()
