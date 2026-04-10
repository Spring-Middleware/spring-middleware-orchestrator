package io.github.spring.middleware.orchestrator.core.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowTrigger {

    private String flowId;
    private Map<String, Object> payload;

}
