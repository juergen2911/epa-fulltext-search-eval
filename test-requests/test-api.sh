#!/bin/bash

# EPA Fulltext Search Service - Test Script
# This script demonstrates how to use the API endpoints

set -e

BASE_URL="http://localhost:8080"

echo "==================================="
echo "EPA Fulltext Search Service - Test"
echo "==================================="
echo

# Check if service is running
echo "1. Checking service health..."
curl -s "${BASE_URL}/api/health" | json_pp || echo "Service might not be running!"
echo
echo

# Upload test documents
echo "2. Uploading test documents..."

echo "   Uploading test.txt..."
curl -X POST "${BASE_URL}/api/documents/upload" \
  -F "file=@test-requests/test.txt" \
  -F "fileName=test.txt" \
  -F "mimeType=text/plain" \
  | json_pp
echo
echo

echo "   Uploading test-fhir.json..."
curl -X POST "${BASE_URL}/api/documents/upload" \
  -F "file=@test-requests/test-fhir.json" \
  -F "fileName=test-fhir.json" \
  -F "mimeType=application/fhir+json" \
  | json_pp
echo
echo

echo "   Uploading test.xml..."
curl -X POST "${BASE_URL}/api/documents/upload" \
  -F "file=@test-requests/test.xml" \
  -F "fileName=test.xml" \
  -F "mimeType=application/xml" \
  | json_pp
echo
echo

# Search for documents
echo "3. Searching for documents..."

echo "   Searching for 'sample'..."
curl -X POST "${BASE_URL}/api/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "sample", "maxResults": 10}' \
  | json_pp
echo
echo

echo "   Searching for 'patient'..."
curl -X POST "${BASE_URL}/api/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "patient", "maxResults": 10}' \
  | json_pp
echo
echo

echo "   Searching for 'medical'..."
curl -X POST "${BASE_URL}/api/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "medical", "maxResults": 10}' \
  | json_pp
echo
echo

echo "==================================="
echo "Test completed!"
echo "==================================="
