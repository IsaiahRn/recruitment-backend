package com.gmp.recruitment.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "applicant_profiles")
class ApplicantProfileEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserEntity,

    @Column(name = "national_id_number_encrypted", nullable = false, length = 255)
    var nationalIdNumberEncrypted: String,

    @Column(name = "national_id_hash", nullable = false, unique = true, length = 64)
    var nationalIdHash: String,

    @Column(nullable = false, length = 80)
    var firstName: String,

    @Column(nullable = false, length = 80)
    var lastName: String,

    @Column(name = "date_of_birth_encrypted", length = 255)
    var dateOfBirthEncrypted: String? = null,

    @Column(name = "gender_encrypted", length = 255)
    var genderEncrypted: String? = null,

    @Column(name = "phone_encrypted", length = 255)
    var phoneEncrypted: String? = null,

    @Column(name = "address_line_encrypted", length = 255)
    var addressLineEncrypted: String? = null,

    @Column(length = 80)
    var province: String? = null,

    @Column(length = 80)
    var district: String? = null,

    @Column(length = 150)
    var schoolName: String? = null,

    @Column(length = 10)
    var grade: String? = null,

    @Column(length = 20)
    var optionAttended: String? = null,

    var completionYear: Int? = null,

    @Column(nullable = false)
    var nidaVerified: Boolean = false,

    var nidaVerifiedAt: OffsetDateTime? = null,

    @Column(nullable = false)
    var nesaVerified: Boolean = false,

    var nesaVerifiedAt: OffsetDateTime? = null,
) : BaseEntity()
