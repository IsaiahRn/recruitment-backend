package com.gmp.recruitment.models.entities

import com.gmp.recruitment.models.enums.ApplicationStatus
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "applications")
class ApplicationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    var profile: ApplicantProfileEntity,

    @Column(nullable = false, unique = true, length = 40)
    var applicationNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ApplicationStatus = ApplicationStatus.DRAFT,

    var submittedAt: OffsetDateTime? = null,

    var reviewedAt: OffsetDateTime? = null,

    var reviewedByUserId: UUID? = null,

    @Column(length = 500)
    var rejectionReason: String? = null,

    @Column(length = 500)
    var decisionNote: String? = null,
) : BaseEntity()
