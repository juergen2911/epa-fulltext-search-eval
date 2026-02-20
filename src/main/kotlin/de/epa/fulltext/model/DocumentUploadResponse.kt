package de.epa.fulltext.model

data class DocumentUploadResponse(
    val documentId: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val indexed: Boolean,
    val message: String
)
