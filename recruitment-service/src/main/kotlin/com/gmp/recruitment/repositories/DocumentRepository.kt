package com.gmp.recruitment.repositories

import com.gmp.recruitment.models.entities.DocumentEntity
import com.gmp.recruitment.models.enums.DocumentType
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentRepository : JpaRepository<DocumentEntity, UUID> {
    fun findTopByUploadedByUserIdAndTypeOrderByUploadedAtDesc(userId: UUID, type: DocumentType): Optional<DocumentEntity>
    fun findAllByApplicationIdOrderByUploadedAtDesc(applicationId: UUID): List<DocumentEntity>
    fun findByIdAndApplicationId(documentId: UUID, applicationId: UUID): Optional<DocumentEntity>
}
