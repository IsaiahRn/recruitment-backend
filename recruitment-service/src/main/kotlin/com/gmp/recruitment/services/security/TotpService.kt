package com.gmp.recruitment.services.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TotpService(
    @Value("\${app.security.authenticator.issuer:Recruitment Platform}") private val issuer: String,
) {
    private val base32Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val secureRandom = SecureRandom()

    fun generateSecret(length: Int = 20): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return encodeBase32(bytes)
    }

    fun issuer(): String = issuer

    fun buildOtpAuthUrl(accountName: String, secret: String): String {
        val encodedIssuer = java.net.URLEncoder.encode(issuer, Charsets.UTF_8)
        val encodedAccount = java.net.URLEncoder.encode(accountName, Charsets.UTF_8)
        return "otpauth://totp/$encodedIssuer:$encodedAccount?secret=$secret&issuer=$encodedIssuer&algorithm=SHA1&digits=6&period=30"
    }

    fun verifyCode(secret: String, code: String, instant: Instant = Instant.now(), allowedWindows: Int = 1): Boolean {
        val normalizedCode = code.filter(Char::isDigit)
        if (normalizedCode.length != 6) return false
        val counter = instant.epochSecond / 30
        return (-allowedWindows..allowedWindows).any { window ->
            generateCode(secret, counter + window) == normalizedCode
        }
    }

    fun generateCode(secret: String, counter: Long): String {
        val key = decodeBase32(secret)
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val hash = mac.doFinal(data)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            (hash[offset + 3].toInt() and 0xff)
        val otp = binary % 1_000_000
        return otp.toString().padStart(6, '0')
    }

    private fun encodeBase32(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val output = StringBuilder((bytes.size * 8 + 4) / 5)
        var current = bytes[0].toInt() and 0xff
        var bitsRemaining = 8
        var index = 1

        while (bitsRemaining > 0 || index < bytes.size) {
            if (bitsRemaining < 5) {
                if (index < bytes.size) {
                    current = (current shl 8) or (bytes[index].toInt() and 0xff)
                    bitsRemaining += 8
                    index++
                } else {
                    val pad = 5 - bitsRemaining
                    current = current shl pad
                    bitsRemaining += pad
                }
            }
            val value = (current shr (bitsRemaining - 5)) and 0x1f
            bitsRemaining -= 5
            output.append(base32Alphabet[value])
        }

        return output.toString()
    }

    private fun decodeBase32(secret: String): ByteArray {
        val normalized = secret.uppercase().replace("=", "")
        if (normalized.isEmpty()) return ByteArray(0)

        val output = ArrayList<Byte>()
        var buffer = 0
        var bitsLeft = 0

        normalized.forEach { ch ->
            val value = base32Alphabet.indexOf(ch)
            require(value >= 0) { "Invalid base32 secret" }
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output.add(((buffer shr (bitsLeft - 8)) and 0xff).toByte())
                bitsLeft -= 8
            }
        }
        return output.toByteArray()
    }
}
