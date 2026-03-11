package com.gmp.recruitment.services.security

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TotpServiceTest {
    private val totpService = TotpService("Recruitment Platform")

    @Test
    fun `generated code verifies for same time window`() {
        val secret = totpService.generateSecret()
        val now = Instant.parse("2026-03-10T12:00:00Z")
        val counter = now.epochSecond / 30
        val code = totpService.generateCode(secret, counter)

        assertTrue(totpService.verifyCode(secret, code, now))
        assertFalse(totpService.verifyCode(secret, "000000", now))
    }
}
