package io.github.spring.middleware.orchestrator.demo.flows.resume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextInput {

    private ResumeType resumeType;
    private String key;

}
