package de.epa.fulltext.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@ApplicationScoped
class EncryptionConfig {

    @ConfigProperty(name = "encryption.algorithm", defaultValue = "AES/GCM/NoPadding")
    lateinit var algorithm: String

    @ConfigProperty(name = "encryption.key-size", defaultValue = "256")
    var keySize: Int = 256

    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(keySize, SecureRandom())
        return keyGen.generateKey()
    }
}
