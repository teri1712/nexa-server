#!/bin/sh
/usr/bin/mc alias set mys3 http://s3:9000 decadedecade decadedecade
/usr/bin/mc mb mys3/decade-bucket || true
/usr/bin/mc anonymous set public mys3/decade-bucket
echo "MinIO initialized with bucket 'decade-bucket'"
exit 0
