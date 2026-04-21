package io.github.spring.middleware.orchestrator.core.domain;

import lombok.Data;

import java.util.Map;

@Data
public class NextActionDefinition {

    private String resolver;
    private Map<String, Object> parameters;

}
