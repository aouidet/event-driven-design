package com.microservices.demo.kafka.to.elastic.service.consumer;

import org.apache.avro.specific.SpecificRecordBase;

import java.io.Serializable;
import java.util.List;

public interface KafkaConsumer<K extends Serializable, V extends SpecificRecordBase> {
    void receive(List<V> message, List<Integer> keys, List<Integer> partition, List<Long> offsets);
}
