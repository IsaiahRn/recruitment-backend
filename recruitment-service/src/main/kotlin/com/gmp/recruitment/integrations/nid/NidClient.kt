package com.gmp.recruitment.integrations.nid

import java.time.LocalDate

interface NidClient {
    fun fetchProfile(nationalIdNumber: String): NidProfile
}

data class NidProfile(
    val nationalIdNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gender: String,
    val phone: String,
    val addressLine: String,
    val province: String,
    val district: String,
)
