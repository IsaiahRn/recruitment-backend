package com.gmp.recruitment.repositories.projections

import java.time.Instant
import java.util.UUID

interface HrApplicantRowProjection {
  fun getApplicationId(): UUID
  fun getApplicationNumber(): String
  fun getFullName(): String
  fun getStatus(): String
  fun getSubmittedAt(): Instant
}
