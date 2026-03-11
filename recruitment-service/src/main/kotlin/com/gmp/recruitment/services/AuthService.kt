package com.gmp.recruitment.services

import com.gmp.recruitment.exceptions.BusinessException
import com.gmp.recruitment.models.dto.auth.ApplicantRegistrationRequest
import com.gmp.recruitment.models.dto.auth.ApplicantRegistrationResponse
import com.gmp.recruitment.models.dto.auth.AuthenticatedUserResponse
import com.gmp.recruitment.models.dto.auth.PasswordLoginRequest
import com.gmp.recruitment.models.dto.auth.RefreshTokenRequest
import com.gmp.recruitment.models.dto.auth.TokenResponse
import com.gmp.recruitment.models.dto.internal.RefreshTokenPayload
import com.gmp.recruitment.models.entities.UserEntity
import com.gmp.recruitment.models.enums.UserRole
import com.gmp.recruitment.models.enums.UserStatus
import com.gmp.recruitment.repositories.UserRepository
import com.gmp.recruitment.services.store.RefreshTokenStore
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import com.gmp.recruitment.utilities.security.CustomUserDetailsService
import com.gmp.recruitment.utilities.security.JwtTokenProvider
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenStore: RefreshTokenStore,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val auditService: AuditService,
) {
    @Transactional
    fun registerApplicant(request: ApplicantRegistrationRequest): ApplicantRegistrationResponse {
        if (userRepository.existsByUsernameIgnoreCase(request.username)) {
            throw BusinessException("Username already exists")
        }
        if (userRepository.existsByEmailIgnoreCase(request.email)) {
            throw BusinessException("Email already exists")
        }

        val entity = userRepository.save(
            UserEntity(
                username = request.username.trim(),
                email = request.email.trim().lowercase(),
                fullName = request.fullName.trim(),
                passwordHash = passwordEncoder.encode(request.password),
                role = UserRole.APPLICANT,
                otpEnabled = false,
                totpConfirmed = false,
            )
        )

        auditService.log(entity.id, "APPLICANT_REGISTERED", "USER", entity.id.toString())
        return ApplicantRegistrationResponse(
            userId = entity.id,
            username = entity.username,
            email = entity.email,
            fullName = entity.fullName,
            role = entity.role,
            message = "Account created successfully. You can sign in immediately.",
        )
    }

    @Transactional
    fun login(request: PasswordLoginRequest): TokenResponse {
        val user = userRepository.findByUsernameIgnoreCase(request.username.trim())
            .orElseThrow { BusinessException("Invalid credentials") }

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException("Account is disabled")
        }
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BusinessException("Invalid credentials")
        }

        val principal = userDetailsService.loadUserByUsername(user.username)
        return buildTokenResponse(principal, updateLastLogin = true)
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val stored = refreshTokenStore.get(request.refreshToken) ?: throw BusinessException("Refresh token not recognized")
        val principal = userDetailsService.loadById(stored.userId.toString())

        if (!jwtTokenProvider.isRefreshToken(request.refreshToken)) {
            throw BusinessException("Invalid refresh token")
        }

        refreshTokenStore.delete(request.refreshToken)
        return buildTokenResponse(principal, updateLastLogin = false)
    }

    fun logout(request: RefreshTokenRequest) {
        refreshTokenStore.delete(request.refreshToken)
    }

    fun me(principal: AuthenticatedUser): AuthenticatedUserResponse {
        val entity = userRepository.findById(principal.id).orElseThrow()
        return AuthenticatedUserResponse(
            id = principal.id,
            username = principal.username,
            email = entity.email,
            fullName = principal.fullName,
            role = principal.role,
        )
    }

    private fun buildTokenResponse(principal: AuthenticatedUser, updateLastLogin: Boolean): TokenResponse {
        val (accessToken, accessExpiresAt) = jwtTokenProvider.generateAccessToken(principal)
        val (refreshToken, refreshExpiresAt) = jwtTokenProvider.generateRefreshToken(principal)

        refreshTokenStore.save(
            refreshToken,
            RefreshTokenPayload(principal.id, principal.username, refreshExpiresAt),
            Duration.between(OffsetDateTime.now(), refreshExpiresAt)
        )

        if (updateLastLogin) {
            val entity = userRepository.findById(principal.id).orElseThrow()
            entity.lastLoginAt = OffsetDateTime.now()
            userRepository.save(entity)
        }

        auditService.log(principal.id, "AUTH_LOGIN_SUCCESS", "USER", principal.id.toString())

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAt = accessExpiresAt,
            refreshTokenExpiresAt = refreshExpiresAt,
            user = me(principal),
        )
    }
}
