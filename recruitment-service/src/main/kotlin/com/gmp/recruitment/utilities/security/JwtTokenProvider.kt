package com.gmp.recruitment.utilities.security

import com.gmp.recruitment.models.enums.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.OffsetDateTime
import java.util.Date
import javax.crypto.SecretKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtTokenProvider(
    @Value("\${app.security.jwt.secret}") secret: String,
    @Value("\${app.security.jwt.access-ttl-minutes}") private val accessTtlMinutes: Long,
    @Value("\${app.security.jwt.refresh-ttl-days}") private val refreshTtlDays: Long,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(user: AuthenticatedUser): Pair<String, OffsetDateTime> {
        val expiry = OffsetDateTime.now().plusMinutes(accessTtlMinutes)
        return buildToken(user, expiry, "access") to expiry
    }

    fun generateRefreshToken(user: AuthenticatedUser): Pair<String, OffsetDateTime> {
        val expiry = OffsetDateTime.now().plusDays(refreshTtlDays)
        return buildToken(user, expiry, "refresh") to expiry
    }

    fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).payload

    fun extractUsername(token: String): String = parseClaims(token).subject

    fun extractUserId(token: String): String = parseClaims(token)["userId"].toString()

    fun extractRole(token: String): UserRole = UserRole.valueOf(parseClaims(token)["role"].toString())

    fun isRefreshToken(token: String): Boolean = parseClaims(token)["tokenType"] == "refresh"

    private fun buildToken(user: AuthenticatedUser, expiry: OffsetDateTime, tokenType: String): String =
        Jwts.builder()
            .subject(user.username)
            .claim("userId", user.id.toString())
            .claim("role", user.role.name)
            .claim("fullName", user.fullName)
            .claim("tokenType", tokenType)
            .issuedAt(Date())
            .expiration(Date.from(expiry.toInstant()))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
}
