package io.github.spring.middleware.orchestrator.core.engine.action;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.domain.NextActionDefinition;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;
import io.github.spring.middleware.orchestrator.core.port.NextActionResolverRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionNextAction;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;
import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowNextActionResolverTest {

    @Mock
    private NextActionResolverRegistry nextActionResolverRegistry;

    @InjectMocks
    private FlowNextActionResolver flowNextActionResolver;

    private FlowDefinition flowDefinition;

    @BeforeEach
    void setUp() {
        flowDefinition = mock(FlowDefinition.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void getNextAction_ShouldResolveSuccessfully_WhenActionValid() {
        String currentActionName = "CURRENT_ACTION";
        String nextActionName = "NEXT_ACTION";
        String resolverName = "TEST_RESOLVER";

        ExecutionContext executionContext = mock(ExecutionContext.class);

        ActionDefinition currentActionDef = new ActionDefinition();
        currentActionDef.setActionName(currentActionName);

        NextActionDefinition nextActionDef = new NextActionDefinition();
        nextActionDef.setResolver(resolverName);
        Map<String, Object> params = new HashMap<>();
        params.put("param1", "val1");
        nextActionDef.setParameters(params);
        currentActionDef.setNextAction(nextActionDef);

        ActionDefinition nextResolvedActionDef = new ActionDefinition();
        nextResolvedActionDef.setActionName(nextActionName);

        when(flowDefinition.getAction(currentActionName)).thenReturn(currentActionDef);
        when(flowDefinition.hasAction(nextActionName)).thenReturn(true);
        when(flowDefinition.getAction(nextActionName)).thenReturn(nextResolvedActionDef);

        NextActionResolver resolverMock = mock(NextActionResolver.class);
        NextActionResolverParams parsedParams = mock(NextActionResolverParams.class);

        when(nextActionResolverRegistry.getNextActionResolver(resolverName)).thenReturn(resolverMock);
        when(resolverMock.parseParams(params)).thenReturn(parsedParams);

        NextActionResolverResult resolverResult = NextActionResolverResult.builder()
                .nextAction(nextActionName)
                .result("payloadResult")
                .build();

        when(resolverMock.resolveNextAction(eq(executionContext), eq("actionResult"), eq(parsedParams)))
                .thenReturn(resolverResult);

        FlowExecutionNextAction nextActionResponse = flowNextActionResolver.getNextAction(
                flowDefinition, executionContext, currentActionName, "actionResult"
        );

        assertNotNull(nextActionResponse);
        assertEquals(nextResolvedActionDef, nextActionResponse.getActionDefinition());
        assertEquals(resolverResult, nextActionResponse.getNextActionResolverResult());
    }

    @Test
    void getNextAction_ShouldThrowException_WhenCurrentActionNotFound() {
        ExecutionContext<?> executionContext = mock(ExecutionContext.class);
        when(flowDefinition.getAction("MISSING_ACTION")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            flowNextActionResolver.getNextAction(flowDefinition, executionContext, "MISSING_ACTION", "payload")
        );

        assertEquals("Action 'MISSING_ACTION' not found in flow definition", exception.getMessage());
    }

    @Test
    void getNextAction_ShouldThrowException_WhenNextActionDefinitionNotFound() {
        ExecutionContext<?> executionContext = mock(ExecutionContext.class);
        ActionDefinition currentActionDef = new ActionDefinition();
        currentActionDef.setActionName("CURRENT");
        currentActionDef.setNextAction(null);

        when(flowDefinition.getAction("CURRENT")).thenReturn(currentActionDef);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            flowNextActionResolver.getNextAction(flowDefinition, executionContext, "CURRENT", "payload")
        );

        assertEquals("Next action definition not found for action 'CURRENT'", exception.getMessage());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void getNextAction_ShouldThrowException_WhenNextTargetActionDoesNotExistInDefinition() {
        ExecutionContext executionContext = mock(ExecutionContext.class);
        ActionDefinition currentActionDef = new ActionDefinition();
        currentActionDef.setActionName("CURRENT");
        NextActionDefinition nextActionDef = new NextActionDefinition();
        nextActionDef.setResolver("RESOLVER");
        currentActionDef.setNextAction(nextActionDef);

        when(flowDefinition.getAction("CURRENT")).thenReturn(currentActionDef);

        NextActionResolver resolverMock = mock(NextActionResolver.class);
        when(nextActionResolverRegistry.getNextActionResolver("RESOLVER")).thenReturn(resolverMock);
        when(resolverMock.parseParams(any())).thenReturn(null);

        NextActionResolverResult resolverResult = NextActionResolverResult.builder()
                .nextAction("TARGET_ACTION")
                .build();
        when(resolverMock.resolveNextAction(any(), any(), any())).thenReturn(resolverResult);

        when(flowDefinition.hasAction("TARGET_ACTION")).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            flowNextActionResolver.getNextAction(flowDefinition, executionContext, "CURRENT", "payload")
        );

        assertEquals("Next action 'TARGET_ACTION' not found in flow definition", exception.getMessage());
    }
}

