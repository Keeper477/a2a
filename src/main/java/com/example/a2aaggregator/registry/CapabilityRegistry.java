package com.example.a2aaggregator.registry;

import com.example.a2aaggregator.config.AggregatorProperties;
import com.example.a2aaggregator.recipient.RecipientStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CapabilityRegistry {
    private final ConcurrentHashMap<String, RecipientRegistration> registrations = new ConcurrentHashMap<>();

    public void startDiscovery(AggregatorProperties.Recipient recipient) {
        registrations.put(recipient.getId(), new RecipientRegistration(recipient, RecipientStatus.DISCOVERING,
                Set.copyOf(recipient.getTaskTypes()), null, Instant.now()));
    }

    public void available(AggregatorProperties.Recipient recipient, Set<String> capabilities) {
        registrations.put(recipient.getId(), new RecipientRegistration(recipient, RecipientStatus.AVAILABLE,
                Set.copyOf(capabilities), null, Instant.now()));
    }

    public void unavailable(AggregatorProperties.Recipient recipient, Throwable error) {
        registrations.put(recipient.getId(), new RecipientRegistration(recipient, RecipientStatus.UNAVAILABLE,
                Set.copyOf(recipient.getTaskTypes()), error.getMessage(), Instant.now()));
    }

    public Set<String> availableCapabilities() {
        return registrations.values().stream()
                .filter(entry -> entry.status() == RecipientStatus.AVAILABLE)
                .flatMap(entry -> entry.capabilities().stream()).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public List<RecipientRegistration> matching(String taskType) {
        return registrations.values().stream()
                .filter(entry -> entry.capabilities().stream().anyMatch(capability -> capability.equalsIgnoreCase(taskType)))
                .toList();
    }

    public Collection<RecipientRegistration> all() { return List.copyOf(registrations.values()); }
}
