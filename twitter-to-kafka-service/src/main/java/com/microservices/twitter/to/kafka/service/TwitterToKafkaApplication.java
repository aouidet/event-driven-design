package com.microservices.twitter.to.kafka.service;

import com.microservices.twitter.to.kafka.service.configuration.TwitterToKafkaServiceConfig;
import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
public class TwitterToKafkaApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaApplication.class);

    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;
    private final StreamRunner streamRunner;

    public TwitterToKafkaApplication(TwitterToKafkaServiceConfig configData, StreamRunner streamRunner) {
        this.twitterToKafkaServiceConfig = configData;
        this.streamRunner = streamRunner;
    }

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("App Starting");
        LOG.info(Arrays.toString(twitterToKafkaServiceConfig.getTwitterKeyword().toArray(new String[] {})));
        LOG.info(twitterToKafkaServiceConfig.getWelcomeMessage());
        streamRunner.start();
    }
}
