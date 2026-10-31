package com.example.a2aaggregator.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class A2aModels {
    private A2aModels() { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Part(String text, Object data, String mediaType) {
        public static Part text(String value) { return new Part(value, null, "text/plain"); }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Message(String messageId, String contextId, String taskId, String role,
                          List<Part> parts, Map<String, Object> metadata) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SendMessageRequest(Message message, Map<String, Object> configuration,
                                     Map<String, Object> metadata) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SendMessageResponse(Task task, Message message) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Task(String id, String contextId, TaskStatus status, List<Artifact> artifacts,
                       List<Message> history, Map<String, Object> metadata) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TaskStatus(String state, Message message, Instant timestamp) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Artifact(String artifactId, String name, String description, List<Part> parts,
                           Map<String, Object> metadata) { }

    public record AgentCapabilities(boolean streaming, boolean pushNotifications) { }
    public record AgentInterface(String url, String protocolBinding, String protocolVersion) { }
    public record AgentSkill(String id, String name, String description, List<String> tags) { }
    public record AgentCard(String name, String description, String version,
                            AgentCapabilities capabilities, List<String> defaultInputModes,
                            List<String> defaultOutputModes, List<AgentInterface> supportedInterfaces,
                            List<AgentSkill> skills) { }

    public record GoogleRpcErrorBody(GoogleRpcError error) { }
    public record GoogleRpcError(int code, String status, String message, List<Map<String, Object>> details) { }
}
