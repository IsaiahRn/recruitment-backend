package com.gmp.recruitment.utilities

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class RequestContextUtils {
    fun clientIp(request: HttpServletRequest): String? =
        request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr

    fun userAgent(request: HttpServletRequest): String? = request.getHeader("User-Agent")
}
