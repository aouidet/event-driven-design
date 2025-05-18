package com.microservices.demo.kafka.producer.service;

import com.microservices.kafka.avro.model.TwitterAvroModel;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public class KafkaProducerImpl implements KafkaProducer<Long, TwitterAvroModel> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, Long key, TwitterAvroModel message) {
        LOG.info("Sending message='{}' to topic='{}'", message, topicName);
        // java 8 class that represent async thread (java.util.concurrent)
        CompletableFuture<SendResult<Long, TwitterAvroModel>> future =
                kafkaTemplate.send(topicName, key, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                handleSuccess(result);
            } else {
                handleFailure(topicName, message, ex);
            }
        });
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            LOG.info("Closing kafka producer!");
            kafkaTemplate.destroy();
        }
    }

    private void handleSuccess(SendResult<Long, TwitterAvroModel> result) {
        RecordMetadata metadata = result.getRecordMetadata();
        LOG.debug("Received new metadata. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                metadata.timestamp(),
                System.nanoTime());
    }

    private void handleFailure(String topicName, TwitterAvroModel message,
                               Throwable ex) {
        LOG.error("Error while sending message={} to topic={}", message, topicName, ex);
    }
}
