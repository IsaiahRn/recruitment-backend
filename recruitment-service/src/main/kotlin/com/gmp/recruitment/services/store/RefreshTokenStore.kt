package com.gmp.recruitment.services.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.gmp.recruitment.models.dto.internal.RefreshTokenPayload
import java.time.Duration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

interface RefreshTokenStore {
    fun save(token: String, payload: RefreshTokenPayload, ttl: Duration)
    fun get(token: String): RefreshTokenPayload?
    fun delete(token: String)
}

@Component
class RedisRefreshTokenStore(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : RefreshTokenStore {
    override fun save(token: String, payload: RefreshTokenPayload, ttl: Duration) {
        redisTemplate.opsForValue().set(key(token), objectMapper.writeValueAsString(payload), ttl)
    }

    override fun get(token: String): RefreshTokenPayload? =
        redisTemplate.opsForValue().get(key(token))?.let { objectMapper.readValue(it, RefreshTokenPayload::class.java) }

    override fun delete(token: String) {
        redisTemplate.delete(key(token))
    }

    private fun key(token: String) = "refresh:$token"
}
