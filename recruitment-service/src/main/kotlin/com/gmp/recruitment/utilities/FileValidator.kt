package com.gmp.recruitment.utilities

import com.gmp.recruitment.exceptions.BusinessException
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class FileValidator {
    private val allowedContentTypes = setOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )

    fun validateCv(file: MultipartFile) {
        if (file.isEmpty) {
            throw BusinessException("Uploaded file is empty")
        }
        if ((file.contentType ?: "") !in allowedContentTypes) {
            throw BusinessException("Only PDF, DOC or DOCX files are allowed")
        }
        if (file.size > 5 * 1024 * 1024) {
            throw BusinessException("File size must not exceed 5MB")
        }
    }
}
