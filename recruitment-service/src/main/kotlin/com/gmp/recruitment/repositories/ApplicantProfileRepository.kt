package com.gmp.recruitment.repositories

import com.gmp.recruitment.models.entities.ApplicantProfileEntity
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicantProfileRepository : JpaRepository<ApplicantProfileEntity, UUID> {
    fun findByUserId(userId: UUID): Optional<ApplicantProfileEntity>
    fun findByNationalIdHash(nationalIdHash: String): Optional<ApplicantProfileEntity>
    fun countByNidaVerifiedFalseOrNesaVerifiedFalse(): Long
}
