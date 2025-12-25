package com.microservices.demo.kafka.to.elastic.service.consumer.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.demo.kafka.to.elastic.service.consumer.KafkaConsumer;
import com.microservices.kafka.avro.model.TwitterAvroModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TwitterKafkaConsumer implements KafkaConsumer<Long, TwitterAvroModel> {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaConsumer.class);

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final KafkaAdminClient kafkaAdminClient;
    private final KafkaConfigData  kafkaConfigData;

    public TwitterKafkaConsumer(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, KafkaAdminClient kafkaAdminClient, KafkaConfigData kafkaConfigData) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.kafkaAdminClient = kafkaAdminClient;
        this.kafkaConfigData = kafkaConfigData;
    }

    @Override
    @KafkaListener(id = "twitterTopicListner", topics = "${kafka-config.topic-name}")
    public void receive(@Payload List<TwitterAvroModel> message,
                        @Header List<Integer> keys,
                        @Header List<Integer> partitions,
                        @Header List<Long> offsets) {

        LOG.info("{} number of message received with keys {}, partition {} and offsets {}, sending it to elastic: Thread id {}",
                message.size(), keys.toString(), partitions.toString(), offsets.toString(), Thread.currentThread().threadId());

    }
}
