package com.gmp.recruitment.controllers

import com.gmp.recruitment.models.dto.hr.HrApplicantRowResponse
import com.gmp.recruitment.models.dto.hr.HrApplicationDetailResponse
import com.gmp.recruitment.models.dto.hr.ReviewDecisionRequest
import com.gmp.recruitment.services.ApplicationReviewService
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/hr")
class HrController(
    private val applicationReviewService: ApplicationReviewService,
) {
    @GetMapping("/applications")
    fun latestApplications(): List<HrApplicantRowResponse> =
        applicationReviewService.latestTenAlphabetically()

    @GetMapping("/applications/{applicationId}")
    fun detail(@PathVariable applicationId: UUID): HrApplicationDetailResponse =
        applicationReviewService.detail(applicationId)

    @GetMapping("/applications/{applicationId}/documents/{documentId}/download")
    fun downloadDocument(
        @AuthenticationPrincipal reviewer: AuthenticatedUser,
        @PathVariable applicationId: UUID,
        @PathVariable documentId: UUID,
    ): ResponseEntity<Resource> {
        val (document, resource) = applicationReviewService.downloadDocument(reviewer, applicationId, documentId)
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(document.originalFilename).build().toString()
            )
            .contentType(MediaType.parseMediaType(document.contentType))
            .contentLength(document.fileSize)
            .body(resource)
    }

    @PutMapping("/applications/{applicationId}/under-review")
    fun underReview(
        @AuthenticationPrincipal reviewer: AuthenticatedUser,
        @PathVariable applicationId: UUID,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ): HrApplicationDetailResponse = applicationReviewService.markUnderReview(reviewer, applicationId, request)

    @PutMapping("/applications/{applicationId}/approve")
    fun approve(
        @AuthenticationPrincipal reviewer: AuthenticatedUser,
        @PathVariable applicationId: UUID,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ): HrApplicationDetailResponse = applicationReviewService.approve(reviewer, applicationId, request)

    @PutMapping("/applications/{applicationId}/reject")
    fun reject(
        @AuthenticationPrincipal reviewer: AuthenticatedUser,
        @PathVariable applicationId: UUID,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ): HrApplicationDetailResponse = applicationReviewService.reject(reviewer, applicationId, request)
}
