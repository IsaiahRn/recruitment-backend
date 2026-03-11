package com.gmp.gateway.filters

import java.util.UUID
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CorrelationIdFilter : GlobalFilter, Ordered {

    companion object {
        const val HEADER_NAME = "X-Request-Id"
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val correlationId = exchange.request.headers.getFirst(HEADER_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        val mutatedRequest = exchange.request
            .mutate()
            .header(HEADER_NAME, correlationId)
            .build()

        val mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build()

        mutatedExchange.response.beforeCommit {
            Mono.fromRunnable {
                mutatedExchange.response.headers.set(HEADER_NAME, correlationId)
            }
        }

        return chain.filter(mutatedExchange)
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
