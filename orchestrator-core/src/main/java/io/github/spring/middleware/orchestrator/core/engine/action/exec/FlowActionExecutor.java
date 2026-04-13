package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlowActionExecutor {

    private final FunctionFlowActionExecutor functionFlowActionExecutor;
    private final ConsumerFlowActionExecutor consumerFlowActionExecutor;
    private final ResumeFlowActionExecutor resumeFlowActionExecutor;

    public FlowExecutionActionRequest execute(FlowExecutionActionRequest flowExecutionActionRequest) {
        var actionType = flowExecutionActionRequest.getActionType();
        switch (actionType) {
            case FUNCTION -> {
                return functionFlowActionExecutor.executionActionRequest(flowExecutionActionRequest);
            }
            case CONSUMER -> {
                consumerFlowActionExecutor.executionActionRequest(flowExecutionActionRequest);
                return null;
            }
            case RESUME -> {
                return resumeFlowActionExecutor.executionActionRequest(flowExecutionActionRequest);
            }
            default -> throw new IllegalArgumentException(STR."Unsupported action type: \{actionType}");
        }

    }


}
