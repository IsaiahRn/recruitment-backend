package com.gmp.recruitment.controllers

import com.gmp.recruitment.models.dto.applicant.*
import com.gmp.recruitment.services.ApplicantService
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/applicant")
class ApplicantController(
    private val applicantService: ApplicantService,
) {
    @GetMapping("/profile")
    fun profile(@AuthenticationPrincipal principal: AuthenticatedUser): ApplicantProfileResponse? =
        applicantService.getMyProfile(principal)

    @PutMapping("/profile")
    fun updateProfile(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @Valid @RequestBody request: ApplicantProfileUpsertRequest,
    ): ApplicantProfileResponse = applicantService.updateProfile(principal, request)

    @PostMapping("/verify/nid")
    fun verifyNid(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @Valid @RequestBody request: NidVerificationRequest,
    ): ApplicantProfileResponse = applicantService.verifyNid(principal, request)

    @PostMapping("/verify/nesa")
    fun verifyNesa(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @Valid @RequestBody request: NesaVerificationRequest,
    ): ApplicantProfileResponse = applicantService.verifyNesa(principal, request)

    @PostMapping("/cv", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadCv(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestPart("file") file: MultipartFile,
    ): FileUploadResponse = applicantService.uploadCv(principal, file)

    @PostMapping("/submit")
    fun submit(@AuthenticationPrincipal principal: AuthenticatedUser): ApplicationStatusResponse =
        applicantService.submit(principal)

    @GetMapping("/status")
    fun status(@AuthenticationPrincipal principal: AuthenticatedUser): ApplicationStatusResponse =
        applicantService.getStatus(principal)
}
