package io.github.spring.middleware.orchestrator.infra.engine.repository;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@Document(collection = "execution_contexts")
public class ExecutionContextPersistedDocument {

    @Id
    private UUID id;
    private LocalDateTime creationDateTime;
    private String actionName;
    private Map<String, Object> context;

}
