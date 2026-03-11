package com.gmp.recruitment.integrations.nesa

import com.gmp.recruitment.exceptions.BusinessException
import org.springframework.stereotype.Component

@Component
class MockNesaClient : NesaClient {
    private val grades = listOf("A", "B+", "B", "C+", "C")
    private val options = listOf("MPC", "MEG", "MCE", "PCB", "HEG", "MPG")
    private val schools = listOf("GS Kigali", "GS Remera", "College de Kigali", "GS Kacyiru", "GS Nyarutarama")

    override fun fetchAcademicRecord(nationalIdNumber: String): NesaAcademicRecord {
        if (!nationalIdNumber.matches(Regex("\\d{16}"))) {
            throw BusinessException("National ID must be 16 digits")
        }

        val seed = nationalIdNumber.takeLast(5).toInt()
        return NesaAcademicRecord(
            nationalIdNumber = nationalIdNumber,
            schoolName = schools[seed % schools.size],
            grade = grades[seed % grades.size],
            optionAttended = options[seed % options.size],
            completionYear = 2018 + (seed % 7),
        )
    }
}
