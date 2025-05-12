package com.microservices.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.config.TwitterToKafkaServiceConfig;
import com.microservices.twitter.to.kafka.service.listner.TwitterKafkaStatusListener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-v2-tweets", havingValue = "true", matchIfMissing = true)
public class TwitterV2StreamHelper {

    private static final String tweetAsRowJson = "{" + "\"created_at\":\"{0}\"," + "\"id\":\"{1}\"," + "\"text\":\"{2}\"," + "\"user\":{\"id\":\"{3}\"}" + "}";
    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZ yyyy";
    private final Logger LOG = LoggerFactory.getLogger(TwitterV2StreamHelper.class);
    private final TwitterToKafkaServiceConfig twitterToKafkaServiceConfig;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    public TwitterV2StreamHelper(TwitterToKafkaServiceConfig twitterToKafkaServiceConfig,
                                 TwitterKafkaStatusListener twitterKafkaStatusListener) {
        this.twitterToKafkaServiceConfig = twitterToKafkaServiceConfig;
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
    }

    /*
     * This method calls the filtered stream endpoint and streams Tweets from it
     * */
    public void connectStream(String bearerToken) throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        URIBuilder uriBuilder = new URIBuilder(twitterToKafkaServiceConfig.getTwitterV2BaseUrl());

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
            String line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                if (!line.isEmpty()) {
                    String tweet = getFormattedTweet(line);
                    Status status = null;
                    try {
                        status = TwitterObjectFactory.createStatus(tweet);
                    } catch (TwitterException e) {
                        LOG.error("Could not create twitter stream {}", tweet, e);
                    }
                    if (status != null) {
                        twitterKafkaStatusListener.onStatus(status);
                    }
                }
            }
        }

    }

    private String getFormattedTweet(String data) {
        JSONObject jsonData = (JSONObject) new JSONObject(data).get("data");

        String[] params = new String[]{ZonedDateTime
                .parse(jsonData.get("created_at").toString())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.FRENCH)),
                jsonData.get("id").toString(),
                jsonData.get("text").toString().replaceAll("\"", "\\\\\""),
                jsonData.get("author_id").toString(),};
        return formatTweetAsString(params);
    }

    /*
     * Helper method to setup rules before streaming data
     * */
    public void setupRules(String bearerToken, Map<String, String> rules) throws IOException, URISyntaxException {
        List<String> existingRules = getRules(bearerToken);
        if (!existingRules.isEmpty()) {
            deleteRules(bearerToken, existingRules);
        }
        createRules(bearerToken, rules);
    }

    private List<String> getRules(String bearerToken) throws URISyntaxException, IOException {
        List<String> rules = new ArrayList<>();
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(twitterToKafkaServiceConfig.getTwitterV2RulesBaseUrl());
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("content-type", String.format("Bearer %s", bearerToken));
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity httpEntity = response.getEntity();
        if (null != httpEntity) {
            JSONObject json = new JSONObject(EntityUtils.toString(httpEntity, "UTF-8"));
            if (json.length() > 1) {
                JSONArray array = (JSONArray) json.get("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = (JSONObject) array.get(i);
                    rules.add(jsonObject.getString("id"));
                }
            }
        }
        return rules;
    }

    /*
     * Helper method to delete rules
     * */
    public void deleteRules(String bearerToken, List<String> existingRules) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();

        URIBuilder uriBuilder = new URIBuilder(twitterToKafkaServiceConfig.getTwitterV2RulesBaseUrl());

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpPost.setHeader("content-type", "application/json");
        StringEntity body = new StringEntity(getFormattedString("{ \"delete\": { \"ids\": [%s]}}", existingRules));
        httpPost.setEntity(body);
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        }
    }

    /*
     * Helper method to create rules for filtering
     * */
    public void createRules(String bearerToken, Map<String, String> rules) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();

        URIBuilder uriBuilder = new URIBuilder(twitterToKafkaServiceConfig.getTwitterV2RulesBaseUrl());

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpPost.setHeader("content-type", "application/json");
        StringEntity body = new StringEntity(getFormattedString("{\"add\": [%s]}", rules));
        httpPost.setEntity(body);
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        }
    }

    private String formatTweetAsString(String[] params) {
        String tweet = tweetAsRowJson;
        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }

    private String getFormattedString(String str, List<String> ids) {
        StringBuilder sb = new StringBuilder();
        if (ids.size() == 1) {
            return String.format(str, "\"" + ids.get(0) + "\"");
        } else {
            for (String id : ids) {
                sb.append("\"").append(id).append("\"").append(",");
            }
            String result = sb.toString();
            return String.format(str, result.substring(0, result.length() - 1));
        }
    }

    private String getFormattedString(String string, Map<String, String> rules) {
        StringBuilder sb = new StringBuilder();
        if (rules.size() == 1) {
            String key = rules.keySet().iterator().next();
            return String.format(string, "{\"value\": \"" + key + "\", \"tag\": \"" + rules.get(key) + "\"}");
        } else {
            for (Map.Entry<String, String> entry : rules.entrySet()) {
                String value = entry.getKey();
                String tag = entry.getValue();
                sb.append("{\"value\": \"" + value + "\", \"tag\": \"" + tag + "\"}" + ",");
            }
            String result = sb.toString();
            return String.format(string, result.substring(0, result.length() - 1));
        }
    }
}
