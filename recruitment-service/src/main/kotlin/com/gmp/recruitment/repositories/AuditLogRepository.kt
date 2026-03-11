package com.gmp.recruitment.repositories

import com.gmp.recruitment.models.entities.AuditLogEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface AuditLogRepository : JpaRepository<AuditLogEntity, UUID> {
    fun findTop100ByOrderByCreatedAtDesc(): List<AuditLogEntity>
}
