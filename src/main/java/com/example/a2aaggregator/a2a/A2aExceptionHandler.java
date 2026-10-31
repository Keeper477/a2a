package com.example.a2aaggregator.a2a;

import com.example.a2aaggregator.task.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class A2aExceptionHandler {
    @ExceptionHandler(TaskNotFoundException.class)
    ResponseEntity<A2aModels.GoogleRpcErrorBody> notFound(TaskNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Задача " + exception.getMessage() + " не найдена",
                "TASK_NOT_FOUND", Map.of("taskId", exception.getMessage()));
    }

    @ExceptionHandler(UnsupportedA2aVersionException.class)
    ResponseEntity<A2aModels.GoogleRpcErrorBody> version(UnsupportedA2aVersionException exception) {
        return error(HttpStatus.BAD_REQUEST, "FAILED_PRECONDITION",
                "Версия A2A " + exception.getMessage() + " не поддерживается; используйте 1.0",
                "VERSION_NOT_SUPPORTED", Map.of("requestedVersion", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<A2aModels.GoogleRpcErrorBody> badRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", exception.getMessage(),
                "INVALID_REQUEST", Map.of());
    }

    private static ResponseEntity<A2aModels.GoogleRpcErrorBody> error(
            HttpStatus httpStatus, String status, String message, String reason, Map<String, Object> metadata) {
        var detail = Map.<String, Object>of(
                "@type", "type.googleapis.com/google.rpc.ErrorInfo", "reason", reason,
                "domain", "a2a-protocol.org", "metadata", metadata);
        var body = new A2aModels.GoogleRpcErrorBody(new A2aModels.GoogleRpcError(
                httpStatus.value(), status, message, List.of(detail)));
        return ResponseEntity.status(httpStatus).contentType(org.springframework.http.MediaType
                .parseMediaType(A2aController.A2A_MEDIA_TYPE)).body(body);
    }
}
