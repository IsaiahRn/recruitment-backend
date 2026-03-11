package com.gmp.recruitment.exceptions

open class ApiException(message: String) : RuntimeException(message)

class BusinessException(message: String) : ApiException(message)

class NotFoundException(message: String) : ApiException(message)

class ForbiddenOperationException(message: String) : ApiException(message)
