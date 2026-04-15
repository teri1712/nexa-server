#!/usr/bin/env bash

curl -X PUT http://elasticsearch:9200/nexa-documents \
-H "Content-Type: application/json" \
-d @schema/embedding_schema.json || return


curl -X PUT http://elasticsearch:9200/nexa-documents \
-H "Content-Type: application/json" \
-d @schema/doc_schema.json || return