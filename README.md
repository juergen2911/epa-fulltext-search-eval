# EPA Fulltext Search Evaluation Service

A Kotlin-based Quarkus service for document indexing and full-text search using Apache Lucene, with encrypted storage in MinIO/S3.

## Features

- **Document Upload & Indexing**: Upload documents in various formats and automatically index them
- **Multiple Format Support**: PDF, TXT, XML, JSON, FHIR+XML, FHIR+JSON, HL7-v3
- **OCR for PDFs**: Automatic detection of image-based PDFs with Tesseract OCR integration
- **Encrypted Storage**: Index files are encrypted (AES-256-GCM) before storage
- **S3-Compatible Storage**: Uses MinIO for S3-compatible object storage
- **Full-Text Search**: Search across all indexed documents using Apache Lucene
- **Concurrency Control**: Limits to 80 parallel requests with proper isolation
- **RESTful API**: Clean REST API for upload and search operations

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (for MinIO)
- Tesseract OCR (optional, for PDF OCR features)

## Quick Start

### 1. Start MinIO

```bash
docker-compose up -d
```

MinIO will be available at:
- API: http://localhost:9000
- Console: http://localhost:9001 (credentials: minioadmin/minioadmin)

### 2. Build the Application

```bash
./mvnw clean package
```

### 3. Run the Application

Development mode:
```bash
./mvnw quarkus:dev
```

Production mode:
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

The service will be available at http://localhost:8080

## API Endpoints

### Upload Document

```bash
POST /api/documents/upload
Content-Type: multipart/form-data

Parameters:
- file: The document file (binary)
- fileName: Name of the file
- mimeType: MIME type of the document
```

### Search Documents

```bash
POST /api/search
Content-Type: application/json

{
  "query": "search term",
  "maxResults": 10
}
```

### Health Check

```bash
GET /api/health
```

## Supported Document Types

- `application/pdf` - PDF documents (with automatic OCR for image-based PDFs)
- `text/plain` - Plain text files
- `application/xml` - XML documents
- `application/json` - JSON documents
- `application/fhir+xml` - FHIR XML resources
- `application/fhir+json` - FHIR JSON resources
- `application/hl7-v3` - HL7 v3 messages

## Configuration

Configuration is in `src/main/resources/application.properties`:

```properties
# MinIO Configuration
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=document-indexes

# Concurrency Configuration
concurrency.max-parallel-requests=80

# Encryption Configuration
encryption.algorithm=AES/GCM/NoPadding
encryption.key-size=256
```

## Testing

Example test requests are provided in the `test-requests/` directory.

### Upload a test document:

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test-requests/test.txt" \
  -F "fileName=test.txt" \
  -F "mimeType=text/plain"
```

### Search for documents:

```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "sample", "maxResults": 10}'
```

## Architecture

### Components

1. **Text Extraction Service**: Extracts text from various document formats
   - Uses Apache PDFBox for PDF processing
   - Uses Apache Tika for XML/structured documents
   - Integrates Tesseract for OCR on image-based PDFs

2. **Indexing Service**: Creates Lucene indexes from extracted text
   - Uses Apache Lucene for indexing
   - Serializes indexes to byte arrays for storage

3. **Encryption Service**: Encrypts/decrypts index data
   - Uses AES-256-GCM encryption
   - Generates unique keys for each document

4. **Storage Service**: Manages encrypted indexes in MinIO/S3
   - Stores encrypted indexes and encryption keys separately
   - Provides listing and retrieval operations

5. **Search Service**: Performs full-text search across documents
   - Downloads and decrypts all indexes
   - Merges indexes for unified search
   - Returns ranked results

6. **Document Processing Service**: Orchestrates the complete workflow
   - Enforces concurrency limits (80 parallel requests)
   - Ensures request isolation
   - Handles end-to-end document processing

### Data Flow

1. Client uploads document → Document Resource
2. Acquire semaphore permit (max 80 concurrent)
3. Extract text from document
4. Create Lucene index
5. Generate encryption key
6. Encrypt index
7. Store encrypted index and key in MinIO
8. Release semaphore permit
9. Return response to client

### Search Flow

1. Client sends search query → Search Resource
2. List all document IDs from MinIO
3. Download and decrypt all indexes
4. Merge indexes into single searchable index
5. Execute Lucene query
6. Return ranked results

## Security

- **Encryption at Rest**: All indexes are encrypted with AES-256-GCM before storage
- **Unique Keys**: Each document has its own encryption key
- **Secure Key Storage**: Encryption keys are stored separately from encrypted data
- **Request Isolation**: Each request is processed in isolation with proper concurrency control

## Performance

- **Concurrency Limit**: Maximum 80 parallel requests
- **In-Memory Processing**: Indexes are processed in-memory for performance
- **Efficient Serialization**: Custom serialization for Lucene indexes
- **Lazy Loading**: Indexes are loaded only when needed for search

## Development

### Running Tests

```bash
./mvnw test
```

### Development Mode

```bash
./mvnw quarkus:dev
```

This enables hot reload and dev UI at http://localhost:8080/q/dev/

For more Quarkus-specific information, see [README.quarkus.md](README.quarkus.md)

## License

This is a test/evaluation project for EPA fulltext search capabilities.
