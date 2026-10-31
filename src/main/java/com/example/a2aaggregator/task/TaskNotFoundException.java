package com.example.a2aaggregator.task;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String id) { super(id); }
}
