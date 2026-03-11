package com.gmp.gateway.configs.security

import java.nio.charset.StandardCharsets
import java.time.Duration
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity
class SecurityConfig(
    @Value("\${app.security.jwt-secret}") private val jwtSecret: String,
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/v1/auth/**"
                    ).permitAll()
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth -> oauth.jwt { } }
            .exceptionHandling {
                it.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.setComplete()
                }
            }
            .headers { headers ->
                headers.contentSecurityPolicy { csp -> csp.policyDirectives("default-src 'self'") }
                headers.frameOptions { frame -> frame.mode(Mode.DENY) }
                headers.hsts { hsts ->
                    hsts.includeSubdomains(true)
                    hsts.maxAge(Duration.ofDays(365))
                }
            }
            .build()

  @Bean
  fun jwtDecoder(): ReactiveJwtDecoder {
    val key = SecretKeySpec(
      jwtSecret.toByteArray(StandardCharsets.UTF_8),
      "HmacSHA256"
    )

    return NimbusReactiveJwtDecoder
      .withSecretKey(key)
      .macAlgorithm(MacAlgorithm.HS256)
      .build()
  }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost", "http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("X-Request-Id")
            allowCredentials = false
            maxAge = 3600
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
