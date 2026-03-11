package com.gmp.recruitment.configs.streaming

import com.gmp.recruitment.models.dto.events.ApplicationLifecycleEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConsumerConfig(
  private val kafkaProperties: KafkaProperties,
  private val sslBundles: SslBundles,
) {
  @Bean
  fun applicationEventKafkaListenerContainerFactory():
    ConcurrentKafkaListenerContainerFactory<String, ApplicationLifecycleEvent> {
    val config = kafkaProperties.buildConsumerProperties(sslBundles).toMutableMap()
    config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
    config[JsonDeserializer.TRUSTED_PACKAGES] = "*"
    config[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
    config[JsonDeserializer.VALUE_DEFAULT_TYPE] =
      ApplicationLifecycleEvent::class.java.name

    val factory = ConcurrentKafkaListenerContainerFactory<String, ApplicationLifecycleEvent>()
    factory.consumerFactory = DefaultKafkaConsumerFactory(config)
    return factory
  }
}
