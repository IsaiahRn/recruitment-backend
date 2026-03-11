package com.gmp.recruitment.configs

import java.util.concurrent.Executor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class AsyncConfig {
    @Bean("eventPublisherExecutor")
    fun eventPublisherExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 4
            queueCapacity = 500
            setThreadNamePrefix("event-publisher-")
            setWaitForTasksToCompleteOnShutdown(false)
            initialize()
        }
}
