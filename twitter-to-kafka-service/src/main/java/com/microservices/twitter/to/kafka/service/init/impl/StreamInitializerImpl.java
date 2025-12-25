package com.microservices.twitter.to.kafka.service.init.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.twitter.to.kafka.service.init.StreamInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StreamInitializerImpl implements StreamInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(StreamInitializerImpl.class);
    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    public StreamInitializerImpl(KafkaConfigData kafkaConfigData,
                                 KafkaAdminClient kafkaAdminClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaAdminClient = kafkaAdminClient;
    }


    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        LOG.info("Topics with name {} is ready for operation!", kafkaConfigData.getTopicNamesToCreate().toArray());

    }
}
