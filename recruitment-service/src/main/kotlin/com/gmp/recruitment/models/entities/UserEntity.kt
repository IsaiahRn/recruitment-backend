package com.gmp.recruitment.models.entities

import com.gmp.recruitment.models.enums.UserRole
import com.gmp.recruitment.models.enums.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 100)
    var username: String,

    @Column(nullable = false, unique = true, length = 150)
    var email: String,

    @Column(nullable = false, length = 150)
    var fullName: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var role: UserRole,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(nullable = false)
    var otpEnabled: Boolean = false,

    @Column(name = "totp_secret_encrypted", length = 255)
    var totpSecretEncrypted: String? = null,

    @Column(name = "totp_confirmed", nullable = false)
    var totpConfirmed: Boolean = false,

    var lastLoginAt: OffsetDateTime? = null,
) : BaseEntity()
