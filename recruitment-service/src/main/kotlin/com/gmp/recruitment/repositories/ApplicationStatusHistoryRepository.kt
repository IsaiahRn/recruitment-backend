package com.gmp.recruitment.repositories

import com.gmp.recruitment.models.entities.ApplicationStatusHistoryEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationStatusHistoryRepository : JpaRepository<ApplicationStatusHistoryEntity, UUID> {
    fun findAllByApplicationIdOrderByCreatedAtDesc(applicationId: UUID): List<ApplicationStatusHistoryEntity>
}
