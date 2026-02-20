package de.epa.fulltext.service

import de.epa.fulltext.config.EncryptionConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@ApplicationScoped
class EncryptionService {

    @Inject
    lateinit var encryptionConfig: EncryptionConfig

    private val GCM_IV_LENGTH = 12
    private val GCM_TAG_LENGTH = 16

    fun encrypt(data: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(encryptionConfig.algorithm)
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        
        val encryptedData = cipher.doFinal(data)
        
        // Prepend IV to encrypted data
        return iv + encryptedData
    }

    fun decrypt(encryptedDataWithIv: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(encryptionConfig.algorithm)
        
        // Extract IV from the beginning
        val iv = encryptedDataWithIv.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedData = encryptedDataWithIv.copyOfRange(GCM_IV_LENGTH, encryptedDataWithIv.size)
        
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        
        return cipher.doFinal(encryptedData)
    }

    fun keyToBytes(key: SecretKey): ByteArray {
        return key.encoded
    }

    fun bytesToKey(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, "AES")
    }
}
