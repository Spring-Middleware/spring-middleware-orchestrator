package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.ActionType;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.engine.action.FlowExecutionActionRequest;
import io.github.spring.middleware.orchestrator.core.engine.action.exec.FlowActionExecutor;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.port.NextActionResolverRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;
import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowExecutorTest {

    @Mock
    private FlowDefinitionRegistry flowDefinitionRegistry;
    @Mock
    private FlowExecutionRegistry flowExecutionRegistry;
    @Mock
    private ExecutionContextManager executionContextManager;
    @Mock
    private FlowActionExecutor flowActionExecutor;
    @Mock
    private NextActionResolverRegistry nextActionResolverRegistry;
    @Mock
    private Executor flowExecutorTaskExecutor;

    @InjectMocks
    private FlowExecutor flowExecutor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private FlowTrigger flowTrigger;
    private FlowExecution flowExecution;
    private FlowDefinition flowDefinition;
    private ActionDefinition actionDefinition;

    @BeforeEach
    void setUp() {
        flowTrigger = new FlowTrigger();
        flowTrigger.setFlowId("testFlow");
        flowTrigger.setPayload("testPayload");

        flowExecution = new FlowExecution();
        flowExecution.setId(UUID.randomUUID());
        flowExecution.setFlowId(new FlowId("testFlow"));

        flowDefinition = new FlowDefinition();
        flowDefinition.setFlowId(new FlowId("testFlow"));

        actionDefinition = new ActionDefinition();
        actionDefinition.setActionName("firstAction");
        actionDefinition.setActionType(ActionType.FUNCTION);

        flowDefinition.setActions(List.of(actionDefinition));
        flowDefinition.buildActionDefinitionMap();
    }

    @Test
    void testStartFlow_Success() {
        flowDefinition.setFirstAction("firstAction");

        when(flowExecutionRegistry.createFlowExecution(anyString(), any())).thenReturn(flowExecution);
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(flowExecutorTaskExecutor).execute(any(Runnable.class));

        UUID result = flowExecutor.startFlow(flowTrigger);

        assertNotNull(result);
        assertEquals(flowExecution.getId(), result);
        verify(executionContextManager).addExecutionContext(any(ExecutionContext.class));
        verify(flowActionExecutor).execute(any(FlowExecutionActionRequest.class));
    }

    @Test
    void testStartFlow_FlowDefinitionNotFound() {
        when(flowExecutionRegistry.createFlowExecution(anyString(), any())).thenReturn(flowExecution);
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> flowExecutor.startFlow(flowTrigger));
        assertEquals("Flow definition not found for flowId: testFlow", exception.getMessage());
    }

    @Test
    void testStartFlow_FirstActionNotDefined() {
        when(flowExecutionRegistry.createFlowExecution(anyString(), any())).thenReturn(flowExecution);
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> flowExecutor.startFlow(flowTrigger));
        assertEquals("First action not defined for flowId: testFlow", exception.getMessage());
    }

    @Test
    void testResumeFlow_Success() {
        flowExecution.setExecutionStatus(ExecutionStatus.SUSPENDED);
        actionDefinition.setActionType(ActionType.RESUME);
        actionDefinition.setActionName("resumeAction");
        flowDefinition.setActions(List.of(actionDefinition));
        flowDefinition.buildActionDefinitionMap();

        when(flowExecutionRegistry.findById(any(UUID.class))).thenReturn(Optional.of(flowExecution));
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);
        when(executionContextManager.loadExecutionContext(any(), anyBoolean())).thenReturn(new ExecutionContext(flowExecution, "context"));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(flowExecutorTaskExecutor).execute(any(Runnable.class));

        flowExecutor.resumeFlow(flowExecution.getId(), "resumeAction", "context");

        verify(flowActionExecutor).execute(any(FlowExecutionActionRequest.class));
    }

    @Test
    void testResumeFlow_FlowExecutionNotFound() {
        when(flowExecutionRegistry.findById(any(UUID.class))).thenReturn(Optional.empty());

        ActionException exception = assertThrows(ActionException.class, () -> flowExecutor.resumeFlow(UUID.randomUUID(), "resumeAction", "context"));
        assertNotNull(exception.getMessage());
    }

    @Test
    void testResumeFlow_NotSuspended() {
        flowExecution.setExecutionStatus(ExecutionStatus.EXECUTING);
        actionDefinition.setActionType(ActionType.RESUME);
        actionDefinition.setActionName("resumeAction");
        flowDefinition.setActions(List.of(actionDefinition));
        flowDefinition.buildActionDefinitionMap();

        when(flowExecutionRegistry.findById(any(UUID.class))).thenReturn(Optional.of(flowExecution));
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);

        flowExecutor.resumeFlow(flowExecution.getId(), "resumeAction", "context");

        verify(executionContextManager, never()).loadExecutionContext(any(), anyBoolean());
        verify(flowActionExecutor, never()).execute(any());
    }

    @Test
    void testRedirectFlow_Success() {
        flowExecution.setExecutionStatus(ExecutionStatus.SUSPENDED);

        TimeoutDefinition timeoutDefinition = new TimeoutDefinition();
        timeoutDefinition.setResolver("testResolver");
        FlowExecutionTimeout timeout = new FlowExecutionTimeout(flowExecution.getId(), timeoutDefinition);

        NextActionResolver resolver = mock(NextActionResolver.class);
        when(nextActionResolverRegistry.getNextActionResolver("testResolver")).thenReturn(resolver);
        when(flowExecutionRegistry.findById(flowExecution.getId())).thenReturn(Optional.of(flowExecution));
        when(flowDefinitionRegistry.getFlowDefinition(flowExecution.getFlowId())).thenReturn(flowDefinition);

        ExecutionContext context = new ExecutionContext(flowExecution, "payload");
        when(executionContextManager.loadExecutionContext(any(), anyBoolean())).thenReturn(context);

        NextActionResolverResult resolverResult = NextActionResolverResult.builder()
                .nextAction("firstAction")
                .result("newPayload")
                .build();
        when(resolver.resolveNextAction(any(), any(), any())).thenReturn(resolverResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(flowExecutorTaskExecutor).execute(any(Runnable.class));

        flowExecutor.redirectFlow(timeout);

        verify(flowActionExecutor).execute(any(FlowExecutionActionRequest.class));
    }

    @Test
    void testResumeFlow_ActionDefinitionNotFound() {
        flowExecution.setExecutionStatus(ExecutionStatus.SUSPENDED);
        when(flowExecutionRegistry.findById(any(UUID.class))).thenReturn(Optional.of(flowExecution));
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);

        ActionException exception = assertThrows(ActionException.class,
                () -> flowExecutor.resumeFlow(flowExecution.getId(), "unknownAction", "context"));
        assertEquals("Action definition not found for action: unknownAction in flowId: FlowId[value=testFlow]", exception.getMessage());
    }

    @Test
    void testResumeFlow_ActionTypeNotResume() {
        flowExecution.setExecutionStatus(ExecutionStatus.SUSPENDED);
        actionDefinition.setActionType(ActionType.FUNCTION);
        actionDefinition.setActionName("notResumeAction");
        flowDefinition.setActions(List.of(actionDefinition));
        flowDefinition.buildActionDefinitionMap();

        when(flowExecutionRegistry.findById(any(UUID.class))).thenReturn(Optional.of(flowExecution));
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);

        ActionException exception = assertThrows(ActionException.class,
                () -> flowExecutor.resumeFlow(flowExecution.getId(), "notResumeAction", "context"));
        assertEquals("Action definition for action: notResumeAction in flowId: FlowId[value=testFlow] is not of type RESUME", exception.getMessage());
    }

    @Test
    void testRedirectFlow_ResolverNotFound() {
        TimeoutDefinition timeoutDefinition = new TimeoutDefinition();
        timeoutDefinition.setResolver("unknownResolver");
        FlowExecutionTimeout timeout = new FlowExecutionTimeout(flowExecution.getId(), timeoutDefinition);

        when(nextActionResolverRegistry.getNextActionResolver("unknownResolver")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> flowExecutor.redirectFlow(timeout));
        assertEquals("Default timeout redirect resolver not found", exception.getMessage());
    }

    @Test
    void testAsyncExecution_ExceptionCleansUpContext() {
        flowDefinition.setFirstAction("firstAction");

        when(flowExecutionRegistry.createFlowExecution(anyString(), any())).thenReturn(flowExecution);
        when(flowDefinitionRegistry.getFlowDefinition(any(FlowId.class))).thenReturn(flowDefinition);

        // Simular que el ejecutor lanza una excepción
        when(flowActionExecutor.execute(any())).thenThrow(new RuntimeException("Simulated error"));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(flowExecutorTaskExecutor).execute(any(Runnable.class));

        flowExecutor.startFlow(flowTrigger);

        // A pesar de la excepción, el bloque finally debe limpiar el contexto
        verify(executionContextManager).removeExecutionContext(flowExecution.getId());
    }
}
