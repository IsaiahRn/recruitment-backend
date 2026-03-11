package com.gmp.recruitment.configs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

@Configuration
class RedisConfig {
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(connectionFactory)

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager =
        RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(30)))
            .build()
}
