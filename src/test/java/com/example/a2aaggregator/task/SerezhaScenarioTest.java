package com.example.a2aaggregator.task;

import com.example.a2aaggregator.a2a.A2aModels;
import com.example.a2aaggregator.config.AggregatorProperties;
import com.example.a2aaggregator.recipient.RecipientGateway;
import com.example.a2aaggregator.recipient.RecipientGatewayFactory;
import com.example.a2aaggregator.recipient.RecipientType;
import com.example.a2aaggregator.registry.CapabilityRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SerezhaScenarioTest {
    @Test
    void createsTaskThenRoutesItAndReturnsGreeting() {
        var repository = new TaskRepository();
        var registry = new CapabilityRegistry();
        var recipient = recipient("greeter", RecipientType.A2A, "greeting");
        registry.available(recipient, Set.of("greeting"));

        RecipientGateway gateway = mock(RecipientGateway.class);
        when(gateway.invoke(recipient, "greeting", "Серёжа")).thenReturn("Привет, Серёжа");
        RecipientGatewayFactory factory = mock(RecipientGatewayFactory.class);
        when(factory.get(RecipientType.A2A)).thenReturn(gateway);
        var service = new TaskRoutingService(repository, registry, factory);

        var message = new A2aModels.Message("message-1", null, null, "ROLE_USER",
                List.of(A2aModels.Part.text("Создай приветствие")),
                Map.of("taskType", "greeting", "initiator", "Серёжа"));

        A2aModels.Task created = service.create(new A2aModels.SendMessageRequest(message, null, null));
        assertThat(created.id()).isNotBlank();
        assertThat(created.status().state()).isEqualTo("TASK_STATE_SUBMITTED");

        A2aModels.Task completed = service.getAndExecute(created.id());
        assertThat(completed.status().state()).isEqualTo("TASK_STATE_COMPLETED");
        assertThat(completed.status().message().parts().getFirst().text()).isEqualTo("Привет, Серёжа");
        assertThat(completed.artifacts().getFirst().parts().getFirst().text()).isEqualTo("Привет, Серёжа");
        verify(gateway).invoke(recipient, "greeting", "Серёжа");
    }

    public static AggregatorProperties.Recipient recipient(String id, RecipientType type, String taskType) {
        var recipient = new AggregatorProperties.Recipient();
        recipient.setId(id);
        recipient.setType(type);
        recipient.setTaskTypes(List.of(taskType));
        return recipient;
    }
}
