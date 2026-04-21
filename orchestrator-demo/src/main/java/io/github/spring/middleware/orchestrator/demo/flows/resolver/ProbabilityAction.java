package io.github.spring.middleware.orchestrator.demo.flows.resolver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProbabilityAction {

    private String actionName;
    private Double probability;

}
