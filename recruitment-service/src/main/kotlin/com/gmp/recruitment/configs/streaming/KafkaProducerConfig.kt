package com.gmp.recruitment.configs.streaming

import com.gmp.recruitment.models.dto.events.ApplicationLifecycleEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaProducerConfig(
  private val kafkaProperties: KafkaProperties,
  private val sslBundles: SslBundles,
) {
  @Bean
  fun applicationEventKafkaTemplate(): KafkaTemplate<String, ApplicationLifecycleEvent> {
    val config = kafkaProperties.buildProducerProperties(sslBundles).toMutableMap()
    config[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    config[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
    config[ProducerConfig.ACKS_CONFIG] = "1"
    config[ProducerConfig.LINGER_MS_CONFIG] = 0
    config[ProducerConfig.MAX_BLOCK_MS_CONFIG] = 1000
    config[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 1000
    config[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 3000
    return KafkaTemplate(DefaultKafkaProducerFactory(config))
  }
}
