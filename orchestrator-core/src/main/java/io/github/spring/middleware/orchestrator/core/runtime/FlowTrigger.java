package io.github.spring.middleware.orchestrator.core.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowTrigger<T> {

    private String flowId;
    private T payload;

}
