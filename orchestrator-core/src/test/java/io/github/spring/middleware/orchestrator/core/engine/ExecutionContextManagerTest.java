package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextRegistry;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextStore;
import io.github.spring.middleware.orchestrator.core.port.TimeoutScheduler;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionContextManagerTest {

    @Mock
    private ExecutionContextRegistry executionContextRegistry;

    @Mock
    private ExecutionContextStore executionContextStore;

    @Mock
    private TimeoutScheduler timeoutScheduler;

    @InjectMocks
    private ExecutionContextManager executionContextManager;

    private UUID flowExecutionId;
    private ExecutionContext<?> executionContext;
    private FlowExecution<?, ?> flowExecution;

    @BeforeEach
    void setUp() {
        flowExecutionId = UUID.randomUUID();
        flowExecution = mock(FlowExecution.class);
        lenient().when(flowExecution.getId()).thenReturn(flowExecutionId);

        executionContext = mock(ExecutionContext.class);
    }

    @Test
    void addExecutionContext() {
        executionContextManager.addExecutionContext(executionContext);
        verify(executionContextRegistry).addExecutionContext(executionContext);
    }

    @Test
    void removeExecutionContext() {
        executionContextManager.removeExecutionContext(flowExecutionId);
        verify(executionContextRegistry).remove(flowExecutionId);
    }

    @Test
    void getExecutionContext() {
        when(executionContextRegistry.get(flowExecutionId)).thenReturn(executionContext);

        ExecutionContext<?> result = executionContextManager.getExecutionContext(flowExecutionId);

        assertSame(executionContext, result);
        verify(executionContextRegistry).get(flowExecutionId);
    }

    @Test
    void getFlowExecutionTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        Collection<FlowExecutionTimeout> timeouts = List.of(mock(FlowExecutionTimeout.class));
        when(timeoutScheduler.getFlowExecutionTimeoutByDateTime(now)).thenReturn(timeouts);

        Collection<FlowExecutionTimeout> result = executionContextManager.getFlowExecutionTimeouts(now);

        assertSame(timeouts, result);
        verify(timeoutScheduler).getFlowExecutionTimeoutByDateTime(now);
    }

    @Test
    void persistExecutionContext_NotFound() {
        when(executionContextRegistry.remove(flowExecutionId)).thenReturn(null);

        executionContextManager.persistExecutionContext(flowExecutionId, null, "action", "payload");

        verify(executionContextStore, never()).persistExecutionContext(any(), any(), any());
        verify(timeoutScheduler, never()).scheduleTimeout(any(), any());
    }

    @Test
    void persistExecutionContext_FoundWithoutTimeout() {
        when(executionContextRegistry.remove(flowExecutionId)).thenReturn(executionContext);

        executionContextManager.persistExecutionContext(flowExecutionId, null, "action", "payload");

        verify(executionContextStore).persistExecutionContext(executionContext, "action", "payload");
        verify(timeoutScheduler, never()).scheduleTimeout(any(), any());
    }

    @Test
    void persistExecutionContext_FoundWithTimeout() {
        when(executionContextRegistry.remove(flowExecutionId)).thenReturn(executionContext);
        TimeoutDefinition timeoutDef = TimeoutDefinition.builder().build();

        executionContextManager.persistExecutionContext(flowExecutionId, timeoutDef, "action", "payload");

        verify(executionContextStore).persistExecutionContext(executionContext, "action", "payload");
        verify(timeoutScheduler).scheduleTimeout(flowExecutionId, timeoutDef);
    }

    @Test
    void loadExecutionContext_NotFound() {
        when(executionContextStore.loadContext(flowExecution)).thenReturn(null);

        ExecutionContext<?> result = executionContextManager.loadExecutionContext(flowExecution, true);

        assertNull(result);
        verify(executionContextStore, never()).removeContext(any());
    }

    @Test
    void loadExecutionContext_FoundNoRemove() {
        when(executionContextStore.loadContext(flowExecution)).thenReturn(executionContext);

        ExecutionContext<?> result = executionContextManager.loadExecutionContext(flowExecution, false);

        assertSame(executionContext, result);
        verify(executionContextStore, never()).removeContext(any());
        verify(timeoutScheduler, never()).removeTimeout(any());
        verify(executionContextRegistry).addExecutionContext(executionContext);
    }

    @Test
    void loadExecutionContext_FoundWithRemove() {
        when(executionContextStore.loadContext(flowExecution)).thenReturn(executionContext);

        ExecutionContext<?> result = executionContextManager.loadExecutionContext(flowExecution, true);

        assertSame(executionContext, result);
        verify(executionContextStore).removeContext(flowExecutionId);
        verify(timeoutScheduler).removeTimeout(flowExecutionId);
        verify(executionContextRegistry).addExecutionContext(executionContext);
    }
}

