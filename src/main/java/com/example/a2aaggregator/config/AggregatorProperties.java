package com.example.a2aaggregator.config;

import com.example.a2aaggregator.recipient.RecipientType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("aggregator")
public class AggregatorProperties {
    private Duration requestTimeout = Duration.ofSeconds(10);
    private List<Recipient> recipients = new ArrayList<>();

    public Duration getRequestTimeout() { return requestTimeout; }
    public void setRequestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; }
    public List<Recipient> getRecipients() { return recipients; }
    public void setRecipients(List<Recipient> recipients) { this.recipients = recipients; }

    public static class Recipient {
        private String id;
        private RecipientType type;
        private String baseUrl;
        private String toolPrefix;
        private List<String> taskTypes = new ArrayList<>();

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public RecipientType getType() { return type; }
        public void setType(RecipientType type) { this.type = type; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getToolPrefix() { return toolPrefix; }
        public void setToolPrefix(String toolPrefix) { this.toolPrefix = toolPrefix; }
        public List<String> getTaskTypes() { return taskTypes; }
        public void setTaskTypes(List<String> taskTypes) { this.taskTypes = taskTypes; }
    }
}
