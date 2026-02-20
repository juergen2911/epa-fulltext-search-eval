package de.epa.fulltext.service

import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.ByteArrayInputStream

@ApplicationScoped
class StorageService {

    private val logger = Logger.getLogger(StorageService::class.java)

    @Inject
    lateinit var minioClient: MinioClient

    @ConfigProperty(name = "minio.bucket-name")
    lateinit var bucketName: String

    @ConfigProperty(name = "minio.auto-create-bucket", defaultValue = "true")
    var autoCreateBucket: Boolean = true

    @PostConstruct
    fun init() {
        if (autoCreateBucket) {
            ensureBucketExists()
        }
    }

    private fun ensureBucketExists() {
        try {
            val exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            )
            
            if (!exists) {
                logger.info("Creating bucket: $bucketName")
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                )
            }
        } catch (e: Exception) {
            logger.error("Error ensuring bucket exists", e)
        }
    }

    fun uploadIndex(documentId: String, indexData: ByteArray) {
        val objectName = "index-$documentId.enc"
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(ByteArrayInputStream(indexData), indexData.size.toLong(), -1)
                .contentType("application/octet-stream")
                .build()
        )
        
        logger.debug("Uploaded encrypted index for document: $documentId")
    }

    fun uploadEncryptionKey(documentId: String, keyData: ByteArray) {
        val objectName = "key-$documentId.bin"
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(ByteArrayInputStream(keyData), keyData.size.toLong(), -1)
                .contentType("application/octet-stream")
                .build()
        )
        
        logger.debug("Uploaded encryption key for document: $documentId")
    }

    fun downloadIndex(documentId: String): ByteArray {
        val objectName = "index-$documentId.enc"
        
        minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        ).use { inputStream ->
            return inputStream.readBytes()
        }
    }

    fun downloadEncryptionKey(documentId: String): ByteArray {
        val objectName = "key-$documentId.bin"
        
        minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        ).use { inputStream ->
            return inputStream.readBytes()
        }
    }

    fun listAllDocumentIds(): List<String> {
        val results = mutableListOf<String>()
        
        try {
            val objects = minioClient.listObjects(
                io.minio.ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix("index-")
                    .build()
            )
            
            objects.forEach { item ->
                val objectName = item.get().objectName()
                // Extract document ID from "index-{documentId}.enc"
                val documentId = objectName.removePrefix("index-").removeSuffix(".enc")
                results.add(documentId)
            }
        } catch (e: Exception) {
            logger.error("Error listing document IDs", e)
        }
        
        return results
    }
}
