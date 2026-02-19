package de.epa.fulltext.service

import de.epa.fulltext.config.ConcurrencyConfig
import de.epa.fulltext.config.EncryptionConfig
import de.epa.fulltext.exception.ConcurrencyLimitException
import de.epa.fulltext.exception.UnsupportedDocumentTypeException
import de.epa.fulltext.model.DocumentUploadResponse
import de.epa.fulltext.model.SupportedMimeType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.util.UUID

@ApplicationScoped
class DocumentProcessingService {

    private val logger = Logger.getLogger(DocumentProcessingService::class.java)

    @Inject
    lateinit var concurrencyConfig: ConcurrencyConfig

    @Inject
    lateinit var encryptionConfig: EncryptionConfig

    @Inject
    lateinit var textExtractionService: TextExtractionService

    @Inject
    lateinit var indexingService: IndexingService

    @Inject
    lateinit var encryptionService: EncryptionService

    @Inject
    lateinit var storageService: StorageService

    fun processDocument(
        fileName: String,
        mimeType: String,
        fileData: ByteArray
    ): DocumentUploadResponse {
        // Check if mime type is supported
        if (!SupportedMimeType.isSupported(mimeType)) {
            throw UnsupportedDocumentTypeException(mimeType)
        }

        val semaphore = concurrencyConfig.getSemaphore()
        
        // Try to acquire permit with timeout
        if (!semaphore.tryAcquire()) {
            throw ConcurrencyLimitException()
        }

        try {
            val documentId = UUID.randomUUID().toString()
            logger.info("Processing document: $fileName (ID: $documentId, Type: $mimeType)")

            // Extract text from document
            val extractedText = textExtractionService.extractText(fileData, mimeType)
            logger.debug("Extracted ${extractedText.length} characters from document")

            // Create Lucene index
            val indexData = indexingService.createIndex(documentId, fileName, extractedText)

            // Generate encryption key
            val encryptionKey = encryptionConfig.generateKey()

            // Encrypt index
            val encryptedIndex = encryptionService.encrypt(indexData, encryptionKey)

            // Store encrypted index and key in MinIO
            storageService.uploadIndex(documentId, encryptedIndex)
            storageService.uploadEncryptionKey(documentId, encryptionService.keyToBytes(encryptionKey))

            logger.info("Successfully processed and indexed document: $documentId")

            return DocumentUploadResponse(
                documentId = documentId,
                fileName = fileName,
                mimeType = mimeType,
                size = fileData.size.toLong(),
                indexed = true,
                message = "Document successfully processed and indexed"
            )
        } finally {
            semaphore.release()
        }
    }
}
