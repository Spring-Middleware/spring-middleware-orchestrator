package io.github.spring.middleware.orchestrator.infra.engine.registry;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ExecutionContextPersisted<T> {

    private UUID flowExecutionId;
    private LocalDateTime creationDateTime;
    private String actionName;
    private T payload;
    private Map<String, Object> context;

}
