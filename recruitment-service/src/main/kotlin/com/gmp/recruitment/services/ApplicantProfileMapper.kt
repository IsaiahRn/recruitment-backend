package com.gmp.recruitment.services

import com.gmp.recruitment.integrations.nid.NidProfile
import com.gmp.recruitment.models.dto.applicant.ApplicantProfileResponse
import com.gmp.recruitment.models.dto.applicant.ApplicantProfileUpsertRequest
import com.gmp.recruitment.models.dto.hr.HrApplicantProfileResponse
import com.gmp.recruitment.models.entities.ApplicantProfileEntity
import com.gmp.recruitment.services.security.FieldEncryptionService
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class ApplicantProfileMapper(
    private val fieldEncryptionService: FieldEncryptionService,
) {
    fun applyVerifiedNid(profile: ApplicantProfileEntity, nidProfile: NidProfile) {
        profile.nationalIdNumberEncrypted = fieldEncryptionService.encrypt(nidProfile.nationalIdNumber)!!
        profile.nationalIdHash = fieldEncryptionService.fingerprint(nidProfile.nationalIdNumber)
        profile.firstName = nidProfile.firstName
        profile.lastName = nidProfile.lastName
        profile.dateOfBirthEncrypted = encryptDate(nidProfile.dateOfBirth)
        profile.genderEncrypted = fieldEncryptionService.encrypt(nidProfile.gender)
        profile.phoneEncrypted = fieldEncryptionService.encrypt(nidProfile.phone)
        profile.addressLineEncrypted = fieldEncryptionService.encrypt(nidProfile.addressLine)
        profile.province = nidProfile.province
        profile.district = nidProfile.district
    }

    fun applyManualUpdate(profile: ApplicantProfileEntity, request: ApplicantProfileUpsertRequest) {
        profile.phoneEncrypted = fieldEncryptionService.encrypt(request.phone)
        if (request.addressLine != null) {
            profile.addressLineEncrypted = fieldEncryptionService.encrypt(request.addressLine)
        }
        if (request.province != null) {
            profile.province = request.province
        }
        if (request.district != null) {
            profile.district = request.district
        }
    }

    fun toApplicantProfileResponse(profile: ApplicantProfileEntity): ApplicantProfileResponse =
        ApplicantProfileResponse(
            userId = profile.user.id,
            nationalIdNumber = fieldEncryptionService.decrypt(profile.nationalIdNumberEncrypted) ?: "",
            firstName = profile.firstName,
            lastName = profile.lastName,
            dateOfBirth = decryptDate(profile.dateOfBirthEncrypted),
            gender = fieldEncryptionService.decrypt(profile.genderEncrypted),
            phone = fieldEncryptionService.decrypt(profile.phoneEncrypted),
            addressLine = fieldEncryptionService.decrypt(profile.addressLineEncrypted),
            province = profile.province,
            district = profile.district,
            schoolName = profile.schoolName,
            grade = profile.grade,
            optionAttended = profile.optionAttended,
            completionYear = profile.completionYear,
            nidaVerified = profile.nidaVerified,
            nesaVerified = profile.nesaVerified,
        )

    fun toHrApplicantProfileResponse(profile: ApplicantProfileEntity): HrApplicantProfileResponse =
        HrApplicantProfileResponse(
            profileId = profile.id,
            firstName = profile.firstName,
            lastName = profile.lastName,
            nationalIdNumber = fieldEncryptionService.decrypt(profile.nationalIdNumberEncrypted) ?: "",
            dateOfBirth = decryptDate(profile.dateOfBirthEncrypted),
            gender = fieldEncryptionService.decrypt(profile.genderEncrypted),
            phone = fieldEncryptionService.decrypt(profile.phoneEncrypted),
            addressLine = fieldEncryptionService.decrypt(profile.addressLineEncrypted),
            province = profile.province,
            district = profile.district,
            schoolName = profile.schoolName,
            grade = profile.grade,
            optionAttended = profile.optionAttended,
            completionYear = profile.completionYear,
        )

    private fun encryptDate(value: LocalDate?): String? = value?.let { fieldEncryptionService.encrypt(it.toString()) }
    private fun decryptDate(value: String?): LocalDate? = fieldEncryptionService.decrypt(value)?.let(LocalDate::parse)
}
