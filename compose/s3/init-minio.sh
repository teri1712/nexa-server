#!/bin/sh
/usr/bin/mc alias set mys3 http://s3:9000 decadedecade decadedecade
/usr/bin/mc mb mys3/decade-bucket || true
echo "MinIO initialized with private bucket 'decade-bucket'"
exit 0
