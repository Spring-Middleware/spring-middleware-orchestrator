package io.github.spring.middleware.orchestrator.demo.flows.resume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step {

    private Integer random;
    private String text;

}
