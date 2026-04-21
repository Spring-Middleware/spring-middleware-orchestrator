package io.github.spring.middleware.orchestrator.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeoutDefinition {

    private Long seconds;
    private String resolver;
    private boolean removeContextOnLoad = true;
    private Map<String, Object> parameters;

}
