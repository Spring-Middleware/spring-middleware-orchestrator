package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.domain.NextActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowNextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.action.FunctionAction;
import io.github.spring.middleware.orchestrator.core.port.ActionRegistry;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.port.NextActionResolverRegistry;
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
class FunctionFlowActionExecutorTest {

    @Mock
    private ActionRegistry actionRegistry;

    @Mock
    private FlowNextActionResolver flowNextActionResolver;

    @Mock
    private FlowExecutionRegistry flowExecutionRegistry;

    @InjectMocks
    private FunctionFlowActionExecutor executor;

    private FlowExecutionActionRequest flowExecutionActionRequest;
    private ActionDefinition actionDefinition;
    private FlowExecution flowExecution;
    private ExecutionContext executionContext;
    private FlowDefinition flowDefinition;

    @BeforeEach
    void setUp() {
        actionDefinition = new ActionDefinition();
        actionDefinition.setActionName("testFunction");
        actionDefinition.setActionType(ActionType.FUNCTION);

        flowExecution = new FlowExecution();
        flowExecution.setId(UUID.randomUUID());
        flowExecution.setFlowId(new FlowId("testFlow"));

        executionContext = new ExecutionContext(flowExecution, "initialPayload");

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
        FunctionAction mockAction = mock(FunctionAction.class);
        when(mockAction.parsePayload(any())).thenReturn("parsedPayload");
        when(mockAction.apply(any(), any(), any())).thenReturn("resultPayload");
        when(actionRegistry.getAction(eq(actionDefinition.getActionName()), any())).thenReturn(mockAction);

        NextActionDefinition nextActionDef = new NextActionDefinition();
        nextActionDef.setResolver("testResolver");
        actionDefinition.setNextAction(nextActionDef);

        NextActionResolverResult resolverResult = NextActionResolverResult.builder()
                .nextAction("nextAction")
                .result("nextPayload")
                .build();

        FlowExecutionNextAction flowExecutionNextAction = mock(FlowExecutionNextAction.class);

        when(flowNextActionResolver.getNextAction(eq(flowDefinition),any(),any(),any())).thenReturn(flowExecutionNextAction);
        when(flowExecutionNextAction.getActionDefinition()).thenReturn(ActionDefinition.builder().actionName("nextAction").build());
        when(flowExecutionNextAction.getNextActionResolverResult()).thenReturn(resolverResult);

        FlowExecutionActionRequest nextRequest = executor.executionActionRequest(flowExecutionActionRequest);

        assertNotNull(nextRequest);
        assertEquals("nextPayload", nextRequest.getPayload());
        assertEquals("nextAction", nextRequest.getActionName());
        assertEquals(executionContext, nextRequest.getExecutionContext());

        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);
    }

    @Test
    void execute_FinalAction() {
        FunctionAction mockAction = mock(FunctionAction.class);
        when(mockAction.parsePayload(any())).thenReturn("parsedPayload");
        when(mockAction.apply(any(), any(), any())).thenReturn("resultPayload");
        when(actionRegistry.getAction(eq(actionDefinition.getActionName()), any())).thenReturn(mockAction);

        actionDefinition.setFinalAction(true);

        FlowExecutionActionRequest nextRequest = executor.executionActionRequest(flowExecutionActionRequest);

        assertNull(nextRequest);
        assertEquals(ExecutionStatus.EXECUTED, executionContext.getFlowExecution().getExecutionStatus());
    }
}
