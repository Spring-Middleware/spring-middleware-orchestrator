package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowActionExecutorTest {

    @Mock
    private FunctionFlowActionExecutor functionFlowActionExecutor;

    @Mock
    private ConsumerFlowActionExecutor consumerFlowActionExecutor;

    @Mock
    private ResumeFlowActionExecutor resumeFlowActionExecutor;

    @InjectMocks
    private FlowActionExecutor flowActionExecutor;

    private FlowExecutionActionRequest flowExecutionActionRequest;
    private FlowExecutionActionRequest resultRequest;
    private ActionDefinition actionDefinition;

    @BeforeEach
    void setUp() {
        actionDefinition = new ActionDefinition();

        flowExecutionActionRequest = FlowExecutionActionRequest.builder()
                .actionDefinition(actionDefinition)
                .build();

        resultRequest = FlowExecutionActionRequest.builder().build();
    }

    @Test
    void execute_FunctionActionType() {
        actionDefinition.setActionType(ActionType.FUNCTION);
        when(functionFlowActionExecutor.executionActionRequest(any())).thenReturn(resultRequest);

        FlowExecutionActionRequest response = flowActionExecutor.execute(flowExecutionActionRequest);

        assertEquals(resultRequest, response);
        verify(functionFlowActionExecutor).executionActionRequest(flowExecutionActionRequest);
    }

    @Test
    void execute_ConsumerActionType() {
        actionDefinition.setActionType(ActionType.CONSUMER);

        FlowExecutionActionRequest response = flowActionExecutor.execute(flowExecutionActionRequest);

        assertNull(response);
        verify(consumerFlowActionExecutor).executionActionRequest(flowExecutionActionRequest);
    }

    @Test
    void execute_ResumeActionType() {
        actionDefinition.setActionType(ActionType.RESUME);
        when(resumeFlowActionExecutor.executionActionRequest(any())).thenReturn(resultRequest);

        FlowExecutionActionRequest response = flowActionExecutor.execute(flowExecutionActionRequest);

        assertEquals(resultRequest, response);
        verify(resumeFlowActionExecutor).executionActionRequest(flowExecutionActionRequest);
    }

}
