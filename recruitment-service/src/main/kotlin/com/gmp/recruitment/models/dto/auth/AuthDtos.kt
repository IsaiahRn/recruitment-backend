package com.gmp.recruitment.models.dto.auth

import com.gmp.recruitment.models.enums.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class PasswordLoginRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String,
)

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class ApplicantRegistrationRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 100)
    val username: String,
    @field:Email
    val email: String,
    @field:NotBlank
    @field:Size(min = 4, max = 150)
    val fullName: String,
    @field:Size(min = 8, max = 128)
    val password: String,
)

data class ApplicantRegistrationResponse(
    val userId: UUID,
    val username: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val message: String,
)

data class AuthenticatedUserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
)

data class TokenResponse(
  val accessToken: String,
  val refreshToken: String,
  val accessTokenExpiresAt: OffsetDateTime,
  val refreshTokenExpiresAt: OffsetDateTime,
  val user: AuthenticatedUserResponse,
)
