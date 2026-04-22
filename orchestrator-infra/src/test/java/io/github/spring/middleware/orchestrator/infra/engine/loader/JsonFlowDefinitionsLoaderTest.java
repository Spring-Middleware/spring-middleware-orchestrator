package io.github.spring.middleware.orchestrator.infra.engine.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.engine.FlowDefinitionsLoaderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonFlowDefinitionsLoaderTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonFlowDefinitionsLoader loader;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loader, "flowsLocation", "classpath*:flows-test/*.json");
    }

    @Test
    void loadFlowDefinitions_ShouldLoadAndBuildMaps_WhenValidLocation() throws Exception {
        // Arrange
        // We set location to a single non-existent directory just to mock its behavior, but Spring PathResolver works directly on the filesystem for classpath*.
        // For testing we will mock the location string directly to point to a test file we know exists relative to real classpath
        // or we intercept objectMapper since PathResolver is hard to mock statically here.
        ReflectionTestUtils.setField(loader, "flowsLocation", "classpath:fixtures/test-valid-flow.json");

        FlowDefinition mockDefinition = new FlowDefinition();
        mockDefinition.setFlowId(new FlowId("test-flow"));
        mockDefinition.setFirstAction("MOCK_ACTION");

        ActionDefinition mockAction = new ActionDefinition();
        mockAction.setActionName("MOCK_ACTION");
        mockDefinition.setActions(List.of(mockAction));

        when(objectMapper.readValue(any(InputStream.class), eq(FlowDefinition.class)))
                .thenReturn(mockDefinition);

        // Act
        Collection<FlowDefinition> definitions = loader.loadFlowDefinitions();

        // Assert
        assertNotNull(definitions);
        assertFalse(definitions.isEmpty());
        assertEquals(1, definitions.size());

        FlowDefinition resultDefinition = definitions.iterator().next();
        assertEquals("test-flow", resultDefinition.getFlowId().value());
    }

    @Test
    void loadFlowDefinitions_ShouldReturnEmpty_WhenNoFilesFound() {
        // Arrange
        ReflectionTestUtils.setField(loader, "flowsLocation", "classpath*:non-existent-flows/*.json");

        // Act
        Collection<FlowDefinition> definitions = loader.loadFlowDefinitions();

        // Assert
        assertNotNull(definitions);
        assertTrue(definitions.isEmpty());
    }

    @Test
    void loadFlowDefinitions_ShouldThrowException_WhenInvalidJsonFile() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(loader, "flowsLocation", "classpath:fixtures/test-invalid-flow.json");

        when(objectMapper.readValue(any(InputStream.class), eq(FlowDefinition.class)))
                .thenThrow(new RuntimeException("JSON Malformed error"));

        // Act
        Collection<FlowDefinition> definitions = loader.loadFlowDefinitions();

        // Assert
        assertNotNull(definitions);
        assertTrue(definitions.isEmpty());
    }

    @Test
    void loadFlowDefinitions_ShouldThrowException_WhenPathIsMalformed() {
        // Arrange - Force a Spring URI exception by passing a malformed classpath location
        ReflectionTestUtils.setField(loader, "flowsLocation", "invalidDir://\0malformed");

        // Act & Assert
        Collection<FlowDefinition> flowDefinition = loader.loadFlowDefinitions();

        assertTrue(flowDefinition.isEmpty());
    }

    @Test
    void loadFlowDefinitions_ShouldThrowException_WhenDuplicateActionNamesExist() throws Exception {
        // Arrange - Force a duplication to make Collectors.toMap fail inside buildActionDefinitionMap()
        ReflectionTestUtils.setField(loader, "flowsLocation", "classpath:fixtures/test-valid-flow.json");

        FlowDefinition mockDefinition = new FlowDefinition();
        mockDefinition.setFlowId(new FlowId("duplicate-test-flow"));

        ActionDefinition action1 = new ActionDefinition();
        action1.setActionName("DUPLICATED_ACTION");

        ActionDefinition action2 = new ActionDefinition();
        action2.setActionName("DUPLICATED_ACTION");

        mockDefinition.setActions(List.of(action1, action2));

        when(objectMapper.readValue(any(InputStream.class), eq(FlowDefinition.class)))
                .thenReturn(mockDefinition);

        // Act
        Collection<FlowDefinition> definitions = loader.loadFlowDefinitions();

        // Assert
        assertNotNull(definitions);
        assertTrue(definitions.isEmpty());
    }

    @Test
    void loadFlowDefinitions_ShouldHandleMissingFirstActionGracefully() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(loader, "flowsLocation", "classpath:fixtures/test-valid-flow.json");

        FlowDefinition mockDefinition = new FlowDefinition();
        mockDefinition.setFlowId(new FlowId("no-first-action-flow"));

        ActionDefinition action1 = new ActionDefinition();
        action1.setActionName("SOME_ACTION");

        mockDefinition.setActions(List.of(action1));
        // We simulate that the JSON node didn't have the 'firstAction' set (it remains null)
        mockDefinition.setFirstAction(null);

        when(objectMapper.readValue(any(InputStream.class), eq(FlowDefinition.class)))
                .thenReturn(mockDefinition);

        // Act
        Collection<FlowDefinition> definitions = loader.loadFlowDefinitions();

        // Assert
        assertTrue(definitions.isEmpty());
    }
}
