package de.epa.fulltext.resource

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/api/health")
class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "fulltext-search-service"
        )
    }
}
