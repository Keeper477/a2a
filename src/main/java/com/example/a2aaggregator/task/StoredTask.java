package com.example.a2aaggregator.task;

import com.example.a2aaggregator.a2a.A2aModels;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoredTask {
    private final String id = UUID.randomUUID().toString();
    private final String contextId = UUID.randomUUID().toString();
    private final String taskType;
    private final String initiator;
    private final A2aModels.Message input;
    private String state = "TASK_STATE_SUBMITTED";
    private String statusText;
    private String result;

    public StoredTask(String taskType, String initiator, A2aModels.Message input) {
        this.taskType = taskType;
        this.initiator = initiator;
        this.input = input;
    }

    public String id() { return id; }
    public String taskType() { return taskType; }
    public String initiator() { return initiator; }
    public synchronized String state() { return state; }
    public synchronized void working() { state = "TASK_STATE_WORKING"; statusText = null; }
    public synchronized void complete(String value) { state = "TASK_STATE_COMPLETED"; result = value; statusText = value; }
    public synchronized void inputRequired(String value) { state = "TASK_STATE_INPUT_REQUIRED"; statusText = value; }
    public synchronized void fail(String value) { state = "TASK_STATE_FAILED"; statusText = value; }

    public synchronized A2aModels.Task toProtocolTask() {
        A2aModels.Message statusMessage = statusText == null ? null : new A2aModels.Message(
                UUID.randomUUID().toString(), contextId, id, "ROLE_AGENT",
                List.of(A2aModels.Part.text(statusText)), null);
        List<A2aModels.Artifact> artifacts = result == null ? null : List.of(new A2aModels.Artifact(
                UUID.randomUUID().toString(), "result", "Результат выполнения задачи",
                List.of(A2aModels.Part.text(result)), null));
        return new A2aModels.Task(id, contextId,
                new A2aModels.TaskStatus(state, statusMessage, Instant.now()), artifacts,
                List.of(input), Map.of("taskType", taskType, "initiator", initiator));
    }
}
