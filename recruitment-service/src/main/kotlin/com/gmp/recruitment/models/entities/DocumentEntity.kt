package com.gmp.recruitment.models.entities

import com.gmp.recruitment.models.enums.DocumentType
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "documents")
class DocumentEntity(
  @Id
    val id: UUID = UUID.randomUUID(),

  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    var application: ApplicationEntity? = null,

  @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: DocumentType,

  @Column(nullable = false, length = 255)
    var originalFilename: String,

  @Column(nullable = false, unique = true, length = 255)
    var storageKey: String,

  @Column(nullable = false, length = 100)
    var contentType: String,

  @Column(nullable = false)
    var fileSize: Long,

  @Column(nullable = false)
    var uploadedAt: Instant = Instant.now(),

  @Column(nullable = false)
    var uploadedByUserId: UUID,
)
