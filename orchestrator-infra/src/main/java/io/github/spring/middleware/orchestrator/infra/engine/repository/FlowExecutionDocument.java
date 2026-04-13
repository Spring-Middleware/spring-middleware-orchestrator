package io.github.spring.middleware.orchestrator.infra.engine.repository;

import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Document(collection = "flow_executions")
public class FlowExecutionDocument<T> {

    @Id
    private UUID id;
    private String requestId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String flowId;
    private ExecutionStatus executionStatus;
    private Object context;
    private List<ActionExecution> actionExecutions;

}
