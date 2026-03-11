package com.gmp.recruitment.helpers

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class AppNumberGenerator {
    fun next(): String = "APP-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" +
        UUID.randomUUID().toString().substring(0, 6).uppercase()
}
