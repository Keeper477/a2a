package com.example.a2aaggregator.recipient;

import com.example.a2aaggregator.config.AggregatorProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class McpRecipientGateway implements RecipientGateway {
    private final ObjectProvider<ToolCallbackProvider> providers;
    private final ObjectMapper objectMapper;

    public McpRecipientGateway(ObjectProvider<ToolCallbackProvider> providers, ObjectMapper objectMapper) {
        this.providers = providers;
        this.objectMapper = objectMapper;
    }

    @Override public RecipientType type() { return RecipientType.MCP; }

    @Override
    public Set<String> discover(AggregatorProperties.Recipient recipient) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        callbacks().filter(callback -> belongsTo(callback, recipient))
                .map(callback -> capabilityName(callback, recipient))
                .forEach(result::add);
        if (result.isEmpty()) throw new IllegalStateException("Spring AI не обнаружил MCP tools для " + recipient.getId());
        return Set.copyOf(result);
    }

    @Override
    public String invoke(AggregatorProperties.Recipient recipient, String taskType, String initiator) {
        ToolCallback callback = callbacks()
                .filter(candidate -> belongsTo(candidate, recipient))
                .filter(candidate -> capabilityName(candidate, recipient).equalsIgnoreCase(taskType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("MCP tool для taskType=" + taskType + " не найден"));
        try {
            return callback.call(objectMapper.writeValueAsString(Map.of(
                    "taskType", taskType, "initiator", initiator)));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Не удалось сериализовать аргументы MCP tool", exception);
        }
    }

    private java.util.stream.Stream<ToolCallback> callbacks() {
        return providers.orderedStream().flatMap(provider -> Arrays.stream(provider.getToolCallbacks()));
    }

    private static boolean belongsTo(ToolCallback callback, AggregatorProperties.Recipient recipient) {
        String name = callback.getToolDefinition().name();
        if (recipient.getToolPrefix() != null && !recipient.getToolPrefix().isBlank()) {
            return name.startsWith(recipient.getToolPrefix());
        }
        return recipient.getTaskTypes().stream().anyMatch(type -> name.equalsIgnoreCase(type));
    }

    private static String capabilityName(ToolCallback callback, AggregatorProperties.Recipient recipient) {
        String name = callback.getToolDefinition().name();
        String prefix = recipient.getToolPrefix();
        return prefix != null && !prefix.isBlank() && name.startsWith(prefix) ? name.substring(prefix.length()) : name;
    }
}
