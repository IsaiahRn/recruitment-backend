package com.gmp.recruitment.models.dto.hr

import com.gmp.recruitment.models.dto.applicant.ApplicationHistoryItemResponse
import com.gmp.recruitment.models.enums.ApplicationStatus
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class HrApplicantRowResponse(
  val applicationId: UUID,
  val applicationNumber: String,
  val fullName: String,
  val status: ApplicationStatus,
  val submittedAt: Instant,
)

data class ReviewDecisionRequest(
  @field:Size(max = 500)
  val reason: String? = null,
  @field:Size(max = 500)
  val note: String? = null,
)

data class HrApplicationDetailResponse(
  val applicationId: UUID,
  val applicationNumber: String,
  val status: ApplicationStatus,
  val rejectionReason: String?,
  val decisionNote: String?,
  val submittedAt: Instant?,
  val reviewedAt: Instant?,
  val applicant: HrApplicantProfileResponse,
  val documents: List<HrDocumentResponse>,
  val history: List<ApplicationHistoryItemResponse>,
)

data class HrApplicantProfileResponse(
  val profileId: UUID,
  val firstName: String,
  val lastName: String,
  val nationalIdNumber: String,
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
)

data class HrDocumentResponse(
  val documentId: UUID,
  val originalFilename: String,
  val contentType: String,
  val fileSize: Long,
  val uploadedAt: Instant,
  val downloadPath: String,
)
