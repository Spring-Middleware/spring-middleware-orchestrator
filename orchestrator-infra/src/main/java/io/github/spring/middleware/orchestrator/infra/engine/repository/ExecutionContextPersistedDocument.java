package io.github.spring.middleware.orchestrator.infra.engine.repository;


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
@Document(collection = "execution_contexts")
public class ExecutionContextPersistedDocument<T> {

    @Id
    private UUID id;
    private LocalDateTime creationDateTime;
    private String actionName;
    private Map<String, Object> context;
    private T payload;

}
