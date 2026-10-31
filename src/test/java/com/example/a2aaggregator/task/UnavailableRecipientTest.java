package com.example.a2aaggregator.task;

import com.example.a2aaggregator.a2a.A2aModels;
import com.example.a2aaggregator.recipient.RecipientGatewayFactory;
import com.example.a2aaggregator.recipient.RecipientType;
import com.example.a2aaggregator.registry.CapabilityRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UnavailableRecipientTest {
    @Test
    void returnsClearFailureWhenMatchingRecipientIsUnavailable() {
        var registry = new CapabilityRegistry();
        var recipient = SerezhaScenarioTest.recipient("broken-greeter", RecipientType.MCP, "greeting");
        registry.unavailable(recipient, new IllegalStateException("connection refused"));
        var service = new TaskRoutingService(new TaskRepository(), registry, mock(RecipientGatewayFactory.class));
        var message = new A2aModels.Message("m-1", null, null, "ROLE_USER",
                List.of(A2aModels.Part.text("Поздоровайся")),
                Map.of("taskType", "greeting", "initiator", "Серёжа"));

        var created = service.create(new A2aModels.SendMessageRequest(message, null, null));
        var result = service.getAndExecute(created.id());

        assertThat(result.status().state()).isEqualTo("TASK_STATE_FAILED");
        assertThat(result.status().message().parts().getFirst().text()).contains("недоступен");
    }
}
