package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.domain.NextActionDefinition;
import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowNextActionResolver;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionNextAction;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeFlowActionExecutorTest {

    @Mock
    private FlowNextActionResolver flowNextActionResolver;

    @Mock
    private FlowExecutionRegistry flowExecutionRegistry;

    @InjectMocks
    private ResumeFlowActionExecutor executor;

    private FlowExecutionActionRequest<?> flowExecutionActionRequest;
    private ActionDefinition actionDefinition;
    private FlowExecution<String, String> flowExecution;
    private ExecutionContext<String> executionContext;
    private FlowDefinition flowDefinition;

    @BeforeEach
    void setUp() {
        actionDefinition = new ActionDefinition();
        actionDefinition.setActionName("testResume");
        actionDefinition.setActionType(ActionType.RESUME);

        flowExecution = new FlowExecution<>();
        flowExecution.setId(UUID.randomUUID());
        flowExecution.setFlowId(new FlowId("testFlow"));

        executionContext = new ExecutionContext<>(flowExecution, "initialPayload");

        flowDefinition = new FlowDefinition();
        flowDefinition.setFlowId(new FlowId("testFlow"));
        flowDefinition.setActions(List.of(actionDefinition));
        flowDefinition.buildActionDefinitionMap();

        flowExecutionActionRequest = FlowExecutionActionRequest.builder()
                .actionDefinition(actionDefinition)
                .executionContext(executionContext)
                .flowDefinition(flowDefinition)
                .payload("testPayload")
                .build();
    }

    @Test
    void execute_SuccessWithNextAction() {
        NextActionDefinition nextActionDef = new NextActionDefinition();
        nextActionDef.setResolver("testResolver");
        actionDefinition.setNextAction(nextActionDef);
        actionDefinition.setFinalAction(false);

        NextActionResolverResult<?> resolverResult = NextActionResolverResult.builder()
                .nextAction("nextAction")
                .result("nextPayload")
                .build();

        FlowExecutionNextAction flowExecutionNextAction = mock(FlowExecutionNextAction.class);

        when(flowNextActionResolver.getNextAction(eq(flowDefinition), any(), eq("testResume"), any())).thenReturn(flowExecutionNextAction);
        when(flowExecutionNextAction.getActionDefinition()).thenReturn(ActionDefinition.builder().actionName("nextAction").build());
        when(flowExecutionNextAction.getNextActionResolverResult()).thenReturn(resolverResult);

        FlowExecutionActionRequest<?> nextRequest = executor.executionActionRequest(flowExecutionActionRequest);

        assertNotNull(nextRequest);
        assertEquals("nextPayload", nextRequest.getPayload());
        assertEquals("nextAction", nextRequest.getActionDefinition().getActionName());
        assertEquals(executionContext, nextRequest.getExecutionContext());

        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testResume");
        assertNotNull(execution);
        assertEquals(true, execution.getExecuted());
    }

    @Test
    void execute_FinalAction() {
        actionDefinition.setFinalAction(true);

        FlowExecutionActionRequest<?> nextRequest = executor.executionActionRequest(flowExecutionActionRequest);

        assertNull(nextRequest);
        assertEquals(ExecutionStatus.EXECUTED, executionContext.getFlowExecution().getExecutionStatus());

        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testResume");
        assertNotNull(execution);
        assertEquals(true, execution.getExecuted());
    }

    @Test
    void execute_ThrowsException() {
        when(flowNextActionResolver.getNextAction(any(), any(), any(), any())).thenThrow(new RuntimeException("Error resolving next action"));

        actionDefinition.setFinalAction(false);

        FlowExecutionActionRequest<?> nextRequest = executor.executionActionRequest(flowExecutionActionRequest);

        assertNull(nextRequest);
        assertEquals(ExecutionStatus.ERROR, executionContext.getFlowExecution().getExecutionStatus());

        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testResume");
        assertNotNull(execution);
        assertNotNull(execution.getError());
        assertEquals("Error resolving next action", execution.getError());
    }
}

