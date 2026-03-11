package com.gmp.recruitment.services

import com.gmp.recruitment.exceptions.BusinessException
import com.gmp.recruitment.exceptions.NotFoundException
import com.gmp.recruitment.models.dto.admin.AuditLogResponse
import com.gmp.recruitment.models.dto.admin.UserResponse
import com.gmp.recruitment.models.dto.admin.UserUpsertRequest
import com.gmp.recruitment.models.entities.UserEntity
import com.gmp.recruitment.models.enums.UserStatus
import com.gmp.recruitment.repositories.AuditLogRepository
import com.gmp.recruitment.repositories.UserRepository
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val auditLogRepository: AuditLogRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditService: AuditService,
) {
    fun listUsers(): List<UserResponse> = userRepository.findAll()
        .sortedBy { it.username.lowercase() }
        .map(::toResponse)

    fun latestAuditLogs(): List<AuditLogResponse> = auditLogRepository.findTop100ByOrderByCreatedAtDesc()
        .map {
            AuditLogResponse(
                id = it.id,
                actorUserId = it.actorUserId,
                action = it.action,
                entityType = it.entityType,
                entityId = it.entityId,
                metadataJson = it.metadataJson,
                createdAt = it.createdAt,
            )
        }

    @Transactional
    fun createUser(actorId: UUID, request: UserUpsertRequest): UserResponse {
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
                passwordHash = passwordEncoder.encode(request.password ?: "Password@123"),
                role = request.role,
                otpEnabled = false,
                totpSecretEncrypted = null,
                totpConfirmed = false,
            )
        )
        auditService.log(actorId, "USER_CREATED", "USER", entity.id.toString())
        return toResponse(entity)
    }

    @Transactional
    fun updateUser(actorId: UUID, userId: UUID, request: UserUpsertRequest): UserResponse {
        val entity = userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }

        val normalizedUsername = request.username.trim()
        if (!entity.username.equals(normalizedUsername, ignoreCase = true) && userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw BusinessException("Username already exists")
        }
        val normalizedEmail = request.email.trim().lowercase()
        if (!entity.email.equals(normalizedEmail, ignoreCase = true) && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw BusinessException("Email already exists")
        }

        entity.username = normalizedUsername
        entity.email = normalizedEmail
        entity.fullName = request.fullName.trim()
        entity.role = request.role
        request.password?.takeIf { it.isNotBlank() }?.let { entity.passwordHash = passwordEncoder.encode(it) }
        entity.updatedAt = OffsetDateTime.now()
        userRepository.save(entity)
        auditService.log(actorId, "USER_UPDATED", "USER", entity.id.toString())
        return toResponse(entity)
    }

    @Transactional
    fun disableUser(actorId: UUID, userId: UUID): UserResponse {
        val entity = userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }
        entity.status = UserStatus.DISABLED
        entity.updatedAt = OffsetDateTime.now()
        userRepository.save(entity)
        auditService.log(actorId, "USER_DISABLED", "USER", entity.id.toString())
        return toResponse(entity)
    }

    @Transactional
    fun enableUser(actorId: UUID, userId: UUID): UserResponse {
        val entity = userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }
        entity.status = UserStatus.ACTIVE
        entity.updatedAt = OffsetDateTime.now()
        userRepository.save(entity)
        auditService.log(actorId, "USER_ENABLED", "USER", entity.id.toString())
        return toResponse(entity)
    }

    @Transactional
    fun resetAuthenticator(actorId: UUID, userId: UUID): UserResponse {
        val entity = userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }
        entity.updatedAt = OffsetDateTime.now()
        userRepository.save(entity)
        auditService.log(actorId, "USER_AUTHENTICATOR_RESET_SKIPPED", "USER", entity.id.toString())
        return toResponse(entity)
    }

    private fun toResponse(entity: UserEntity): UserResponse =
        UserResponse(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            fullName = entity.fullName,
            role = entity.role,
            status = entity.status,
            lastLoginAt = entity.lastLoginAt,
            createdAt = entity.createdAt,
        )
}
