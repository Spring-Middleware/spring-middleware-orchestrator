package io.github.spring.middleware.orchestrator.infra.engine.repository;

import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionOrder;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "flow_executions")
public class FlowExecutionDocument {

    @Id
    private UUID id;
    private String requestId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String flowId;
    private ExecutionStatus executionStatus;
    private Object input;
    private Object context;
    private Map<String, ActionExecutionOrder> actionExecutions;

}
