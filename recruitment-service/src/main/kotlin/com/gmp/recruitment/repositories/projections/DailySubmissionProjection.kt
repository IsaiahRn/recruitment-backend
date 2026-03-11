package com.gmp.recruitment.repositories.projections

import java.time.LocalDate

interface DailySubmissionProjection {
    fun getDay(): LocalDate
    fun getCount(): Long
}
