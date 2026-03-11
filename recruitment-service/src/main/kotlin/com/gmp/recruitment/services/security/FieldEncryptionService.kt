package com.gmp.recruitment.services.security

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FieldEncryptionService(
    @Value("\${app.security.data-encryption-secret}") secret: String,
    @Value("\${app.security.data-hash-secret:\${app.security.data-encryption-secret}}") hashSecret: String,
) {
    private val secureRandom = SecureRandom()
    private val encryptionKey = SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(secret.toByteArray()).copyOf(32), "AES")
    private val hashKey = SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(hashSecret.toByteArray()).copyOf(32), "HmacSHA256")

    fun encrypt(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val iv = ByteArray(12).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.size + encrypted.size).put(iv).put(encrypted).array())
    }

    fun decrypt(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val decoded = Base64.getDecoder().decode(value)
        val iv = decoded.copyOfRange(0, 12)
        val ciphertext = decoded.copyOfRange(12, decoded.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    fun fingerprint(value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(hashKey)
        return mac.doFinal(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
