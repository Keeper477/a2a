package com.example.a2aaggregator.task;

import com.example.a2aaggregator.a2a.A2aModels;
import com.example.a2aaggregator.recipient.RecipientGatewayFactory;
import com.example.a2aaggregator.recipient.RecipientStatus;
import com.example.a2aaggregator.registry.CapabilityRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskRoutingService {
    private final TaskRepository tasks;
    private final CapabilityRegistry registry;
    private final RecipientGatewayFactory gateways;

    public TaskRoutingService(TaskRepository tasks, CapabilityRegistry registry, RecipientGatewayFactory gateways) {
        this.tasks = tasks;
        this.registry = registry;
        this.gateways = gateways;
    }

    public A2aModels.Task create(A2aModels.SendMessageRequest request) {
        if (request == null || request.message() == null) throw new IllegalArgumentException("message обязателен");
        String taskType = value(request.message().metadata(), request.metadata(), "taskType");
        String initiator = value(request.message().metadata(), request.metadata(), "initiator");
        if (taskType == null || taskType.isBlank()) throw new IllegalArgumentException("metadata.taskType обязателен");
        if (initiator == null || initiator.isBlank()) initiator = "unknown";
        return tasks.save(new StoredTask(taskType, initiator, request.message())).toProtocolTask();
    }

    public A2aModels.Task getAndExecute(String id) {
        StoredTask task = tasks.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        synchronized (task) {
            if (!"TASK_STATE_SUBMITTED".equals(task.state())) return task.toProtocolTask();
            route(task);
            return task.toProtocolTask();
        }
    }

    private void route(StoredTask task) {
        var matching = registry.matching(task.taskType());
        List<com.example.a2aaggregator.registry.RecipientRegistration> available = matching.stream()
                .filter(entry -> entry.status() == RecipientStatus.AVAILABLE).toList();
        if (available.size() > 1) {
            task.inputRequired("Найдено несколько получателей для taskType=" + task.taskType()
                    + ": " + available.stream().map(entry -> entry.recipient().getId()).toList()
                    + ". Уточните получателя.");
            return;
        }
        if (available.isEmpty()) {
            if (!matching.isEmpty()) task.fail("Подходящий получатель для taskType=" + task.taskType() + " недоступен");
            else task.fail("Подходящий получатель для taskType=" + task.taskType() + " не найден");
            return;
        }
        var target = available.getFirst();
        task.working();
        try {
            String result = gateways.get(target.recipient().getType())
                    .invoke(target.recipient(), task.taskType(), task.initiator());
            task.complete(result);
        } catch (Exception exception) {
            task.fail("Ошибка вызова получателя " + target.recipient().getId() + ": " + exception.getMessage());
        }
    }

    private static String value(java.util.Map<String, Object> primary, java.util.Map<String, Object> secondary, String key) {
        Object value = primary == null ? null : primary.get(key);
        if (value == null && secondary != null) value = secondary.get(key);
        return value == null ? null : value.toString();
    }
}
