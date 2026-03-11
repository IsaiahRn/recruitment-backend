package com.gmp.recruitment.models.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_logs")
class AuditLogEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    var actorUserId: UUID? = null,

    @Column(nullable = false, length = 80)
    var action: String,

    @Column(nullable = false, length = 80)
    var entityType: String,

    @Column(nullable = false, length = 80)
    var entityId: String,

    @Column(columnDefinition = "TEXT")
    var metadataJson: String? = null,

    @Column(length = 80)
    var ipAddress: String? = null,

    @Column(length = 255)
    var userAgent: String? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
)
