package de.epa.fulltext.service

import de.epa.fulltext.exception.SearchException
import de.epa.fulltext.model.SearchResult
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.ByteBuffersDirectory
import org.jboss.logging.Logger

@ApplicationScoped
class SearchService {

    private val logger = Logger.getLogger(SearchService::class.java)

    @Inject
    lateinit var storageService: StorageService

    @Inject
    lateinit var encryptionService: EncryptionService

    @Inject
    lateinit var indexingService: IndexingService

    fun search(queryString: String, maxResults: Int): List<SearchResult> {
        logger.debug("Searching for: $queryString")
        
        try {
            // Get all document IDs
            val documentIds = storageService.listAllDocumentIds()
            
            if (documentIds.isEmpty()) {
                logger.info("No documents indexed yet")
                return emptyList()
            }
            
            // Download and decrypt all indexes
            val indexes = mutableListOf<ByteBuffersDirectory>()
            
            documentIds.forEach { documentId ->
                try {
                    val encryptedIndex = storageService.downloadIndex(documentId)
                    val keyBytes = storageService.downloadEncryptionKey(documentId)
                    val key = encryptionService.bytesToKey(keyBytes)
                    val decryptedIndex = encryptionService.decrypt(encryptedIndex, key)
                    val directory = indexingService.deserializeIndex(decryptedIndex)
                    indexes.add(directory)
                } catch (e: Exception) {
                    logger.warn("Error loading index for document: $documentId", e)
                }
            }
            
            if (indexes.isEmpty()) {
                logger.warn("No valid indexes found")
                return emptyList()
            }
            
            // Merge all indexes
            val mergedIndex = indexingService.mergeIndexes(indexes)
            
            // Perform search
            val reader = DirectoryReader.open(mergedIndex)
            val searcher = IndexSearcher(reader)
            val analyzer = StandardAnalyzer()
            val parser = QueryParser("content", analyzer)
            val query = parser.parse(queryString)
            
            val topDocs = searcher.search(query, maxResults)
            
            val results = topDocs.scoreDocs.map { scoreDoc ->
                val doc = searcher.storedFields().document(scoreDoc.doc)
                val content = doc.get("content") ?: ""
                val snippet = if (content.length > 200) {
                    content.substring(0, 200) + "..."
                } else {
                    content
                }
                
                SearchResult(
                    documentId = doc.get("documentId") ?: "unknown",
                    fileName = doc.get("fileName") ?: "unknown",
                    score = scoreDoc.score,
                    snippet = snippet
                )
            }
            
            reader.close()
            
            logger.info("Search completed. Found ${results.size} results")
            return results
        } catch (e: Exception) {
            logger.error("Error performing search", e)
            throw SearchException("Failed to perform search", e)
        }
    }
}
