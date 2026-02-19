package de.epa.fulltext.model

data class SearchResponse(
    val totalHits: Long,
    val results: List<SearchResult>
)

data class SearchResult(
    val documentId: String,
    val fileName: String,
    val score: Float,
    val snippet: String
)
