# A2A capability aggregator

Java 25 / Spring Boot 4.0 / Spring AI 2.0 service implementing the A2A 1.0 HTTP+JSON binding.
It discovers configured A2A Agent Cards and Spring AI MCP tool callbacks asynchronously after
`ApplicationReadyEvent`, publishes the available skills in its own Agent Card and routes tasks lazily
when the client polls `GET /tasks/{id}`.

## Configuration

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        type: SYNC
        # Configure Streamable HTTP/SSE/STDIO connections using standard Spring AI properties.

aggregator:
  recipients:
    - id: greeting-a2a
      type: A2A
      base-url: https://agent.example.org
      task-types: [greeting]
    - id: greeting-mcp
      type: MCP
      tool-prefix: greeting_mcp_
      task-types: [greeting]
```

For MCP, Spring AI initializes the configured clients and exposes their remote tools as
`ToolCallbackProvider` beans. `tool-prefix` associates those discovered callbacks with a recipient;
the suffix is the capability/task type. If no prefix is used, callback names must match `task-types`.

## A2A calls

Only `Authorization: Bearer test-token-123` is accepted. The Agent Card is public.

```bash
curl -X POST http://localhost:8080/message:send \
  -H 'Authorization: Bearer test-token-123' \
  -H 'A2A-Version: 1.0' -H 'Content-Type: application/a2a+json' \
  -d '{"message":{"messageId":"m-1","role":"ROLE_USER","parts":[{"text":"Поздоровайся"}],"metadata":{"taskType":"greeting","initiator":"Серёжа"}}}'

curl http://localhost:8080/tasks/TASK_ID \
  -H 'Authorization: Bearer test-token-123' -H 'A2A-Version: 1.0'
```

Run tests with `mvn test` on a machine with JDK 25 and Maven installed.
