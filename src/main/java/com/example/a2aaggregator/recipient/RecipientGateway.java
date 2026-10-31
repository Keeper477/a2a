package com.example.a2aaggregator.recipient;

import com.example.a2aaggregator.config.AggregatorProperties;

import java.util.Set;

public interface RecipientGateway {
    RecipientType type();
    Set<String> discover(AggregatorProperties.Recipient recipient);
    String invoke(AggregatorProperties.Recipient recipient, String taskType, String initiator);
}
