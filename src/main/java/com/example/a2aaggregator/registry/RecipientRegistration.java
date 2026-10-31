package com.example.a2aaggregator.registry;

import com.example.a2aaggregator.config.AggregatorProperties;
import com.example.a2aaggregator.recipient.RecipientStatus;

import java.time.Instant;
import java.util.Set;

public record RecipientRegistration(
        AggregatorProperties.Recipient recipient,
        RecipientStatus status,
        Set<String> capabilities,
        String error,
        Instant updatedAt) {
}
