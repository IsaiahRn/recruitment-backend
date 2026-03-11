package com.gmp.recruitment.services

import com.gmp.recruitment.exceptions.BusinessException
import com.gmp.recruitment.exceptions.NotFoundException
import com.gmp.recruitment.helpers.AppNumberGenerator
import com.gmp.recruitment.integrations.nesa.NesaClient
import com.gmp.recruitment.integrations.nid.NidClient
import com.gmp.recruitment.models.dto.applicant.*
import com.gmp.recruitment.models.dto.events.ApplicationLifecycleEvent
import com.gmp.recruitment.models.entities.ApplicantProfileEntity
import com.gmp.recruitment.models.entities.ApplicationEntity
import com.gmp.recruitment.models.entities.ApplicationStatusHistoryEntity
import com.gmp.recruitment.models.entities.DocumentEntity
import com.gmp.recruitment.models.enums.ApplicationStatus
import com.gmp.recruitment.models.enums.DocumentType
import com.gmp.recruitment.repositories.*
import com.gmp.recruitment.services.security.FieldEncryptionService
import com.gmp.recruitment.services.storage.DocumentStorageService
import com.gmp.recruitment.utilities.FileValidator
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import java.time.OffsetDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ApplicantService(
    private val userRepository: UserRepository,
    private val applicantProfileRepository: ApplicantProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val documentRepository: DocumentRepository,
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository,
    private val nidClient: NidClient,
    private val nesaClient: NesaClient,
    private val documentStorageService: DocumentStorageService,
    private val fileValidator: FileValidator,
    private val appNumberGenerator: AppNumberGenerator,
    private val auditService: AuditService,
    private val eventPublisherService: EventPublisherService,
    private val applicantProfileMapper: ApplicantProfileMapper,
    private val fieldEncryptionService: FieldEncryptionService,
) {
    @Transactional(readOnly = true)
    fun getMyProfile(principal: AuthenticatedUser): ApplicantProfileResponse? =
        applicantProfileRepository.findByUserId(principal.id).map(applicantProfileMapper::toApplicantProfileResponse).orElse(null)

    @Transactional
    fun verifyNid(principal: AuthenticatedUser, request: NidVerificationRequest): ApplicantProfileResponse {
        val user = userRepository.findById(principal.id).orElseThrow()
        val nidProfile = nidClient.fetchProfile(request.nationalIdNumber)
        val nationalIdHash = fieldEncryptionService.fingerprint(nidProfile.nationalIdNumber)

        applicantProfileRepository.findByNationalIdHash(nationalIdHash)
            .filter { it.user.id != principal.id }
            .ifPresent { throw BusinessException("This national ID is already attached to another applicant") }

        val profile = applicantProfileRepository.findByUserId(principal.id).orElse(
            ApplicantProfileEntity(
                user = user,
                nationalIdNumberEncrypted = fieldEncryptionService.encrypt(nidProfile.nationalIdNumber)!!,
                nationalIdHash = nationalIdHash,
                firstName = nidProfile.firstName,
                lastName = nidProfile.lastName,
            )
        )

        applicantProfileMapper.applyVerifiedNid(profile, nidProfile)
        profile.nidaVerified = true
        profile.nidaVerifiedAt = OffsetDateTime.now()
        profile.updatedAt = OffsetDateTime.now()

        applicantProfileRepository.save(profile)
        ensureDraftApplication(profile)

        auditService.log(principal.id, "NID_VERIFIED", "PROFILE", profile.id.toString())
        return applicantProfileMapper.toApplicantProfileResponse(profile)
    }

    @Transactional
    fun verifyNesa(principal: AuthenticatedUser, request: NesaVerificationRequest): ApplicantProfileResponse {
        val profile = applicantProfileRepository.findByUserId(principal.id)
            .orElseThrow { BusinessException("Verify NID before verifying NESA") }

        if (!profile.nidaVerified) {
            throw BusinessException("NID verification is required first")
        }

        val decryptedNationalId = fieldEncryptionService.decrypt(profile.nationalIdNumberEncrypted)
        if (decryptedNationalId != request.nationalIdNumber) {
            throw BusinessException("Provided national ID does not match the verified applicant profile")
        }

        val nesaRecord = nesaClient.fetchAcademicRecord(request.nationalIdNumber)
        profile.schoolName = nesaRecord.schoolName
        profile.grade = nesaRecord.grade
        profile.optionAttended = nesaRecord.optionAttended
        profile.completionYear = nesaRecord.completionYear
        profile.nesaVerified = true
        profile.nesaVerifiedAt = OffsetDateTime.now()
        profile.updatedAt = OffsetDateTime.now()

        applicantProfileRepository.save(profile)
        auditService.log(principal.id, "NESA_VERIFIED", "PROFILE", profile.id.toString())
        return applicantProfileMapper.toApplicantProfileResponse(profile)
    }

    @Transactional
    fun updateProfile(principal: AuthenticatedUser, request: ApplicantProfileUpsertRequest): ApplicantProfileResponse {
        val profile = applicantProfileRepository.findByUserId(principal.id)
            .orElseThrow { NotFoundException("Profile not found. Verify NID first.") }

        applicantProfileMapper.applyManualUpdate(profile, request)
        profile.updatedAt = OffsetDateTime.now()

        applicantProfileRepository.save(profile)
        auditService.log(principal.id, "PROFILE_UPDATED", "PROFILE", profile.id.toString())
        return applicantProfileMapper.toApplicantProfileResponse(profile)
    }

    @Transactional
    fun uploadCv(principal: AuthenticatedUser, file: MultipartFile): FileUploadResponse {
        val profile = applicantProfileRepository.findByUserId(principal.id)
            .orElseThrow { NotFoundException("Profile not found. Verify NID first.") }

        fileValidator.validateCv(file)
        val application = ensureDraftApplication(profile)
        val storageKey = documentStorageService.store(file)

        val document = documentRepository.save(
            DocumentEntity(
                application = application,
                type = DocumentType.CV,
                originalFilename = file.originalFilename ?: "cv",
                storageKey = storageKey,
                contentType = file.contentType ?: "application/octet-stream",
                fileSize = file.size,
                uploadedByUserId = principal.id,
            )
        )

        auditService.log(principal.id, "CV_UPLOADED", "DOCUMENT", document.id.toString())

        return FileUploadResponse(
            documentId = document.id,
            originalFilename = document.originalFilename,
            contentType = document.contentType,
            fileSize = document.fileSize,
        )
    }

    @Transactional
    fun submit(principal: AuthenticatedUser): ApplicationStatusResponse {
        val profile = applicantProfileRepository.findByUserId(principal.id)
            .orElseThrow { NotFoundException("Profile not found") }

        if (!profile.nidaVerified || !profile.nesaVerified) {
            throw BusinessException("NID and NESA verification must be completed before submission")
        }

        val application = ensureDraftApplication(profile)
        documentRepository.findTopByUploadedByUserIdAndTypeOrderByUploadedAtDesc(principal.id, DocumentType.CV)
            .orElseThrow { BusinessException("Upload a CV before submission") }
            .let {
                it.application = application
                documentRepository.save(it)
            }

        val previousStatus = application.status
        application.status = ApplicationStatus.SUBMITTED
        application.submittedAt = OffsetDateTime.now()
        application.updatedAt = OffsetDateTime.now()
        applicationRepository.save(application)

        recordStatusChange(application, previousStatus, ApplicationStatus.SUBMITTED, "Application submitted", principal.id)
        eventPublisherService.publishApplicationEventAfterCommit(
            ApplicationLifecycleEvent(
                applicationId = application.id,
                applicationNumber = application.applicationNumber,
                userId = principal.id,
                oldStatus = previousStatus,
                newStatus = ApplicationStatus.SUBMITTED,
                changedAt = OffsetDateTime.now(),
            )
        )
        auditService.log(principal.id, "APPLICATION_SUBMITTED", "APPLICATION", application.id.toString())

        return getStatus(principal)
    }

    @Transactional(readOnly = true)
    fun getStatus(principal: AuthenticatedUser): ApplicationStatusResponse {
        val application = applicationRepository.findByProfileUserId(principal.id)
            .orElseThrow { NotFoundException("Application not found") }
        val history = applicationStatusHistoryRepository.findAllByApplicationIdOrderByCreatedAtDesc(application.id)
            .map {
                ApplicationHistoryItemResponse(
                    status = it.newStatus,
                    reason = it.reason,
                    changedAt = it.createdAt,
                    changedByUserId = it.changedByUserId,
                )
            }

        return ApplicationStatusResponse(
            applicationId = application.id,
            applicationNumber = application.applicationNumber,
            status = application.status,
            rejectionReason = application.rejectionReason,
            decisionNote = application.decisionNote,
            submittedAt = application.submittedAt,
            reviewedAt = application.reviewedAt,
            history = history,
        )
    }

    private fun ensureDraftApplication(profile: ApplicantProfileEntity): ApplicationEntity =
        applicationRepository.findByProfileUserId(profile.user.id).orElseGet {
            val entity = applicationRepository.save(
                ApplicationEntity(
                    profile = profile,
                    applicationNumber = appNumberGenerator.next(),
                    status = ApplicationStatus.DRAFT,
                )
            )
            recordStatusChange(entity, null, ApplicationStatus.DRAFT, "Draft created", profile.user.id)
            entity
        }

    private fun recordStatusChange(
        application: ApplicationEntity,
        oldStatus: ApplicationStatus?,
        newStatus: ApplicationStatus,
        reason: String?,
        changedByUserId: java.util.UUID,
    ) {
        applicationStatusHistoryRepository.save(
            ApplicationStatusHistoryEntity(
                application = application,
                oldStatus = oldStatus,
                newStatus = newStatus,
                reason = reason,
                changedByUserId = changedByUserId,
            )
        )
    }
}
