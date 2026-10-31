package com.example.a2aaggregator.registry;

import com.example.a2aaggregator.config.AggregatorProperties;
import com.example.a2aaggregator.recipient.RecipientGatewayFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class CapabilityRegistryInitializer {
    private static final Logger log = LoggerFactory.getLogger(CapabilityRegistryInitializer.class);
    private final AggregatorProperties properties;
    private final CapabilityRegistry registry;
    private final RecipientGatewayFactory gateways;
    private final Executor executor;

    public CapabilityRegistryInitializer(AggregatorProperties properties, CapabilityRegistry registry,
                                         RecipientGatewayFactory gateways, Executor capabilityExecutor) {
        this.properties = properties;
        this.registry = registry;
        this.gateways = gateways;
        this.executor = capabilityExecutor;
    }

    @Async("capabilityExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() { refreshAll(); }

    public CompletableFuture<Void> refreshAll() {
        List<CompletableFuture<Void>> futures = properties.getRecipients().stream().map(recipient -> {
            registry.startDiscovery(recipient);
            return CompletableFuture.runAsync(() -> probe(recipient), executor);
        }).toList();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void probe(AggregatorProperties.Recipient recipient) {
        try {
            registry.available(recipient, gateways.get(recipient.getType()).discover(recipient));
            log.info("Получатель {} доступен", recipient.getId());
        } catch (Exception exception) {
            registry.unavailable(recipient, exception);
            log.error("Получатель {} недоступен: {}", recipient.getId(), exception.getMessage(), exception);
        }
    }
}
