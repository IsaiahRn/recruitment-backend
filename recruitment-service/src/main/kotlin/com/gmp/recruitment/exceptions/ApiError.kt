package com.gmp.recruitment.exceptions

import java.time.Instant

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val code: String,
    val message: String,
    val details: Map<String, String>? = null,
)
