package io.github.spring.middleware.orchestrator.demo.flows.resolver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowInput {

    private UUID flowInputId;
    private Map<String, String> valuesByAction;

}
