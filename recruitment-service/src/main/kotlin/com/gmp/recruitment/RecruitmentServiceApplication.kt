package com.gmp.recruitment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableCaching
@SpringBootApplication
class RecruitmentServiceApplication

fun main(args: Array<String>) {
    runApplication<RecruitmentServiceApplication>(*args)
}
