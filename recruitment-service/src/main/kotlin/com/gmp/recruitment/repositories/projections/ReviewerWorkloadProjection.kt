package com.gmp.recruitment.repositories.projections

interface ReviewerWorkloadProjection {
    fun getReviewerName(): String
    fun getReviewedCount(): Long
}
