# Quick Start Guide

Get the EPA Fulltext Search Service up and running in minutes!

## 🚀 Quick Start (5 minutes)

### 1. Start MinIO (Required)
```bash
docker-compose up -d
```
Verify MinIO is running at http://localhost:9001 (admin/admin: minioadmin/minioadmin)

### 2. Start the Service
```bash
# Development mode (with hot reload)
./mvnw quarkus:dev

# Or build and run in production mode
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

The service will be available at http://localhost:8080

### 3. Upload a Document
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test-requests/test.txt" \
  -F "fileName=test.txt" \
  -F "mimeType=text/plain"
```

Expected response:
```json
{
  "documentId": "123e4567-e89b-12d3-a456-426614174000",
  "fileName": "test.txt",
  "mimeType": "text/plain",
  "size": 342,
  "indexed": true,
  "message": "Document successfully processed and indexed"
}
```

### 4. Search Documents
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "sample", "maxResults": 10}'
```

Expected response:
```json
{
  "totalHits": 1,
  "results": [
    {
      "documentId": "123e4567-e89b-12d3-a456-426614174000",
      "fileName": "test.txt",
      "score": 0.85,
      "snippet": "This is a sample text document for testing..."
    }
  ]
}
```

## 📋 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/documents/upload` | POST | Upload and index a document |
| `/api/search` | POST | Search indexed documents |
| `/api/health` | GET | Health check |

## 📄 Supported Document Types

- ✅ `application/pdf` - PDF documents (with OCR)
- ✅ `text/plain` - Plain text
- ✅ `application/xml` - XML documents
- ✅ `application/json` - JSON documents
- ✅ `application/fhir+xml` - FHIR XML
- ✅ `application/fhir+json` - FHIR JSON
- ✅ `application/hl7-v3` - HL7 v3

## 🧪 Run Tests

### Automated Tests
```bash
./mvnw test
```

### Manual Test Script
```bash
cd test-requests
./test-api.sh
```

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# MinIO Connection
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin

# Concurrency
concurrency.max-parallel-requests=80

# Encryption
encryption.key-size=256
```

## 🐛 Troubleshooting

### MinIO not accessible
```bash
# Check if MinIO is running
docker ps | grep minio

# Restart MinIO
docker-compose restart
```

### Build fails
```bash
# Clean and rebuild
./mvnw clean install
```

### OCR not working
Tesseract is optional. For full OCR support, install Tesseract:
```bash
# Ubuntu/Debian
sudo apt-get install tesseract-ocr

# macOS
brew install tesseract

# Or use the service without OCR (text-based PDFs still work)
```

## 📚 Additional Resources

- **Detailed Documentation**: See [README.md](README.md)
- **Implementation Details**: See [IMPLEMENTATION.md](IMPLEMENTATION.md)
- **Test Examples**: See [test-requests/README.md](test-requests/README.md)
- **Quarkus Guide**: See [README.quarkus.md](README.quarkus.md)

## 💡 Example Workflow

1. **Start MinIO**: `docker-compose up -d`
2. **Start Service**: `./mvnw quarkus:dev`
3. **Upload Documents**:
   ```bash
   for file in test-requests/*.{txt,json,xml}; do
     curl -X POST http://localhost:8080/api/documents/upload \
       -F "file=@$file" \
       -F "fileName=$(basename $file)" \
       -F "mimeType=text/plain"
   done
   ```
4. **Search**: 
   ```bash
   curl -X POST http://localhost:8080/api/search \
     -H "Content-Type: application/json" \
     -d '{"query": "patient OR medical", "maxResults": 10}'
   ```

## 🎯 Key Features

- ✅ 80 parallel requests supported
- ✅ AES-256-GCM encryption at rest
- ✅ Automatic OCR for image-based PDFs
- ✅ Full-text search with ranking
- ✅ Request isolation (no data mixing)
- ✅ Production-ready error handling

## ⚡ Performance Tips

- Use SSD storage for MinIO data
- Increase Java heap for large documents: `-Xmx2g`
- For production, use external MinIO cluster
- Consider index caching for frequent searches

## 📞 Need Help?

Check the detailed documentation in:
- README.md (overview)
- IMPLEMENTATION.md (technical details)
- test-requests/README.md (API examples)

Happy searching! 🎉
