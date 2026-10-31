package com.example.a2aaggregator.registry;

import com.example.a2aaggregator.config.AggregatorProperties;
import com.example.a2aaggregator.recipient.RecipientGateway;
import com.example.a2aaggregator.recipient.RecipientGatewayFactory;
import com.example.a2aaggregator.recipient.RecipientStatus;
import com.example.a2aaggregator.recipient.RecipientType;
import com.example.a2aaggregator.task.SerezhaScenarioTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CapabilityRegistryInitializerTest {
    @Test
    void fillsRegistryAtStartupAndKeepsFailedRecipientAsUnavailable() {
        var good = SerezhaScenarioTest.recipient("good", RecipientType.A2A, "greeting");
        var broken = SerezhaScenarioTest.recipient("broken", RecipientType.A2A, "translation");
        var properties = new AggregatorProperties();
        properties.setRecipients(List.of(good, broken));
        var registry = new CapabilityRegistry();

        RecipientGateway gateway = mock(RecipientGateway.class);
        when(gateway.discover(good)).thenReturn(Set.of("greeting", "welcome"));
        when(gateway.discover(broken)).thenThrow(new IllegalStateException("simulated failure"));
        RecipientGatewayFactory factory = mock(RecipientGatewayFactory.class);
        when(factory.get(RecipientType.A2A)).thenReturn(gateway);
        Executor direct = Runnable::run;

        new CapabilityRegistryInitializer(properties, registry, factory, direct).refreshAll().join();

        assertThat(registry.availableCapabilities()).containsExactlyInAnyOrder("greeting", "welcome");
        assertThat(registry.all()).hasSize(2);
        assertThat(registry.all()).anySatisfy(entry -> {
            assertThat(entry.recipient().getId()).isEqualTo("broken");
            assertThat(entry.status()).isEqualTo(RecipientStatus.UNAVAILABLE);
            assertThat(entry.error()).contains("simulated failure");
        });
    }
}
