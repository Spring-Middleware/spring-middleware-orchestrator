package io.github.spring.middleware.orchestrator.core.runtime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ActionExecution {

    private String actionName;
    private String error;
    private Boolean executed;
    private LocalDateTime executionDatetime;
    private ActionExecutionContext context;

}
