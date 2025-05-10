//package com.microservices.twitter.to.kafka.service.runner.impl;
//
//import com.microservices.twitter.to.kafka.service.configuration.TwitterToKafkaServiceConfig;
//import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//@ConditionalOnExpression("${twitter-to-kafka-service.enable-v2-tweets} && not ${twitter-to-kafka-service.enable-mock-tweets}")
//public class TwitterV2KafkaStreamRunner implements StreamRunner {
//
//    private static final Logger LOG = LoggerFactory.getLogger(TwitterV2KafkaStreamRunner.class);
//
//    private final TwitterV2StreamHelper twitterV2StreamHelper;
//    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;
//
//    public TwitterV2KafkaStreamRunner(TwitterV2StreamHelper twitterV2StreamHelper,
//                                      TwitterToKafkaServiceConfig twitterToKafkaServiceConfig) {
//        this.twitterV2StreamHelper = twitterV2StreamHelper;
//        this.twitterToKafkaServiceConfig = twitterToKafkaServiceConfig;
//    }
//
//
//    @Override
//    public void start() {
//
//        String bearerToken = twitterToKafkaServiceConfig.getTwitterV2BearerToken();
//        if (null != bearerToken) {
//            try {
//                twitterV2StreamHelper.setupRules(bearerToken, getRules());
//                twitterV2StreamHelper.connectStream(bearerToken);
//            } catch (IOException | URISyntaxException e) {
//                LOG.error("Error streaming tweets!", e);
//                throw new RuntimeException("Error streaming tweets", e);
//            }
//        } else {
//            LOG.error("""
//                    There was a problem getting your bearer token.
//                    Please make sure that you set the TWITTER_BEARER_TOKEN environment variable
//                    """);
//            throw new RuntimeException("""
//                    There was a problem getting your bearer token.
//                    Please make sure that you set the TWITTER_BEARER_TOKEN environment variable
//                    """);
//        }
//
//    }
//
//    private Map<String, String> getRules() {
//        List<String> keyWords = twitterToKafkaServiceConfig.getTwitterKeyword();
//        Map<String, String> rules = new HashMap<>();
//        for (String keyWord: keyWords) {
//            rules.put(keyWord, "keyword" + keyWord);
//            LOG.info("Create filter for keywords : {}", rules);
//        }
//
//        return rules;
//    }
//}
