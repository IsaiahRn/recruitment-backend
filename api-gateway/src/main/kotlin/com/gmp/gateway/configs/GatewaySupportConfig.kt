package com.gmp.gateway.configs

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class GatewaySupportConfig {
    @Bean
    fun ipKeyResolver(): KeyResolver = KeyResolver { exchange ->
        Mono.justOrEmpty(
            exchange.request.headers.getFirst("X-Forwarded-For")
                ?.split(",")
                ?.firstOrNull()
                ?.trim()
                ?: exchange.request.remoteAddress?.address?.hostAddress
                ?: "anonymous"
        )
    }
}
