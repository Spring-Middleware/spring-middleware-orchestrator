package io.github.spring.middleware.orchestrator.core.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TimeoutDefinition {

    private Long timeoutSeconds;
    private String onTimeoutResolver;

}
