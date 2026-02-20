package de.epa.fulltext

import de.epa.fulltext.config.EncryptionConfig
import de.epa.fulltext.service.EncryptionService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@QuarkusTest
class EncryptionServiceTest {

    @Inject
    lateinit var encryptionService: EncryptionService

    @Inject
    lateinit var encryptionConfig: EncryptionConfig

    @Test
    fun testEncryptDecrypt() {
        val originalData = "This is a test message for encryption".toByteArray()
        val key = encryptionConfig.generateKey()

        val encrypted = encryptionService.encrypt(originalData, key)
        val decrypted = encryptionService.decrypt(encrypted, key)

        assertArrayEquals(originalData, decrypted)
    }

    @Test
    fun testKeyConversion() {
        val originalKey = encryptionConfig.generateKey()
        val keyBytes = encryptionService.keyToBytes(originalKey)
        val restoredKey = encryptionService.bytesToKey(keyBytes)

        assertEquals(originalKey.algorithm, restoredKey.algorithm)
        assertArrayEquals(originalKey.encoded, restoredKey.encoded)
    }

    @Test
    fun testEncryptedDataDifferent() {
        val originalData = "Test data".toByteArray()
        val key = encryptionConfig.generateKey()

        val encrypted1 = encryptionService.encrypt(originalData, key)
        val encrypted2 = encryptionService.encrypt(originalData, key)

        // Each encryption should produce different ciphertext due to random IV
        assertFalse(encrypted1.contentEquals(encrypted2))

        // But both should decrypt to the same original data
        assertArrayEquals(originalData, encryptionService.decrypt(encrypted1, key))
        assertArrayEquals(originalData, encryptionService.decrypt(encrypted2, key))
    }
}
