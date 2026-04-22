package io.github.spring.middleware.orchestrator.infra.engine.scheduler;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTimeoutSchedulerTest {

    private InMemoryTimeoutScheduler timeoutScheduler;
    private final Long defaultTimeout = 3600L;

    @BeforeEach
    void setUp() {
        timeoutScheduler = new InMemoryTimeoutScheduler();
        ReflectionTestUtils.setField(timeoutScheduler, "defaultMaxSecondsContextPersisted", defaultTimeout);
    }

    @Test
    void scheduleTimeout_ShouldStoreWithDefaultValues_WhenBasicTimeoutPassed() {
        UUID executionId = UUID.randomUUID();
        TimeoutDefinition definition = TimeoutDefinition.builder().build();

        timeoutScheduler.scheduleTimeout(executionId, definition);

        // Advance simulated time directly checking via the get method
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(defaultTimeout + 1);
        Collection<FlowExecutionTimeout> timeouts = timeoutScheduler.getFlowExecutionTimeoutByDateTime(futureTime);

        assertFalse(timeouts.isEmpty());
        assertEquals(1, timeouts.size());

        FlowExecutionTimeout timeoutResult = timeouts.iterator().next();
        assertEquals(executionId, timeoutResult.flowExecutionId());
        assertEquals(defaultTimeout, timeoutResult.timeoutDefinition().getSeconds());
        assertEquals(InMemoryTimeoutScheduler.DEFAULT_ACTION_TIMEOUT_RESOLVER, timeoutResult.timeoutDefinition().getResolver());
    }

    @Test
    void scheduleTimeout_ShouldStoreWithCustomValues_WhenTimeoutHasCustomValues() {
        UUID executionId = UUID.randomUUID();
        Map<String, Object> params = new HashMap<>();
        params.put("customParam", "value");

        TimeoutDefinition definition = TimeoutDefinition.builder()
                .seconds(10L)
                .resolver("CUSTOM_RESOLVER")
                .removeContextOnLoad(true)
                .parameters(params)
                .build();

        timeoutScheduler.scheduleTimeout(executionId, definition);

        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(11L);
        Collection<FlowExecutionTimeout> timeouts = timeoutScheduler.getFlowExecutionTimeoutByDateTime(futureTime);

        assertFalse(timeouts.isEmpty());
        FlowExecutionTimeout timeoutResult = timeouts.iterator().next();

        assertEquals(executionId, timeoutResult.flowExecutionId());
        assertEquals(10L, timeoutResult.timeoutDefinition().getSeconds());
        assertEquals("CUSTOM_RESOLVER", timeoutResult.timeoutDefinition().getResolver());
        assertTrue(timeoutResult.timeoutDefinition().isRemoveContextOnLoad());
        assertEquals(params, timeoutResult.timeoutDefinition().getParameters());
    }

    @Test
    void scheduleTimeout_ShouldThrowException_WhenTimeoutDefinitionIsNull() {
        UUID executionId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            timeoutScheduler.scheduleTimeout(executionId, null)
        );

        assertEquals("TimeoutDefinition cannot be null", exception.getMessage());
    }

    @Test
    void removeTimeout_ShouldRemoveStoredTimeout() {
        UUID executionId = UUID.randomUUID();
        TimeoutDefinition definition = TimeoutDefinition.builder().seconds(5L).build();

        timeoutScheduler.scheduleTimeout(executionId, definition);
        timeoutScheduler.removeTimeout(executionId);

        // Should return empty even if time passed because it was removed
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(10L);
        Collection<FlowExecutionTimeout> timeouts = timeoutScheduler.getFlowExecutionTimeoutByDateTime(futureTime);

        assertTrue(timeouts.isEmpty());
    }

    @Test
    void getFlowExecutionTimeoutByDateTime_ShouldNotReturnTimeouts_WhenTimeNotPassed() {
        UUID executionId = UUID.randomUUID();
        TimeoutDefinition definition = TimeoutDefinition.builder().seconds(100L).build();

        timeoutScheduler.scheduleTimeout(executionId, definition);

        // Before timeout
        LocalDateTime beforeTime = LocalDateTime.now().plusSeconds(50L);
        Collection<FlowExecutionTimeout> timeouts = timeoutScheduler.getFlowExecutionTimeoutByDateTime(beforeTime);

        assertTrue(timeouts.isEmpty());
    }

    @Test
    void getFlowExecutionTimeoutByDateTime_ShouldRemoveTimeoutsAfterReturningThem() {
        UUID executionId = UUID.randomUUID();
        TimeoutDefinition definition = TimeoutDefinition.builder().seconds(10L).build();

        timeoutScheduler.scheduleTimeout(executionId, definition);

        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(15L);

        // First call retrieves and removes it
        Collection<FlowExecutionTimeout> firstCallTimeouts = timeoutScheduler.getFlowExecutionTimeoutByDateTime(futureTime);
        assertEquals(1, firstCallTimeouts.size());

        // Second call should be empty
        Collection<FlowExecutionTimeout> secondCallTimeouts = timeoutScheduler.getFlowExecutionTimeoutByDateTime(futureTime);
        assertTrue(secondCallTimeouts.isEmpty());
    }
}

