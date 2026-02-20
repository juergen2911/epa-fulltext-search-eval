package de.epa.fulltext.exception

class UnsupportedDocumentTypeException(mimeType: String) : 
    Exception("Unsupported document type: $mimeType")

class DocumentProcessingException(message: String, cause: Throwable? = null) : 
    Exception(message, cause)

class IndexingException(message: String, cause: Throwable? = null) : 
    Exception(message, cause)

class SearchException(message: String, cause: Throwable? = null) : 
    Exception(message, cause)

class ConcurrencyLimitException : 
    Exception("Maximum number of parallel requests reached. Please try again later.")
