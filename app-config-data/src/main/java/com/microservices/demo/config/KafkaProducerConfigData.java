package com.microservices.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "kafka-producer-config")
@Configuration
@Data
public class KafkaProducerConfigData {
    private String keySerializerClass;
    private String valueSerializeClass;
    private String compressionType;
    private String acks;
    private Integer batchSize;
    private Integer batchSizeBoostFactory;
    private Integer lignerMs;
    private Integer requestTimoutMs;
    private Integer retryCount;
}
