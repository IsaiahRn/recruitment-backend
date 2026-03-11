package com.gmp.recruitment.models.dto.admin

import com.gmp.recruitment.models.enums.UserRole
import com.gmp.recruitment.models.enums.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class UserUpsertRequest(
    @field:NotBlank
    val username: String,
    @field:Email
    val email: String,
    @field:NotBlank
    val fullName: String,
    val role: UserRole,
    @field:Size(min = 8, max = 128)
    val password: String? = null,
)

data class UserResponse(
  val id: UUID,
  val username: String,
  val email: String,
  val fullName: String,
  val role: UserRole,
  val status: UserStatus,
  val lastLoginAt: OffsetDateTime?,
  val createdAt: OffsetDateTime,
)

data class AuditLogResponse(
  val id: UUID,
  val actorUserId: UUID?,
  val action: String,
  val entityType: String,
  val entityId: String,
  val metadataJson: String?,
  val createdAt: Instant,
)
