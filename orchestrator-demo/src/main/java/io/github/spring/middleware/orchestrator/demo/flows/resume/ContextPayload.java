package io.github.spring.middleware.orchestrator.demo.flows.resume;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ContextPayload {

    private String key;
    private ResumeType resumeType;
    private UUID flowExecutionId;
    private List<Step> steps = new ArrayList<>();

    public void addStep(final Step step) {
        steps.add(step);
    }

}
