package com.gmp.recruitment.services

import com.gmp.recruitment.models.dto.events.ApplicationLifecycleEvent
import java.util.concurrent.Executor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class EventPublisherService(
    @Value("\${app.kafka.enabled:true}") private val kafkaEnabled: Boolean,
    @Value("\${app.kafka.topics.application-events}") private val applicationTopic: String,
    private val kafkaTemplateProvider: ObjectProvider<KafkaTemplate<String, ApplicationLifecycleEvent>>,
    private val eventPublisherExecutor: Executor,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publishApplicationEventAfterCommit(event: ApplicationLifecycleEvent) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    publishAsync(event)
                }
            })
        } else {
            publishAsync(event)
        }
    }

    fun publishAsync(event: ApplicationLifecycleEvent) {
        eventPublisherExecutor.execute {
            if (!kafkaEnabled) {
                log.info("Kafka publishing skipped for {} because app.kafka.enabled=false", event.applicationNumber)
                return@execute
            }

            val kafkaTemplate = kafkaTemplateProvider.ifAvailable
            if (kafkaTemplate == null) {
                log.warn("Kafka template unavailable; skipping publish for {}", event.applicationNumber)
                return@execute
            }

            runCatching {
                kafkaTemplate.send(applicationTopic, event.applicationId.toString(), event)
                    .whenComplete { _, throwable ->
                        if (throwable != null) {
                            log.warn("Failed to publish application lifecycle event for {}", event.applicationNumber, throwable)
                        } else {
                            log.info("Published application lifecycle event for {}", event.applicationNumber)
                        }
                    }
            }.onFailure {
                log.warn("Kafka publish dispatch failed for {}", event.applicationNumber, it)
            }
        }
    }
}
