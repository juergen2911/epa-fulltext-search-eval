# Test Requests for EPA Fulltext Search Service

This directory contains example requests for testing the API endpoints.

## Prerequisites

- The service should be running on `http://localhost:8080`
- MinIO should be running on `http://localhost:9000`

## Test Documents

Create test documents in this directory:

### 1. Sample Text File (test.txt)
```
This is a sample text document for testing the fulltext search service.
It contains some sample content that can be indexed and searched.
```

### 2. Sample JSON File (test.json)
```json
{
  "title": "Test Document",
  "content": "This is a JSON document with searchable content",
  "author": "Test Author"
}
```

### 3. Sample XML File (test.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<document>
  <title>Test XML Document</title>
  <content>This is an XML document with searchable content</content>
</document>
```

## API Endpoints

### 1. Upload Document

Upload a text file:
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test.txt" \
  -F "fileName=test.txt" \
  -F "mimeType=text/plain"
```

Upload a JSON file:
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test.json" \
  -F "fileName=test.json" \
  -F "mimeType=application/json"
```

Upload an XML file:
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test.xml" \
  -F "fileName=test.xml" \
  -F "mimeType=application/xml"
```

Upload a PDF file:
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@sample.pdf" \
  -F "fileName=sample.pdf" \
  -F "mimeType=application/pdf"
```

### 2. Search Documents

Simple search:
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "sample",
    "maxResults": 10
  }'
```

Search with specific term:
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "content",
    "maxResults": 5
  }'
```

### 3. Health Check

```bash
curl http://localhost:8080/api/health
```

## Supported MIME Types

- `application/pdf` - PDF documents (with OCR for image-based PDFs)
- `text/plain` - Plain text files
- `application/xml` - XML documents
- `application/json` - JSON documents
- `application/fhir+xml` - FHIR XML resources
- `application/fhir+json` - FHIR JSON resources
- `application/hl7-v3` - HL7 v3 messages

## Response Examples

### Successful Upload Response:
```json
{
  "documentId": "123e4567-e89b-12d3-a456-426614174000",
  "fileName": "test.txt",
  "mimeType": "text/plain",
  "size": 1024,
  "indexed": true,
  "message": "Document successfully processed and indexed"
}
```

### Search Response:
```json
{
  "totalHits": 2,
  "results": [
    {
      "documentId": "123e4567-e89b-12d3-a456-426614174000",
      "fileName": "test.txt",
      "score": 0.85,
      "snippet": "This is a sample text document for testing..."
    },
    {
      "documentId": "234e5678-e89b-12d3-a456-426614174001",
      "fileName": "test.json",
      "score": 0.72,
      "snippet": "This is a JSON document with searchable content..."
    }
  ]
}
```

## Error Responses

### Unsupported Document Type (400):
```json
{
  "error": "Unsupported document type: application/unsupported"
}
```

### Concurrency Limit Reached (429):
```json
{
  "error": "Maximum number of parallel requests reached. Please try again later."
}
```

### Processing Error (500):
```json
{
  "error": "Failed to process document: <error details>"
}
```
