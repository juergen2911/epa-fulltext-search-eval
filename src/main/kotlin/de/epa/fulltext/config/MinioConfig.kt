package de.epa.fulltext.config

import io.minio.MinioClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class MinioConfig {

    @ConfigProperty(name = "minio.endpoint")
    lateinit var endpoint: String

    @ConfigProperty(name = "minio.access-key")
    lateinit var accessKey: String

    @ConfigProperty(name = "minio.secret-key")
    lateinit var secretKey: String

    @Produces
    @ApplicationScoped
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }
}
