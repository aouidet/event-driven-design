package com.microservices.twitter.to.kafka.service.listner;

import com.microservices.demo.common.config.KafkaConfigData;
import com.microservices.demo.kafka.producer.service.KafkaProducer;
import com.microservices.kafka.avro.model.TwitterAvroModel;
import com.microservices.twitter.to.kafka.service.TwitterToKafkaApplication;
import com.microservices.twitter.to.kafka.service.transformer.TwitterStatusToAvroTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusAdapter;

@Component
public class TwitterKafkaStatusListener extends StatusAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaApplication.class);

    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
    private final TwitterStatusToAvroTransformer twitterStatusToAvroTransformer;

    public TwitterKafkaStatusListener(KafkaConfigData kafkaConfigData, KafkaProducer<Long, TwitterAvroModel> kafkaProducer, TwitterStatusToAvroTransformer twitterStatusToAvroTransformer) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducer = kafkaProducer;
        this.twitterStatusToAvroTransformer = twitterStatusToAvroTransformer;
    }

    @Override
    public void onStatus(Status status) {
       LOG.info("TRecieved status text {} sending to kafka topic {}", status.getText(), kafkaConfigData.getTopicName());
       TwitterAvroModel twitterAvroModel = twitterStatusToAvroTransformer.getTwitterAvroModelTransformerFromStatus(status);
       kafkaProducer.send(kafkaConfigData.getTopicName(), twitterAvroModel.getUserId(), twitterAvroModel);
    }
}
