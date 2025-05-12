package com.microservices.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
@Data
public class TwitterToKafkaServiceConfig {
    private List<String> twitterKeyword;
    private String welcomeMessage;
    private Boolean enableV2Tweets;
    private Boolean enableMockTweets;
    private Integer mockMinTweetLength;
    private Integer mockMaxTweetLength;
    private Long mockSleepMs;


    private String twitterV2BaseUrl;
    private String twitterV2RulesBaseUrl;
    private String twitterV2BearerToken;
}
