package com.gmp.recruitment.controllers

import com.gmp.recruitment.models.dto.admin.AuditLogResponse
import com.gmp.recruitment.models.dto.admin.UserResponse
import com.gmp.recruitment.models.dto.admin.UserUpsertRequest
import com.gmp.recruitment.services.AdminService
import com.gmp.recruitment.utilities.security.AuthenticatedUser
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService,
) {
    @GetMapping("/users")
    fun listUsers(): List<UserResponse> = adminService.listUsers()

    @GetMapping("/audit-logs")
    fun latestAuditLogs(): List<AuditLogResponse> = adminService.latestAuditLogs()

    @PostMapping("/users")
    fun createUser(
        @AuthenticationPrincipal actor: AuthenticatedUser,
        @Valid @RequestBody request: UserUpsertRequest,
    ): UserResponse = adminService.createUser(actor.id, request)

    @PutMapping("/users/{userId}")
    fun updateUser(
        @AuthenticationPrincipal actor: AuthenticatedUser,
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UserUpsertRequest,
    ): UserResponse = adminService.updateUser(actor.id, userId, request)

    @PutMapping("/users/{userId}/disable")
    fun disableUser(
        @AuthenticationPrincipal actor: AuthenticatedUser,
        @PathVariable userId: UUID,
    ): UserResponse = adminService.disableUser(actor.id, userId)

    @PutMapping("/users/{userId}/enable")
    fun enableUser(
        @AuthenticationPrincipal actor: AuthenticatedUser,
        @PathVariable userId: UUID,
    ): UserResponse = adminService.enableUser(actor.id, userId)

    @PutMapping("/users/{userId}/reset-authenticator")
    fun resetAuthenticator(
        @AuthenticationPrincipal actor: AuthenticatedUser,
        @PathVariable userId: UUID,
    ): UserResponse = adminService.resetAuthenticator(actor.id, userId)
}
