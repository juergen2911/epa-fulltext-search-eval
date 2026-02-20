package de.epa.fulltext.model

data class SearchRequest(
    val query: String,
    val maxResults: Int = 10
)
