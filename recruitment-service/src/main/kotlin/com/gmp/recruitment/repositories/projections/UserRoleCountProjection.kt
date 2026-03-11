package com.gmp.recruitment.repositories.projections

interface UserRoleCountProjection {
    fun getRole(): String
    fun getCount(): Long
}
