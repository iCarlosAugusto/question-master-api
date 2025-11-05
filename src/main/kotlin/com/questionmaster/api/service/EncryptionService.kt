package com.questionmaster.api.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.questionmaster.api.domain.dto.response.EncryptedDataResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService(
    private val objectMapper: ObjectMapper
) {
    
    @Value("\${app.encryption.key}")
    private val encryptionKey: String = ""
    
    private val algorithm = "AES/GCM/NoPadding"
    private val gcmTagLength = 128 // Tag length in bits (must be one of {128, 120, 112, 104, 96})
    private val ivLength = 12 // 96 bits for GCM
    
    private val secretKey: SecretKey by lazy {
        if (encryptionKey.isNotBlank()) {
            try {
                // Use provided key (must be 32 bytes for AES-256, encoded in Base64)
                val keyBytes = Base64.getDecoder().decode(encryptionKey)
                if (keyBytes.size != 32) {
                    throw IllegalArgumentException("Encryption key must be 32 bytes (256 bits) when decoded. Got ${keyBytes.size} bytes")
                }
                SecretKeySpec(keyBytes, "AES")
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid encryption key format. Must be valid Base64 encoded 32-byte key.", e)
            }
        } else {
            // Generate a new key (for development only)
            throw IllegalArgumentException("Encryption key missing", )
        }
    }
    
    /**
     * Encrypts a data object to JSON and then encrypts it
     */
    fun <T> encrypt(data: T): EncryptedDataResponse {
        try {
            // Convert object to JSON string
            val jsonData = objectMapper.writeValueAsString(data)
            
            // Generate IV
            val iv = ByteArray(ivLength)
            java.security.SecureRandom().nextBytes(iv)
            
            // Initialize cipher
            val cipher = Cipher.getInstance(algorithm)
            val parameterSpec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
            
            // Encrypt
            val encryptedBytes = cipher.doFinal(jsonData.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = ByteArray(ivLength + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, ivLength)
            System.arraycopy(encryptedBytes, 0, combined, ivLength, encryptedBytes.size)
            
            // Encode to Base64
            val encryptedBase64 = Base64.getEncoder().encodeToString(combined)
            return EncryptedDataResponse(
                encryptedData = encryptedBase64
            )
        } catch (e: Exception) {
            throw RuntimeException("Error encrypting data: ${e.message}", e)
        }
    }
}