package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowExecutorRedirectSchedulerTest {

    @Mock
    private ExecutionContextManager executionContextManager;

    @Mock
    private FlowExecutor flowExecutor;

    @InjectMocks
    private FlowExecutorRedirectScheduler scheduler;

    @Test
    void run_ShouldProcessAllTimeouts_WhenMultipleTimeoutsExist() {
        FlowExecutionTimeout timeout1 = new FlowExecutionTimeout(UUID.randomUUID(), TimeoutDefinition.builder().build());
        FlowExecutionTimeout timeout2 = new FlowExecutionTimeout(UUID.randomUUID(), TimeoutDefinition.builder().build());
        List<FlowExecutionTimeout> timeouts = Arrays.asList(timeout1, timeout2);

        when(executionContextManager.getFlowExecutionTimeouts(any(LocalDateTime.class))).thenReturn(timeouts);

        scheduler.run();

        verify(flowExecutor, times(1)).redirectFlow(timeout1);
        verify(flowExecutor, times(1)).redirectFlow(timeout2);
    }

    @Test
    void run_ShouldDoNothing_WhenNoTimeoutsExist() {
        when(executionContextManager.getFlowExecutionTimeouts(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        scheduler.run();

        verify(flowExecutor, never()).redirectFlow(any());
    }

    @Test
    void run_ShouldContinueProcessing_WhenRedirectFlowThrowsException() {
        FlowExecutionTimeout timeout1 = new FlowExecutionTimeout(UUID.randomUUID(), TimeoutDefinition.builder().build());
        FlowExecutionTimeout timeout2 = new FlowExecutionTimeout(UUID.randomUUID(), TimeoutDefinition.builder().build());
        List<FlowExecutionTimeout> timeouts = Arrays.asList(timeout1, timeout2);

        when(executionContextManager.getFlowExecutionTimeouts(any(LocalDateTime.class))).thenReturn(timeouts);

        doThrow(new RuntimeException("Simulated error")).when(flowExecutor).redirectFlow(timeout1);

        scheduler.run();

        verify(flowExecutor, times(1)).redirectFlow(timeout1);
        verify(flowExecutor, times(1)).redirectFlow(timeout2);
    }
}

