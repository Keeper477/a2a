package com.example.a2aaggregator.recipient;

import com.example.a2aaggregator.config.AggregatorProperties;
import tools.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class A2aRecipientGateway implements RecipientGateway {
    private static final String A2A_MEDIA_TYPE = "application/a2a+json";
    private final RestClient.Builder restClient;

    public A2aRecipientGateway(RestClient.Builder restClient) {
        this.restClient = restClient;
    }

    @Override public RecipientType type() { return RecipientType.A2A; }

    @Override
    public Set<String> discover(AggregatorProperties.Recipient recipient) {
        JsonNode card = restClient.build().get()
                .uri(normalize(recipient.getBaseUrl()) + "/.well-known/agent-card.json")
                .retrieve().body(JsonNode.class);
        HashSet<String> result = new HashSet<>();
        if (card != null) {
            card.path("skills").forEach(skill -> {
                if (skill.hasNonNull("id")) result.add(skill.get("id").asText());
                skill.path("tags").forEach(tag -> result.add(tag.asText()));
            });
        }
        if (result.isEmpty()) result.addAll(recipient.getTaskTypes());
        if (result.isEmpty()) throw new IllegalStateException("Agent Card не содержит skills");
        return Set.copyOf(result);
    }

    @Override
    public String invoke(AggregatorProperties.Recipient recipient, String taskType, String initiator) {
        var message = Map.of(
                "message", Map.of(
                        "messageId", UUID.randomUUID().toString(),
                        "role", "ROLE_USER",
                        "parts", new Object[]{Map.of("text", "Выполни задачу типа " + taskType)},
                        "metadata", Map.of("taskType", taskType, "initiator", initiator)));
        JsonNode response = restClient.build().post()
                .uri(normalize(recipient.getBaseUrl()) + "/message:send")
                .header("A2A-Version", "1.0")
                .contentType(MediaType.parseMediaType(A2A_MEDIA_TYPE))
                .body(message)
                .retrieve().body(JsonNode.class);
        var text = findText(response);
        if (text == null) throw new IllegalStateException("A2A-получатель не вернул текстовый результат");
        return text;
    }

    private static String findText(JsonNode node) {
        if (node == null) return null;
        if (node.isObject() && node.hasNonNull("text")) return node.get("text").asText();
        if (node.isContainer()) {
            for (JsonNode child : node) {
                var found = findText(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static String normalize(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("baseUrl обязателен для A2A");
        return url.replaceAll("/+$", "");
    }
}
