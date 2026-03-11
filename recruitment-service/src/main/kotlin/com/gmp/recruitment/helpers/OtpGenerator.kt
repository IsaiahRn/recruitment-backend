package com.gmp.recruitment.helpers

import kotlin.random.Random
import org.springframework.stereotype.Component

@Component
class OtpGenerator {
    fun generate(length: Int = 6): String = (1..length)
        .map { Random.nextInt(0, 10) }
        .joinToString("")
}
