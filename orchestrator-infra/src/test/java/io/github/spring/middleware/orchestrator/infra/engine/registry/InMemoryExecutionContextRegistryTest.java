package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryExecutionContextRegistryTest {

    private InMemoryExecutionContextRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryExecutionContextRegistry();
    }

    @Test
    void shouldAddAndRetrieveExecutionContext() {
        UUID executionId = UUID.randomUUID();
        FlowExecution<?, ?> mockFlowExecution = mock(FlowExecution.class);
        when(mockFlowExecution.getId()).thenReturn(executionId);

        @SuppressWarnings("unchecked")
        ExecutionContext<Object> mockContext = mock(ExecutionContext.class);
        when(mockContext.getFlowExecution()).thenReturn(mockFlowExecution);

        registry.addExecutionContext(mockContext);

        ExecutionContext<?> retrievedContext = registry.get(executionId);
        assertEquals(mockContext, retrievedContext);
    }

    @Test
    void shouldRemoveExecutionContext() {
        UUID executionId = UUID.randomUUID();
        FlowExecution<?, ?> mockFlowExecution = mock(FlowExecution.class);
        when(mockFlowExecution.getId()).thenReturn(executionId);

        @SuppressWarnings("unchecked")
        ExecutionContext<Object> mockContext = mock(ExecutionContext.class);
        when(mockContext.getFlowExecution()).thenReturn(mockFlowExecution);

        registry.addExecutionContext(mockContext);

        ExecutionContext<?> removedContext = registry.remove(executionId);
        assertEquals(mockContext, removedContext);

        assertNull(registry.get(executionId));
    }

    @Test
    void shouldReturnNullWhenGettingNonExistentContext() {
        assertNull(registry.get(UUID.randomUUID()));
    }

    @Test
    void shouldReturnNullWhenRemovingNonExistentContext() {
        assertNull(registry.remove(UUID.randomUUID()));
    }
}

