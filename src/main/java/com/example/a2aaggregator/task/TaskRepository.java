package com.example.a2aaggregator.task;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TaskRepository {
    private final ConcurrentHashMap<String, StoredTask> tasks = new ConcurrentHashMap<>();
    public StoredTask save(StoredTask task) { tasks.put(task.id(), task); return task; }
    public Optional<StoredTask> findById(String id) { return Optional.ofNullable(tasks.get(id)); }
}
