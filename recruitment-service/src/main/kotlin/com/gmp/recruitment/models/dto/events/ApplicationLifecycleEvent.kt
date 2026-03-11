package com.gmp.recruitment.models.dto.events

import com.gmp.recruitment.models.enums.ApplicationStatus
import java.time.OffsetDateTime
import java.util.UUID

data class ApplicationLifecycleEvent(
    val applicationId: UUID,
    val applicationNumber: String,
    val userId: UUID,
    val oldStatus: ApplicationStatus?,
    val newStatus: ApplicationStatus,
    val changedAt: OffsetDateTime,
)
