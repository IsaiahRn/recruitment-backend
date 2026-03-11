package com.gmp.recruitment.repositories.projections

interface StatusCountProjection {
    fun getStatus(): String
    fun getCount(): Long
}
