package com.example.a2aaggregator.a2a;

import com.example.a2aaggregator.registry.CapabilityRegistry;
import com.example.a2aaggregator.task.TaskRoutingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController
public class A2aController {
    public static final String A2A_MEDIA_TYPE = "application/a2a+json";
    private final TaskRoutingService tasks;
    private final CapabilityRegistry registry;

    public A2aController(TaskRoutingService tasks, CapabilityRegistry registry) {
        this.tasks = tasks;
        this.registry = registry;
    }

    @PostMapping(value = "/message:send", consumes = {A2A_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE},
            produces = A2A_MEDIA_TYPE)
    public A2aModels.SendMessageResponse send(
            @RequestHeader(value = "A2A-Version", defaultValue = "1.0") String version,
            @RequestBody A2aModels.SendMessageRequest request) {
        checkVersion(version);
        return new A2aModels.SendMessageResponse(tasks.create(request), null);
    }

    @GetMapping(value = "/tasks/{id}", produces = A2A_MEDIA_TYPE)
    public A2aModels.Task get(@RequestHeader(value = "A2A-Version", defaultValue = "1.0") String version,
                              @PathVariable String id) {
        checkVersion(version);
        return tasks.getAndExecute(id);
    }

    @GetMapping(value = "/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> capabilities() {
        return Map.of("protocolVersion", "1.0", "capabilities", registry.availableCapabilities());
    }

    @GetMapping(value = "/.well-known/agent-card.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<A2aModels.AgentCard> agentCard() {
        List<A2aModels.AgentSkill> skills = registry.availableCapabilities().stream().sorted()
                .map(capability -> new A2aModels.AgentSkill(capability, capability,
                        "Агрегированная возможность внешнего получателя", List.of(capability)))
                .toList();
        var card = new A2aModels.AgentCard(
                "A2A capability aggregator", "Агрегатор и маршрутизатор A2A/MCP", "1.0.0",
                new A2aModels.AgentCapabilities(false, false), List.of("text/plain"), List.of("text/plain"),
                List.of(new A2aModels.AgentInterface("http://localhost:8080", "HTTP+JSON", "1.0")), skills);
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).body(card);
    }

    private static void checkVersion(String version) {
        if (!"1.0".equals(version)) throw new UnsupportedA2aVersionException(version);
    }
}
