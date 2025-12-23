package com.microservices.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.config.TwitterToKafkaServiceConfig;
import com.microservices.twitter.to.kafka.service.exception.TwitterToKafkaException;
import com.microservices.twitter.to.kafka.service.listner.TwitterKafkaStatusListener;
import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "true") // havingValue = true for not implementation StreamRunner
public class MockKafkaStreamRunner implements StreamRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MockKafkaStreamRunner.class);
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;
    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;

    private static final Random RANDOM = new Random();
    private static final String[] WORDS = {
            "#Kafka",
            "#SpringBoot",
            "#Microservices",
            "#EventDriven",
            "#CloudNative",
            "#Docker",
            "#Kubernetes",
            "#Java",
            "#DevOps",
            "#Streaming",
            "#BigData",
            "#GCP",
            "#AWS",
            "#Observability",
            "#Scalability",
            "#Resilience",
            "Real-time",
            "Data",
            "Processing",
            "Architecture",
            "System",
            "Design"
    };


    private static final String tweetAsRawJson = "{" +
            "\"created_at\":\"{0}\", " +
            "\"id\":\"{1}\", " +
            "\"text\":\"{2}\", " +
            "\"user\":{\"id\":\"{3}\"}" +
            "}";

    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZ yyyy";

    public MockKafkaStreamRunner(TwitterKafkaStatusListener twitterKafkaStatusListener, TwitterToKafkaServiceConfig twitterToKafkaServiceConfig) {
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
        this.twitterToKafkaServiceConfig = twitterToKafkaServiceConfig;
    }

    @Override
    public void start() throws TwitterToKafkaException, TwitterException {
        String [] keywords = twitterToKafkaServiceConfig.getTwitterKeyword().toArray(new String[0]);
        int minTweetLength = twitterToKafkaServiceConfig.getMockMinTweetLength();
        int maxTweetLength = twitterToKafkaServiceConfig.getMockMaxTweetLength();
        long sleepTimesMs = twitterToKafkaServiceConfig.getMockSleepMs();
        LOG.info("Starting mock filtering twitter stream for keywords {}", Arrays.toString(keywords));
        simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepTimesMs);
    }

    private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepTimesMs) {
        // Run as Virtual thread
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    String formattedTweetAsRawJson = getFormattedTweet(keywords, minTweetLength, maxTweetLength);
                    Status status = TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
                    twitterKafkaStatusListener.onStatus(status);
                    sleep(sleepTimesMs);
                }
            } catch (TwitterException e) {
                LOG.error("Error creating tweeter status : ", e);
            }
        });

    }

    private void sleep(long sleepTimesMs) {
        try {
            Thread.sleep(sleepTimesMs);
        } catch (InterruptedException e) {
            throw new TwitterToKafkaException("Error while sleeping for waiting new status to created", e);
        }
    }

    private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
        String [] params = new String[] {
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };
        return formatTweeterAsJsonWithParams(params);
    }

    private static String formatTweeterAsJsonWithParams(String[] params) {
        String tweet = tweetAsRawJson;
        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }

    private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {
        StringBuilder tweet = new StringBuilder();
        int tweetLength = RANDOM.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;
        return constructRandomTweet(keywords, tweetLength, tweet);
    }

    private static String constructRandomTweet(String[] keywords, int tweetLength, StringBuilder tweet) {
        for (int i = 0; i < tweetLength; i++) {
            tweet.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
            if(i == tweetLength / 2) {
                tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweet.toString();
    }
}

