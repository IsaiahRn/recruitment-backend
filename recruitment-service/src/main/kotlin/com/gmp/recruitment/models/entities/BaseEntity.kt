package com.gmp.recruitment.models.entities

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.OffsetDateTime

@MappedSuperclass
abstract class BaseEntity(
    @Column(nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
