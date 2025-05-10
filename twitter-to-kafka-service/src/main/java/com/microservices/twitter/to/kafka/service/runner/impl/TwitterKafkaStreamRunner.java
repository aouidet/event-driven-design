package com.microservices.twitter.to.kafka.service.runner.impl;

import com.microservices.twitter.to.kafka.service.configuration.TwitterToKafkaServiceConfig;
import com.microservices.twitter.to.kafka.service.listner.TwitterKafkaStatusListener;
import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.Arrays;

/**
 * Component to simulate mock tweeter stream
 */
@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "false", matchIfMissing = true)
//@ConditionalOnExpression("${twitter-to-kafka-service.enable-mock-tweets} && not ${twitter-to-kafka-service.enable-v2-tweets}")
public class TwitterKafkaStreamRunner implements StreamRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterKafkaStreamRunner.class);
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;
    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;

    private TwitterStream twitterStream;

    public TwitterKafkaStreamRunner(TwitterKafkaStatusListener twitterKafkaStatusListener,
                                    TwitterToKafkaServiceConfig twitterToKafkaServiceConfig) {
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
        this.twitterToKafkaServiceConfig = twitterToKafkaServiceConfig;
    }

    @Override
    public void start() {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterKafkaStatusListener);
        addFilter();

    }

    @PreDestroy
    public void shutdown() {
        if (twitterStream != null) {
            LOG.info("Closing twitter stream");
            twitterStream.shutdown();
        }
    }

    private void addFilter() {
        String[] keyword = twitterToKafkaServiceConfig.getTwitterKeyword().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keyword);
        twitterStream.filter(filterQuery);
        LOG.info("Started filtring twitter stream for keyword {}", Arrays.toString(keyword));
    }
}
