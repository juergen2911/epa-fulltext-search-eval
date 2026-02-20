package de.epa.fulltext.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.concurrent.Semaphore

@ApplicationScoped
class ConcurrencyConfig {

    @ConfigProperty(name = "concurrency.max-parallel-requests", defaultValue = "80")
    var maxParallelRequests: Int = 80

    private lateinit var semaphore: Semaphore

    fun getSemaphore(): Semaphore {
        if (!::semaphore.isInitialized) {
            semaphore = Semaphore(maxParallelRequests)
        }
        return semaphore
    }
}
