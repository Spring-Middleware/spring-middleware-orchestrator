package io.github.spring.middleware.orchestrator.demo.flows.resume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextEvent {

    private UUID flowExecutionId;
    private ResumeType resumeType;

}
