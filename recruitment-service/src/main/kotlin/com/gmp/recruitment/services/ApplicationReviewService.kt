package com.gmp.recruitment.services

import com.gmp.recruitment.exceptions.BusinessException
import com.gmp.recruitment.exceptions.NotFoundException
import com.gmp.recruitment.models.dto.applicant.ApplicationHistoryItemResponse
import com.gmp.recruitment.models.dto.events.ApplicationLifecycleEvent
import com.gmp.recruitment.models.dto.hr.HrApplicantRowResponse
import com.gmp.recruitment.models.dto.hr.HrApplicationDetailResponse
import com.gmp.recruitment.models.dto.hr.HrDocumentResponse
import com.gmp.recruitment.models.dto.hr.ReviewDecisionRequest
import com.gmp.recruitment.models.entities.ApplicationStatusHistoryEntity
import com.gmp.recruitment.models.entities.DocumentEntity
import com.gmp.recruitment.models.enums.ApplicationStatus
import com.gmp.recruitment.repositories.ApplicationRepository
import com.gmp.recruitment.repositories.ApplicationStatusHistoryRepository
import com.gmp.recruitment.repositories.DocumentRepository
import com.gmp.recruitment.services.storage.DocumentStorageService
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicationReviewService(
  private val applicationRepository: ApplicationRepository,
  private val documentRepository: DocumentRepository,
  private val historyRepository: ApplicationStatusHistoryRepository,
  private val storageService: DocumentStorageService,
  private val auditService: AuditService,
  private val eventPublisherService: EventPublisherService,
  private val applicantProfileMapper: ApplicantProfileMapper,
) {
  @Transactional(readOnly = true)
  fun latestTenAlphabetically(): List<HrApplicantRowResponse> =
    applicationRepository.findLatestTenAlphabetically().map {
      HrApplicantRowResponse(
        applicationId = it.getApplicationId(),
        applicationNumber = it.getApplicationNumber(),
        fullName = it.getFullName(),
        status = ApplicationStatus.valueOf(it.getStatus()),
        submittedAt = it.getSubmittedAt(),
      )
    }

  @Transactional(readOnly = true)
  fun detail(applicationId: UUID): HrApplicationDetailResponse {
    val application = applicationRepository.findById(applicationId)
      .orElseThrow { NotFoundException("Application not found") }

    val documents = documentRepository.findAllByApplicationIdOrderByUploadedAtDesc(application.id)
      .map {
        HrDocumentResponse(
          documentId = it.id,
          originalFilename = it.originalFilename,
          contentType = it.contentType,
          fileSize = it.fileSize,
          uploadedAt = it.uploadedAt,
          downloadPath = "/v1/hr/applications/${application.id}/documents/${it.id}/download",
        )
      }

    val history = historyRepository.findAllByApplicationIdOrderByCreatedAtDesc(application.id)
      .map {
        ApplicationHistoryItemResponse(
          status = it.newStatus,
          reason = it.reason,
          changedAt = it.createdAt,
          changedByUserId = it.changedByUserId,
        )
      }

    return HrApplicationDetailResponse(
      applicationId = application.id,
      applicationNumber = application.applicationNumber,
      status = application.status,
      rejectionReason = application.rejectionReason,
      decisionNote = application.decisionNote,
      submittedAt = application.submittedAt?.toInstant(),
      reviewedAt = application.reviewedAt?.toInstant(),
      applicant = applicantProfileMapper.toHrApplicantProfileResponse(application.profile),
      documents = documents,
      history = history,
    )
  }

  @Transactional(readOnly = true)
  fun downloadDocument(reviewer: AuthenticatedUser, applicationId: UUID, documentId: UUID): Pair<DocumentEntity, Resource> {
    val document = documentRepository.findByIdAndApplicationId(documentId, applicationId)
      .orElseThrow { NotFoundException("Document not found") }
    auditService.log(reviewer.id, "DOCUMENT_DOWNLOADED", "DOCUMENT", document.id.toString())
    return document to storageService.read(document.storageKey)
  }

  @Transactional
  fun markUnderReview(reviewer: AuthenticatedUser, applicationId: UUID, request: ReviewDecisionRequest): HrApplicationDetailResponse =
    transition(reviewer, applicationId, ApplicationStatus.UNDER_REVIEW, request)

  @Transactional
  fun approve(reviewer: AuthenticatedUser, applicationId: UUID, request: ReviewDecisionRequest): HrApplicationDetailResponse =
    transition(reviewer, applicationId, ApplicationStatus.APPROVED, request)

  @Transactional
  fun reject(reviewer: AuthenticatedUser, applicationId: UUID, request: ReviewDecisionRequest): HrApplicationDetailResponse {
    if (request.reason.isNullOrBlank()) {
      throw BusinessException("Rejection reason is required")
    }
    return transition(reviewer, applicationId, ApplicationStatus.REJECTED, request)
  }

  private fun transition(
    reviewer: AuthenticatedUser,
    applicationId: UUID,
    targetStatus: ApplicationStatus,
    request: ReviewDecisionRequest,
  ): HrApplicationDetailResponse {
    val application = applicationRepository.findById(applicationId)
      .orElseThrow { NotFoundException("Application not found") }

    val oldStatus = application.status
    if (oldStatus == ApplicationStatus.APPROVED || oldStatus == ApplicationStatus.REJECTED) {
      throw BusinessException("Terminal applications cannot be changed")
    }
    if (oldStatus == ApplicationStatus.DRAFT) {
      throw BusinessException("Draft applications cannot be reviewed")
    }

    application.status = targetStatus
    application.reviewedAt = OffsetDateTime.now()
    application.reviewedByUserId = reviewer.id
    application.decisionNote = request.note
    application.rejectionReason = request.reason.takeIf { targetStatus == ApplicationStatus.REJECTED }
    application.updatedAt = OffsetDateTime.now()
    applicationRepository.save(application)

    historyRepository.save(
      ApplicationStatusHistoryEntity(
        application = application,
        oldStatus = oldStatus,
        newStatus = targetStatus,
        reason = request.reason ?: request.note,
        changedByUserId = reviewer.id,
      )
    )

    auditService.log(
      reviewer.id,
      "APPLICATION_STATUS_CHANGED",
      "APPLICATION",
      application.id.toString(),
      mapOf("from" to oldStatus.name, "to" to targetStatus.name)
    )

    eventPublisherService.publishApplicationEventAfterCommit(
      ApplicationLifecycleEvent(
        applicationId = application.id,
        applicationNumber = application.applicationNumber,
        userId = reviewer.id,
        oldStatus = oldStatus,
        newStatus = targetStatus,
        changedAt = OffsetDateTime.now(),
      )
    )

    return detail(applicationId)
  }
}
