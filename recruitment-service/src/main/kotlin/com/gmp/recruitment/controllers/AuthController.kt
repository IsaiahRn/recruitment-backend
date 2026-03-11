package com.gmp.recruitment.controllers

import com.gmp.recruitment.models.dto.auth.*
import com.gmp.recruitment.services.AuthService
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register/applicant")
    fun registerApplicant(@Valid @RequestBody request: ApplicantRegistrationRequest): ApplicantRegistrationResponse =
        authService.registerApplicant(request)

    @PostMapping("/login/password")
    fun loginWithPassword(@Valid @RequestBody request: PasswordLoginRequest): TokenResponse =
        authService.login(request)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): TokenResponse =
        authService.refresh(request)

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshTokenRequest) = authService.logout(request)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: AuthenticatedUser): AuthenticatedUserResponse =
        authService.me(principal)
}
