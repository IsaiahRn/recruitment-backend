package com.gmp.recruitment.models.dto.internal

import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class OtpChallengePayload(
  val userId: UUID,
  val username: String,
  val mode: String = "DISABLED",
  val pendingSecret: String? = null,
  val expiresAt: Instant,
)

data class RefreshTokenPayload(
  val userId: UUID,
  val username: String,
  val expiresAt: OffsetDateTime,
)
