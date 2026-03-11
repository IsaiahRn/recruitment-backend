package com.gmp.recruitment.services.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.gmp.recruitment.models.dto.internal.OtpChallengePayload
import java.time.Duration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

interface OtpStore {
    fun save(challengeId: String, payload: OtpChallengePayload, ttl: Duration)
    fun get(challengeId: String): OtpChallengePayload?
    fun delete(challengeId: String)
}

@Component
class RedisOtpStore(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : OtpStore {
    override fun save(challengeId: String, payload: OtpChallengePayload, ttl: Duration) {
        redisTemplate.opsForValue().set(key(challengeId), objectMapper.writeValueAsString(payload), ttl)
    }

    override fun get(challengeId: String): OtpChallengePayload? =
        redisTemplate.opsForValue().get(key(challengeId))?.let { objectMapper.readValue(it, OtpChallengePayload::class.java) }

    override fun delete(challengeId: String) {
        redisTemplate.delete(key(challengeId))
    }

    private fun key(challengeId: String) = "otp:$challengeId"
}
