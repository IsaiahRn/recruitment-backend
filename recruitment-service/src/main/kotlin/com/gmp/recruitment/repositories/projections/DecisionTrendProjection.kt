package com.gmp.recruitment.repositories.projections

import java.time.LocalDate

interface DecisionTrendProjection {
    fun getDay(): LocalDate
    fun getApproved(): Long
    fun getRejected(): Long
}
