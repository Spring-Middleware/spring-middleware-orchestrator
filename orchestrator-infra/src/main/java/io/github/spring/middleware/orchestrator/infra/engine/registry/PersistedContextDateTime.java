package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class PersistedContextDateTime {

    private LocalDateTime dateTime;
    private TimeoutDefinition timeoutDefinition;

}
