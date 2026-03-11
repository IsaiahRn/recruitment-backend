package com.gmp.recruitment.models.dto.applicant

import com.gmp.recruitment.models.enums.ApplicationStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class NidVerificationRequest(
  @field:NotBlank
  @field:Pattern(regexp = "\\d{16}", message = "National ID must be 16 digits")
  val nationalIdNumber: String,
)

data class NesaVerificationRequest(
  @field:NotBlank
  @field:Pattern(regexp = "\\d{16}", message = "National ID must be 16 digits")
  val nationalIdNumber: String,
)

data class ApplicantProfileUpsertRequest(
  @field:Size(max = 30)
  val phone: String?,
  @field:Size(max = 255)
  val addressLine: String?,
  @field:Size(max = 80)
  val province: String?,
  @field:Size(max = 80)
  val district: String?,
)

data class ApplicantProfileResponse(
  val userId: UUID,
  val nationalIdNumber: String,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate?,
  val gender: String?,
  val phone: String?,
  val addressLine: String?,
  val province: String?,
  val district: String?,
  val schoolName: String?,
  val grade: String?,
  val optionAttended: String?,
  val completionYear: Int?,
  val nidaVerified: Boolean,
  val nesaVerified: Boolean,
)

data class FileUploadResponse(
  val documentId: UUID,
  val originalFilename: String,
  val contentType: String,
  val fileSize: Long,
)

data class ApplicationHistoryItemResponse(
  val status: ApplicationStatus,
  val reason: String?,
  val changedAt: OffsetDateTime,
  val changedByUserId: UUID?,
)

data class ApplicationStatusResponse(
  val applicationId: UUID?,
  val applicationNumber: String?,
  val status: ApplicationStatus,
  val rejectionReason: String?,
  val decisionNote: String?,
  val submittedAt: OffsetDateTime?,
  val reviewedAt: OffsetDateTime?,
  val history: List<ApplicationHistoryItemResponse>,
)
