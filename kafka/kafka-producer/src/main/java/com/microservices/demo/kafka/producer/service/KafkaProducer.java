package com.microservices.demo.kafka.producer.service;

import org.apache.avro.specific.SpecificRecordBase;

import java.io.Serializable;

public interface KafkaProducer <k extends Serializable, v extends SpecificRecordBase> {
    void send(String topicName, k key, v message);
}
