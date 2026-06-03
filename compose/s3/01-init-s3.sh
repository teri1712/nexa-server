#!/bin/bash
set -e
awslocal s3 mb s3://decade-bucket

awslocal s3api put-bucket-cors --bucket decade-bucket --cors-configuration '{
  "CORSRules": [
    {
      "AllowedOrigins": ["http://localhost:4200"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["ETag"]
    }
  ]
}'
