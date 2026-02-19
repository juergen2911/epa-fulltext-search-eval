package de.epa.fulltext.service

import de.epa.fulltext.exception.IndexingException
import jakarta.enterprise.context.ApplicationScoped
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.ByteBuffersDirectory
import org.jboss.logging.Logger
import java.io.ByteArrayOutputStream

@ApplicationScoped
class IndexingService {

    private val logger = Logger.getLogger(IndexingService::class.java)

    fun createIndex(documentId: String, fileName: String, content: String): ByteArray {
        logger.debug("Creating index for document: $documentId")
        
        try {
            val directory = ByteBuffersDirectory()
            val analyzer = StandardAnalyzer()
            val config = IndexWriterConfig(analyzer)
            
            val writer = IndexWriter(directory, config)
            
            val doc = Document()
            doc.add(StringField("documentId", documentId, Field.Store.YES))
            doc.add(StringField("fileName", fileName, Field.Store.YES))
            doc.add(TextField("content", content, Field.Store.YES))
            
            writer.addDocument(doc)
            writer.commit()
            writer.close()
            
            // Serialize index to byte array
            return serializeIndex(directory)
        } catch (e: Exception) {
            logger.error("Error creating index for document: $documentId", e)
            throw IndexingException("Failed to create index", e)
        }
    }

    private fun serializeIndex(directory: ByteBuffersDirectory): ByteArray {
        val baos = ByteArrayOutputStream()
        
        // Get all files in the directory
        val fileNames = directory.listAll()
        
        // Write number of files
        baos.write(fileNames.size)
        
        fileNames.forEach { fileName ->
            // Write file name length and name
            val nameBytes = fileName.toByteArray()
            baos.write(nameBytes.size shr 8)
            baos.write(nameBytes.size and 0xFF)
            baos.write(nameBytes)
            
            // Write file content
            val fileContent = directory.openInput(fileName, null).use { input ->
                val length = input.length().toInt()
                val bytes = ByteArray(length)
                input.readBytes(bytes, 0, length)
                bytes
            }
            
            // Write content length and content
            baos.write(fileContent.size shr 24)
            baos.write((fileContent.size shr 16) and 0xFF)
            baos.write((fileContent.size shr 8) and 0xFF)
            baos.write(fileContent.size and 0xFF)
            baos.write(fileContent)
        }
        
        return baos.toByteArray()
    }

    fun deserializeIndex(indexData: ByteArray): ByteBuffersDirectory {
        val directory = ByteBuffersDirectory()
        var offset = 0
        
        // Read number of files
        val fileCount = indexData[offset++].toInt() and 0xFF
        
        repeat(fileCount) {
            // Read file name length
            val nameLength = ((indexData[offset++].toInt() and 0xFF) shl 8) or
                            (indexData[offset++].toInt() and 0xFF)
            
            // Read file name
            val nameBytes = indexData.copyOfRange(offset, offset + nameLength)
            offset += nameLength
            val fileName = String(nameBytes)
            
            // Read content length
            val contentLength = ((indexData[offset++].toInt() and 0xFF) shl 24) or
                               ((indexData[offset++].toInt() and 0xFF) shl 16) or
                               ((indexData[offset++].toInt() and 0xFF) shl 8) or
                               (indexData[offset++].toInt() and 0xFF)
            
            // Read content
            val content = indexData.copyOfRange(offset, offset + contentLength)
            offset += contentLength
            
            // Write to directory
            directory.createOutput(fileName, null).use { output ->
                output.writeBytes(content, 0, content.size)
            }
        }
        
        return directory
    }

    fun mergeIndexes(indexes: List<ByteBuffersDirectory>): ByteBuffersDirectory {
        logger.debug("Merging ${indexes.size} indexes")
        
        val mergedDirectory = ByteBuffersDirectory()
        val analyzer = StandardAnalyzer()
        val config = IndexWriterConfig(analyzer)
        
        val writer = IndexWriter(mergedDirectory, config)
        
        indexes.forEach { indexDir ->
            try {
                writer.addIndexes(indexDir)
            } catch (e: Exception) {
                logger.warn("Error merging index", e)
            }
        }
        
        writer.commit()
        writer.close()
        
        return mergedDirectory
    }
}
