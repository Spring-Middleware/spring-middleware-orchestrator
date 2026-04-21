package io.github.spring.middleware.orchestrator.demo.flows.chained;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainedPayload {

    private String value;
    private ChainedType type;

}
