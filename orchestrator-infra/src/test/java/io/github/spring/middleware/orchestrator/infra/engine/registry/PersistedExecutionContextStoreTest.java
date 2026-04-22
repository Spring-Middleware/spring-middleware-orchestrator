package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.infra.engine.repository.ExecutionContextPersistedDocument;
import io.github.spring.middleware.orchestrator.infra.engine.repository.MongoExecutionContextPersistedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistedExecutionContextStoreTest {

    @Mock
    private MongoExecutionContextPersistedRepository repository;

    @InjectMocks
    private PersistedExecutionContextStore store;

    private UUID flowExecutionId;
    private FlowExecution<?, ?> flowExecution;
    private ExecutionContext<String> executionContext;

    @BeforeEach
    void setUp() {
        flowExecutionId = UUID.randomUUID();
        flowExecution = mock(FlowExecution.class);
        lenient().when(flowExecution.getId()).thenReturn(flowExecutionId);

        executionContext = new ExecutionContext<>(flowExecution, "initialPayload");
    }

    @Test
    void persistExecutionContext_ShouldPersist_WhenContextNotEmpty() {
        // Arrange
        executionContext.put("key", "value");
        ExecutionContextPersistedDocument savedDoc = ExecutionContextPersistedDocument.builder()
                .id(flowExecutionId)
                .build();

        when(repository.save(any(ExecutionContextPersistedDocument.class))).thenReturn(savedDoc);

        // Act
        store.persistExecutionContext(executionContext, "testAction", "customPayload");

        // Assert
        verify(repository).save(any(ExecutionContextPersistedDocument.class));
    }

    @Test
    void persistExecutionContext_ShouldPersist_WhenPayloadNotNull() {
        // Arrange
        ExecutionContextPersistedDocument savedDoc = ExecutionContextPersistedDocument.builder()
                .id(flowExecutionId)
                .build();

        when(repository.save(any(ExecutionContextPersistedDocument.class))).thenReturn(savedDoc);

        // Act
        store.persistExecutionContext(executionContext, "testAction", "customPayload");

        // Assert
        verify(repository).save(any(ExecutionContextPersistedDocument.class));
    }

    @Test
    void persistExecutionContext_ShouldNotPersist_WhenContextEmptyAndPayloadNull() {
        // Act
        store.persistExecutionContext(executionContext, "testAction", null);

        // Assert
        verify(repository, never()).save(any());
    }

    @Test
    void loadContext_ShouldReturnContext_WhenDocumentExists() {
        // Arrange
        Map<String, Object> storedContextParams = new HashMap<>();
        storedContextParams.put("storedKey", "storedValue");

        ExecutionContextPersistedDocument doc = ExecutionContextPersistedDocument.builder()
                .id(flowExecutionId)
                .actionName("testAction")
                .payload("loadedPayload")
                .context(storedContextParams)
                .creationDateTime(LocalDateTime.now())
                .build();

        when(repository.findById(flowExecutionId)).thenReturn(Optional.of(doc));

        // Act
        @SuppressWarnings("rawtypes")
        ExecutionContext result = store.loadContext(flowExecution);

        // Assert
        assertNotNull(result);
        assertEquals("loadedPayload", result.getPayload());
        assertEquals("storedValue", result.getRuntimeContext().get("storedKey"));
        assertEquals(flowExecution, result.getFlowExecution());
    }

    @Test
    void loadContext_ShouldReturnNull_WhenDocumentDoesNotExist() {
        when(repository.findById(flowExecutionId)).thenReturn(Optional.empty());

        ExecutionContext<?> result = store.loadContext(flowExecution);

        assertNull(result);
    }

    @Test
    void removeContext_ShouldCallDeleteById() {
        // Act
        store.removeContext(flowExecutionId);

        // Assert
        verify(repository).deleteById(flowExecutionId);
    }
}
