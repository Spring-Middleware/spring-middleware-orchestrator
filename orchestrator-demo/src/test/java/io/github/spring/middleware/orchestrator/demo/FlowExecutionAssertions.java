package io.github.spring.middleware.orchestrator.demo;


import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionOrder;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public final class FlowExecutionAssertions {

    private FlowExecutionAssertions() {
    }

    public static void assertSimpleFlowExecuted(FlowExecution<?,?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();
        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isAfterOrEqualTo(flowExecution.getStartDateTime());

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("SIMPLE_FLOW");
        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.EXECUTED);

        assertThat(flowExecution.getInput()).isNull();
        assertThat(flowExecution.getContext()).isNull();

        assertThat(flowExecution.getActionExecutions())
                .isNotNull()
                .hasSize(2)
                .containsKeys("FIRST_ACTION", "SECOND_ACTION");

        ActionExecutionOrder firstActionOrder = flowExecution.getActionExecutions().get("FIRST_ACTION");
        assertThat(firstActionOrder).isNotNull();
        assertThat(firstActionOrder.getExecutionOrder()).isEqualTo(1);
        assertThat(firstActionOrder.getActionExecution()).isNotNull();
        assertThat(firstActionOrder.getActionExecution().getActionName()).isEqualTo("FIRST_ACTION");
        assertThat(firstActionOrder.getActionExecution().getExecuted()).isTrue();
        assertThat(firstActionOrder.getActionExecution().getError()).isNull();
        assertThat(firstActionOrder.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(firstActionOrder.getActionExecution().getContext()).isNull();

        ActionExecutionOrder secondActionOrder = flowExecution.getActionExecutions().get("SECOND_ACTION");
        assertThat(secondActionOrder).isNotNull();
        assertThat(secondActionOrder.getExecutionOrder()).isEqualTo(2);
        assertThat(secondActionOrder.getActionExecution()).isNotNull();
        assertThat(secondActionOrder.getActionExecution().getActionName()).isEqualTo("SECOND_ACTION");
        assertThat(secondActionOrder.getActionExecution().getExecuted()).isTrue();
        assertThat(secondActionOrder.getActionExecution().getError()).isNull();
        assertThat(secondActionOrder.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(secondActionOrder.getActionExecution().getContext()).isNull();

        assertThat(firstActionOrder.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(secondActionOrder.getActionExecution().getExecutionDatetime());
    }
}
