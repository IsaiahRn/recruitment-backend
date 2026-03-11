package com.gmp.recruitment.integrations.nid

import com.gmp.recruitment.exceptions.BusinessException
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class MockNidClient : NidClient {
    private val firstNames = listOf("Aline", "Eric", "Jean", "Nadine", "Diane", "Patrick", "Oliver", "Ineza")
    private val lastNames = listOf("Uwase", "Hakizimana", "Ndayisaba", "Mugisha", "Irakoze", "Ishimwe", "Mukamana")
    private val provinces = listOf("Kigali", "Northern", "Southern", "Eastern", "Western")
    private val districts = listOf("Gasabo", "Kicukiro", "Nyarugenge", "Musanze", "Huye", "Rwamagana", "Rubavu")

    override fun fetchProfile(nationalIdNumber: String): NidProfile {
        if (!nationalIdNumber.matches(Regex("""\d{16}"""))) {
            throw BusinessException("National ID must be 16 digits")
        }

        val seed = nationalIdNumber.takeLast(6).toInt()
        val firstName = firstNames[seed % firstNames.size]
        val lastName = lastNames[seed % lastNames.size]
        val province = provinces[seed % provinces.size]
        val district = districts[seed % districts.size]
        val day = 1 + (seed % 28)
        val month = 1 + ((seed / 10) % 12)
        val year = 1990 + (seed % 12)
        val phone = "+25078${(1000000 + seed).toString().takeLast(7)}"

        return NidProfile(
            nationalIdNumber = nationalIdNumber,
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = LocalDate.of(year, month, day),
            gender = if (seed % 2 == 0) "F" else "M",
            phone = phone,
            addressLine = "$district sector",
            province = province,
            district = district,
        )
    }
}
