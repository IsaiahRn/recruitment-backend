package com.gmp.recruitment.exceptions

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(code = "NOT_FOUND", message = ex.message ?: "Not found"))

    @ExceptionHandler(ForbiddenOperationException::class, AccessDeniedException::class)
    fun handleForbidden(ex: Exception) =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError(code = "FORBIDDEN", message = ex.message ?: "Forbidden"))

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(ex: BusinessException) =
        ResponseEntity.badRequest().body(ApiError(code = "BUSINESS_ERROR", message = ex.message ?: "Request rejected"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val details = ex.bindingResult.allErrors.associate {
            val field = (it as? FieldError)?.field ?: it.objectName
            field to (it.defaultMessage ?: "Invalid value")
        }
        return ResponseEntity.badRequest().body(ApiError(code = "VALIDATION_ERROR", message = "Invalid request", details = details))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(ex: ConstraintViolationException) =
        ResponseEntity.badRequest().body(ApiError(code = "VALIDATION_ERROR", message = ex.message ?: "Invalid request"))

    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception) =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(code = "INTERNAL_SERVER_ERROR", message = ex.message ?: "Unexpected error"))
}
