package de.epa.fulltext.resource

import de.epa.fulltext.exception.ConcurrencyLimitException
import de.epa.fulltext.exception.DocumentProcessingException
import de.epa.fulltext.exception.UnsupportedDocumentTypeException
import de.epa.fulltext.model.DocumentUploadResponse
import de.epa.fulltext.service.DocumentProcessingService
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.MultipartForm
import org.jboss.resteasy.reactive.PartType

@Path("/api/documents")
class DocumentResource {

    private val logger = Logger.getLogger(DocumentResource::class.java)

    @Inject
    lateinit var documentProcessingService: DocumentProcessingService

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadDocument(@MultipartForm form: DocumentUploadForm): Response {
        logger.info("Received document upload request: ${form.fileName}")

        return try {
            val result = documentProcessingService.processDocument(
                fileName = form.fileName ?: "unknown",
                mimeType = form.mimeType ?: "application/octet-stream",
                fileData = form.file
            )

            Response.ok(result).build()
        } catch (e: UnsupportedDocumentTypeException) {
            logger.warn("Unsupported document type", e)
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: ConcurrencyLimitException) {
            logger.warn("Concurrency limit reached")
            Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: DocumentProcessingException) {
            logger.error("Error processing document", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Failed to process document: ${e.message}"))
                .build()
        } catch (e: Exception) {
            logger.error("Unexpected error processing document", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "An unexpected error occurred"))
                .build()
        }
    }

    data class DocumentUploadForm(
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        val file: ByteArray,
        
        @PartType(MediaType.TEXT_PLAIN)
        val fileName: String?,
        
        @PartType(MediaType.TEXT_PLAIN)
        val mimeType: String?
    )
}
