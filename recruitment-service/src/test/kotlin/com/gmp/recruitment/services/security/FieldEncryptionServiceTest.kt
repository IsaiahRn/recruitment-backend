package com.gmp.recruitment.services.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class FieldEncryptionServiceTest {
    private val encryptionService = FieldEncryptionService("unit-test-encryption-secret", "unit-test-hash-secret")

    @Test
    fun `encrypt and decrypt round trip`() {
        val encrypted = encryptionService.encrypt("1199980012345678")
        val decrypted = encryptionService.decrypt(encrypted)

        assertNotEquals("1199980012345678", encrypted)
        assertEquals("1199980012345678", decrypted)
    }

    @Test
    fun `fingerprint is deterministic`() {
        val a = encryptionService.fingerprint("abc")
        val b = encryptionService.fingerprint("abc")
        assertEquals(a, b)
    }
}
