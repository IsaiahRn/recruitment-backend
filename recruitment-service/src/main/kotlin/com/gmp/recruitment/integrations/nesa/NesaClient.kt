package com.gmp.recruitment.integrations.nesa

interface NesaClient {
    fun fetchAcademicRecord(nationalIdNumber: String): NesaAcademicRecord
}

data class NesaAcademicRecord(
    val nationalIdNumber: String,
    val schoolName: String,
    val grade: String,
    val optionAttended: String,
    val completionYear: Int,
)
