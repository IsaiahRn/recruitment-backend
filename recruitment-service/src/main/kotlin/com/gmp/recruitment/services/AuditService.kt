package com.gmp.recruitment.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.gmp.recruitment.models.entities.AuditLogEntity
import com.gmp.recruitment.repositories.AuditLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun log(
        actorUserId: java.util.UUID?,
        action: String,
        entityType: String,
        entityId: String,
        metadata: Any? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
    ) {
        auditLogRepository.save(
            AuditLogEntity(
                actorUserId = actorUserId,
                action = action,
                entityType = entityType,
                entityId = entityId,
                metadataJson = metadata?.let(objectMapper::writeValueAsString),
                ipAddress = ipAddress,
                userAgent = userAgent,
            )
        )
    }
}
