package com.gmp.recruitment.models.entities

import com.gmp.recruitment.models.enums.ApplicationStatus
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "application_status_history")
class ApplicationStatusHistoryEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: ApplicationEntity,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    var oldStatus: ApplicationStatus? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var newStatus: ApplicationStatus,

    @Column(length = 500)
    var reason: String? = null,

    @Column(nullable = false)
    var changedByUserId: UUID,

    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
)
