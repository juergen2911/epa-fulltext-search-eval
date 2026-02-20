package de.epa.fulltext.resource

import de.epa.fulltext.exception.SearchException
import de.epa.fulltext.model.SearchRequest
import de.epa.fulltext.model.SearchResponse
import de.epa.fulltext.service.SearchService
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

@Path("/api/search")
class SearchResource {

    private val logger = Logger.getLogger(SearchResource::class.java)

    @Inject
    lateinit var searchService: SearchService

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun search(request: SearchRequest): Response {
        logger.info("Received search request for: ${request.query}")

        return try {
            val results = searchService.search(request.query, request.maxResults)
            
            val response = SearchResponse(
                totalHits = results.size.toLong(),
                results = results
            )

            Response.ok(response).build()
        } catch (e: SearchException) {
            logger.error("Error performing search", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Search failed: ${e.message}"))
                .build()
        } catch (e: Exception) {
            logger.error("Unexpected error during search", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "An unexpected error occurred"))
                .build()
        }
    }
}
