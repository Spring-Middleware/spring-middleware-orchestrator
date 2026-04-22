package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.port.ActionRegistry;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerFlowActionExecutorTest {

    @Mock
    private ActionRegistry actionRegistry;

    @Mock
    private FlowExecutionRegistry flowExecutionRegistry;

    @Mock
    private ExecutionContextManager executionContextManager;

    @InjectMocks
    private ConsumerFlowActionExecutor executor;

    private FlowExecutionActionRequest<String> flowExecutionActionRequest;
    private ActionDefinition actionDefinition;
    private FlowExecution<String, String> flowExecution;
    private ExecutionContext<String> executionContext;
    private TimeoutDefinition timeoutDefinition;

    @BeforeEach
    void setUp() {
        timeoutDefinition = TimeoutDefinition.builder().build();

        actionDefinition = new ActionDefinition();
        actionDefinition.setActionName("testConsumer");
        actionDefinition.setActionType(ActionType.CONSUMER);
        actionDefinition.setTimeout(timeoutDefinition);

        flowExecution = new FlowExecution<>();
        flowExecution.setId(UUID.randomUUID());
        flowExecution.setFlowId(new FlowId("testFlow"));

        executionContext = new ExecutionContext<>(flowExecution, "initialPayload");

        FlowDefinition flowDefinition = new FlowDefinition();
        flowDefinition.setFlowId(new FlowId("testFlow"));
        flowDefinition.setActions(List.of(actionDefinition));
        flowDefinition.buildActionDefinitionMap();

        flowExecutionActionRequest = FlowExecutionActionRequest.<String>builder()
                .actionDefinition(actionDefinition)
                .executionContext(executionContext)
                .flowDefinition(flowDefinition)
                .payload("testPayload")
                .build();
    }

    @Test
    void execute_SuccessNotFinalAction() {
        @SuppressWarnings("unchecked")
        ConsumerAction<Object> mockAction = mock(ConsumerAction.class);
        when(mockAction.parsePayload(any())).thenReturn("parsedPayload");
        when(actionRegistry.getAction(eq(actionDefinition.getActionName()), any())).thenReturn(mockAction);

        actionDefinition.setFinalAction(false);

        executor.executionActionRequest(flowExecutionActionRequest);

        // Being a ConsumerAction and not final -> should Suspend flow waiting for an event
        assertEquals(ExecutionStatus.SUSPENDED, executionContext.getFlowExecution().getExecutionStatus());

        // Assert it persisted the ExecutionContext to resume later
        verify(executionContextManager).persistExecutionContext(
                eq(flowExecution.getId()),
                eq(timeoutDefinition),
                eq("testConsumer"),
                eq("parsedPayload")
        );

        verify(mockAction).consume(eq(executionContext), any(ActionExecutionContext.class), eq("parsedPayload"));
        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testConsumer");
        assertNotNull(execution);
        assertTrue(execution.getExecuted());
    }

    @Test
    void execute_SuccessFinalAction() {
        @SuppressWarnings("unchecked")
        ConsumerAction<Object> mockAction = mock(ConsumerAction.class);
        when(mockAction.parsePayload(any())).thenReturn("parsedPayload");
        when(actionRegistry.getAction(eq(actionDefinition.getActionName()), any())).thenReturn(mockAction);

        actionDefinition.setFinalAction(true);

        executor.executionActionRequest(flowExecutionActionRequest);

        // If it was final, flow should END, not suspend.
        assertEquals(ExecutionStatus.EXECUTED, executionContext.getFlowExecution().getExecutionStatus());

        // And it shouldn't persist any wait context
        verify(executionContextManager, never()).persistExecutionContext(any(), any(), any(), any());

        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testConsumer");
        assertNotNull(execution);
        assertTrue(execution.getExecuted());
    }

    @Test
    void execute_WithActionContextValues() {
        @SuppressWarnings("unchecked")
        ConsumerAction<Object> mockAction = mock(ConsumerAction.class);
        when(mockAction.parsePayload(any())).thenReturn("parsedPayload");

        doAnswer((Answer<Void>) invocation -> {
            ActionExecutionContext context = invocation.getArgument(1);
            context.put("keyTest", "valueTest");
            return null;
        }).when(mockAction).consume(any(), any(), any());

        when(actionRegistry.getAction(eq(actionDefinition.getActionName()), any())).thenReturn(mockAction);
        actionDefinition.setFinalAction(true);

        executor.executionActionRequest(flowExecutionActionRequest);

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testConsumer");
        assertNotNull(execution);
        assertNotNull(execution.getContext());
        assertEquals("valueTest", execution.getContext().get("keyTest"));
    }

    @Test
    void execute_ThrowsException() {
        @SuppressWarnings("unchecked")
        ConsumerAction<Object> mockAction = mock(ConsumerAction.class);
        when(mockAction.parsePayload(any())).thenReturn("parsedPayload");

        doThrow(new RuntimeException("Error executing action")).when(mockAction).consume(any(), any(), any());
        when(actionRegistry.getAction(eq(actionDefinition.getActionName()), any())).thenReturn(mockAction);

        executor.executionActionRequest(flowExecutionActionRequest);

        assertEquals(ExecutionStatus.ERROR, executionContext.getFlowExecution().getExecutionStatus());

        io.github.spring.middleware.orchestrator.core.runtime.ActionExecution execution = flowExecution.getActionExecution("testConsumer");
        assertNotNull(execution);
        assertFalse(execution.getExecuted());
        assertEquals("Error executing action", execution.getError());

        verify(flowExecutionRegistry).updateFlowExecution(flowExecution);
    }
}
