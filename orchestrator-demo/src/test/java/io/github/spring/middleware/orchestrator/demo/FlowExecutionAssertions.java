package io.github.spring.middleware.orchestrator.demo;


import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionOrder;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public final class FlowExecutionAssertions {

    private FlowExecutionAssertions() {
    }

    public static void assertSimpleFlowExecuted(FlowExecution<?, ?> flowExecution, UUID executionId) {

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

    public static void assertChainedSuccessFlowExecuted(FlowExecution<?, ?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("CHAINED_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.EXECUTED);
        assertThat(flowExecution.getInput()).isEqualTo("SUCCESS");
        assertThat(flowExecution.getContext()).isNull();

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime())
                .isAfterOrEqualTo(flowExecution.getStartDateTime());

        assertThat(flowExecution.getActionExecutions())
                .isNotNull()
                .hasSize(3)
                .containsKeys(
                        "FIRST_CHAINED_ACTION",
                        "SECOND_CHAINED_ACTION",
                        "THIRD_CHAINED_ACTION"
                );

        ActionExecutionOrder first = flowExecution.getActionExecutions().get("FIRST_CHAINED_ACTION");
        ActionExecutionOrder second = flowExecution.getActionExecutions().get("SECOND_CHAINED_ACTION");
        ActionExecutionOrder third = flowExecution.getActionExecutions().get("THIRD_CHAINED_ACTION");

        // FIRST
        assertThat(first).isNotNull();
        assertThat(first.getExecutionOrder()).isEqualTo(1);
        assertThat(first.getActionExecution().getActionName()).isEqualTo("FIRST_CHAINED_ACTION");
        assertThat(first.getActionExecution().getExecuted()).isTrue();
        assertThat(first.getActionExecution().getError()).isNull();
        assertThat(first.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(first.getActionExecution().getContext()).isNull();

        // SECOND (con contexto)
        assertThat(second).isNotNull();
        assertThat(second.getExecutionOrder()).isEqualTo(2);
        assertThat(second.getActionExecution().getActionName()).isEqualTo("SECOND_CHAINED_ACTION");
        assertThat(second.getActionExecution().getExecuted()).isTrue();
        assertThat(second.getActionExecution().getError()).isNull();
        assertThat(second.getActionExecution().getExecutionDatetime()).isNotNull();

        assertThat(second.getActionExecution().getContext()).isNotNull();
        assertThat(second.getActionExecution().getContext().getData())
                .containsEntry("DATA", "MY_DATA");

        // THIRD
        assertThat(third).isNotNull();
        assertThat(third.getExecutionOrder()).isEqualTo(3);
        assertThat(third.getActionExecution().getActionName()).isEqualTo("THIRD_CHAINED_ACTION");
        assertThat(third.getActionExecution().getExecuted()).isTrue();
        assertThat(third.getActionExecution().getError()).isNull();
        assertThat(third.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(third.getActionExecution().getContext()).isNull();

        // Orden temporal (muy importante en orchestrator)
        assertThat(first.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(second.getActionExecution().getExecutionDatetime());

        assertThat(second.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(third.getActionExecution().getExecutionDatetime());
    }

    public static void assertChainedErrorFlowExecuted(FlowExecution<?, ?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("CHAINED_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.ERROR);
        assertThat(flowExecution.getInput()).isEqualTo("FAIL");
        assertThat(flowExecution.getContext()).isNull();

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();

        assertThat(flowExecution.getActionExecutions())
                .isNotNull()
                .hasSize(2)
                .containsKeys("FIRST_CHAINED_ACTION", "SECOND_CHAINED_ACTION");

        ActionExecutionOrder first = flowExecution.getActionExecutions().get("FIRST_CHAINED_ACTION");
        ActionExecutionOrder second = flowExecution.getActionExecutions().get("SECOND_CHAINED_ACTION");

        // FIRST -> ejecutada OK
        assertThat(first).isNotNull();
        assertThat(first.getExecutionOrder()).isEqualTo(1);
        assertThat(first.getActionExecution().getActionName()).isEqualTo("FIRST_CHAINED_ACTION");
        assertThat(first.getActionExecution().getExecuted()).isTrue();
        assertThat(first.getActionExecution().getError()).isNull();
        assertThat(first.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(first.getActionExecution().getContext()).isNull();

        // SECOND -> falla
        assertThat(second).isNotNull();
        assertThat(second.getExecutionOrder()).isEqualTo(2);
        assertThat(second.getActionExecution().getActionName()).isEqualTo("SECOND_CHAINED_ACTION");
        assertThat(second.getActionExecution().getExecuted()).isFalse(); // clave
        assertThat(second.getActionExecution().getError())
                .isNotNull()
                .contains("Chained action failed");
        assertThat(second.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(second.getActionExecution().getContext()).isNull();

        // Orden temporal
        assertThat(first.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(second.getActionExecution().getExecutionDatetime());
    }


    public static void assertResolverFlowExecuted(FlowExecution<?,?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("RESOLVER_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.EXECUTED);
        assertThat(flowExecution.getContext()).isNull();

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime())
                .isAfterOrEqualTo(flowExecution.getStartDateTime());

        assertThat(flowExecution.getInput()).isNotNull();

        assertThat(flowExecution.getActionExecutions())
                .isNotNull()
                .hasSize(2)
                .containsKey("FIRST_RESOLVER_ACTION");

        ActionExecutionOrder resolverAction = flowExecution.getActionExecutions().get("FIRST_RESOLVER_ACTION");
        assertThat(resolverAction).isNotNull();
        assertThat(resolverAction.getExecutionOrder()).isEqualTo(1);
        assertThat(resolverAction.getActionExecution()).isNotNull();
        assertThat(resolverAction.getActionExecution().getActionName()).isEqualTo("FIRST_RESOLVER_ACTION");
        assertThat(resolverAction.getActionExecution().getExecuted()).isTrue();
        assertThat(resolverAction.getActionExecution().getError()).isNull();
        assertThat(resolverAction.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(resolverAction.getActionExecution().getContext()).isNull();

        boolean firstProbExecuted = flowExecution.getActionExecutions().containsKey("FIRST_PROB_ACTION");
        boolean secondProbExecuted = flowExecution.getActionExecutions().containsKey("SECOND_PROB_ACTION");

        assertThat(firstProbExecuted || secondProbExecuted).isTrue();
        assertThat(firstProbExecuted && secondProbExecuted).isFalse();

        String executedProbActionName = firstProbExecuted ? "FIRST_PROB_ACTION" : "SECOND_PROB_ACTION";
        ActionExecutionOrder probAction = flowExecution.getActionExecutions().get(executedProbActionName);

        assertThat(probAction).isNotNull();
        assertThat(probAction.getExecutionOrder()).isEqualTo(2);
        assertThat(probAction.getActionExecution()).isNotNull();
        assertThat(probAction.getActionExecution().getActionName()).isEqualTo(executedProbActionName);
        assertThat(probAction.getActionExecution().getExecuted()).isTrue();
        assertThat(probAction.getActionExecution().getError()).isNull();
        assertThat(probAction.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(probAction.getActionExecution().getContext()).isNotNull();
        assertThat(probAction.getActionExecution().getContext().getData()).containsKey("rand");

        Object randValue = probAction.getActionExecution().getContext().getData().get("rand");
        assertThat(randValue).isInstanceOf(Number.class);

        double rand = ((Number) randValue).doubleValue();
        assertThat(rand).isBetween(0.0, 1.0);

        if (firstProbExecuted) {
            assertThat(rand).isLessThan(0.7d);
            assertThat(flowExecution.getActionExecutions()).doesNotContainKey("SECOND_PROB_ACTION");
        } else {
            assertThat(rand).isGreaterThanOrEqualTo(0.7d);
            assertThat(flowExecution.getActionExecutions()).doesNotContainKey("FIRST_PROB_ACTION");
        }

        assertThat(resolverAction.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(probAction.getActionExecution().getExecutionDatetime());
    }


    public static void assertResumeFlowExecuted(FlowExecution<?,?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("RESUME_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.EXECUTED);

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime())
                .isAfterOrEqualTo(flowExecution.getStartDateTime());

        assertThat(flowExecution.getContext()).isNull();

        // input
        assertThat(flowExecution.getInput()).isNotNull();
        assertThat(flowExecution.getInput())
                .extracting("key", "resumeType")
                .containsExactly("11111", "RESUME");

        // actions
        assertThat(flowExecution.getActionExecutions())
                .hasSize(4)
                .containsKeys(
                        "CREATE_CONTEXT_ACTION",
                        "SEND_CONTEXT_ACTION",
                        "RESUME_CONTEXT_ACTION",
                        "LAST_CONTEXT_ACTION"
                );

        ActionExecutionOrder create = flowExecution.getActionExecutions().get("CREATE_CONTEXT_ACTION");
        ActionExecutionOrder send = flowExecution.getActionExecutions().get("SEND_CONTEXT_ACTION");
        ActionExecutionOrder resume = flowExecution.getActionExecutions().get("RESUME_CONTEXT_ACTION");
        ActionExecutionOrder last = flowExecution.getActionExecutions().get("LAST_CONTEXT_ACTION");

        // CREATE
        assertThat(create.getExecutionOrder()).isEqualTo(1);
        assertThat(create.getActionExecution().getActionName()).isEqualTo("CREATE_CONTEXT_ACTION");
        assertThat(create.getActionExecution().getExecuted()).isTrue();
        assertThat(create.getActionExecution().getError()).isNull();
        assertThat(create.getActionExecution().getExecutionDatetime()).isNotNull();

        // SEND
        assertThat(send.getExecutionOrder()).isEqualTo(2);
        assertThat(send.getActionExecution().getActionName()).isEqualTo("SEND_CONTEXT_ACTION");
        assertThat(send.getActionExecution().getExecuted()).isTrue();
        assertThat(send.getActionExecution().getError()).isNull();
        assertThat(send.getActionExecution().getExecutionDatetime()).isNotNull();

        // RESUME (nuevo ciclo)
        assertThat(resume.getExecutionOrder()).isEqualTo(1);
        assertThat(resume.getActionExecution().getActionName()).isEqualTo("RESUME_CONTEXT_ACTION");
        assertThat(resume.getActionExecution().getExecuted()).isTrue();
        assertThat(resume.getActionExecution().getError()).isNull();
        assertThat(resume.getActionExecution().getExecutionDatetime()).isNotNull();

        // LAST
        assertThat(last.getExecutionOrder()).isEqualTo(2);
        assertThat(last.getActionExecution().getActionName()).isEqualTo("LAST_CONTEXT_ACTION");
        assertThat(last.getActionExecution().getExecuted()).isTrue();
        assertThat(last.getActionExecution().getError()).isNull();
        assertThat(last.getActionExecution().getExecutionDatetime()).isNotNull();

        // 🔥 orden temporal (clave en resume flows)
        assertThat(create.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(send.getActionExecution().getExecutionDatetime());

        assertThat(send.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(resume.getActionExecution().getExecutionDatetime());

        assertThat(resume.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(last.getActionExecution().getExecutionDatetime());
    }

    public static void assertResumeFlowSuspended(FlowExecution<?,?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("RESUME_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.SUSPENDED);

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNull(); // clave en suspended

        assertThat(flowExecution.getContext()).isNull();

        // input
        assertThat(flowExecution.getInput()).isNotNull();
        assertThat(flowExecution.getInput())
                .extracting("key", "resumeType")
                .containsExactly("22222", "NOT_RESUME");

        // actions (solo primer ciclo)
        assertThat(flowExecution.getActionExecutions())
                .hasSize(2)
                .containsKeys(
                        "CREATE_CONTEXT_ACTION",
                        "SEND_CONTEXT_ACTION"
                );

        ActionExecutionOrder create = flowExecution.getActionExecutions().get("CREATE_CONTEXT_ACTION");
        ActionExecutionOrder send = flowExecution.getActionExecutions().get("SEND_CONTEXT_ACTION");

        // CREATE
        assertThat(create).isNotNull();
        assertThat(create.getExecutionOrder()).isEqualTo(1);
        assertThat(create.getActionExecution().getActionName()).isEqualTo("CREATE_CONTEXT_ACTION");
        assertThat(create.getActionExecution().getExecuted()).isTrue();
        assertThat(create.getActionExecution().getError()).isNull();
        assertThat(create.getActionExecution().getExecutionDatetime()).isNotNull();

        // SEND
        assertThat(send).isNotNull();
        assertThat(send.getExecutionOrder()).isEqualTo(2);
        assertThat(send.getActionExecution().getActionName()).isEqualTo("SEND_CONTEXT_ACTION");
        assertThat(send.getActionExecution().getExecuted()).isTrue();
        assertThat(send.getActionExecution().getError()).isNull();
        assertThat(send.getActionExecution().getExecutionDatetime()).isNotNull();

        // orden temporal
        assertThat(create.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(send.getActionExecution().getExecutionDatetime());

        // 🔥 clave: no hay acciones de resume todavía
        assertThat(flowExecution.getActionExecutions())
                .doesNotContainKeys("RESUME_CONTEXT_ACTION", "LAST_CONTEXT_ACTION");
    }

    public static void assertTimeoutErrorFlowExecuted(FlowExecution<?,?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("TIMEOUT_ERROR_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.ERROR);

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime())
                .isAfterOrEqualTo(flowExecution.getStartDateTime());

        assertThat(flowExecution.getContext()).isNull();

        assertThat(flowExecution.getInput()).isNotNull();
        assertThat(flowExecution.getInput())
                .extracting("key", "resumeType")
                .containsExactly("11111", "NOT_RESUME");

        assertThat(flowExecution.getActionExecutions())
                .hasSize(3)
                .containsKeys("CREATE_CONTEXT_ACTION", "SEND_CONTEXT_ACTION", "ERROR");

        ActionExecutionOrder create = flowExecution.getActionExecutions().get("CREATE_CONTEXT_ACTION");
        ActionExecutionOrder send = flowExecution.getActionExecutions().get("SEND_CONTEXT_ACTION");
        ActionExecutionOrder error = flowExecution.getActionExecutions().get("ERROR");

        assertThat(create).isNotNull();
        assertThat(create.getExecutionOrder()).isEqualTo(1);
        assertThat(create.getActionExecution().getActionName()).isEqualTo("CREATE_CONTEXT_ACTION");
        assertThat(create.getActionExecution().getExecuted()).isTrue();
        assertThat(create.getActionExecution().getError()).isNull();
        assertThat(create.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(create.getActionExecution().getContext()).isNull();

        assertThat(send).isNotNull();
        assertThat(send.getExecutionOrder()).isEqualTo(2);
        assertThat(send.getActionExecution().getActionName()).isEqualTo("SEND_CONTEXT_ACTION");
        assertThat(send.getActionExecution().getExecuted()).isTrue();
        assertThat(send.getActionExecution().getError()).isNull();
        assertThat(send.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(send.getActionExecution().getContext()).isNull();

        assertThat(error).isNotNull();
        assertThat(error.getExecutionOrder()).isEqualTo(1);
        assertThat(error.getActionExecution().getActionName()).isEqualTo("ERROR");
        assertThat(error.getActionExecution().getExecuted()).isFalse();
        assertThat(error.getActionExecution().getError()).isEqualTo("Error action executed");
        assertThat(error.getActionExecution().getExecutionDatetime()).isNull();
        assertThat(error.getActionExecution().getContext()).isNull();

        assertThat(create.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(send.getActionExecution().getExecutionDatetime());

        assertThat(flowExecution.getEndDateTime())
                .isAfterOrEqualTo(send.getActionExecution().getExecutionDatetime());
    }

    public static void assertTimeoutEndFlowExecuted(FlowExecution<?,?> flowExecution, UUID executionId) {

        assertThat(flowExecution).isNotNull();
        assertThat(flowExecution.getId()).isEqualTo(executionId);
        assertThat(flowExecution.getRequestId()).isNotBlank();

        assertThat(flowExecution.getFlowId()).isNotNull();
        assertThat(flowExecution.getFlowId().value()).isEqualTo("TIMEOUT_END_FLOW");

        assertThat(flowExecution.getExecutionStatus()).isEqualTo(ExecutionStatus.EXECUTED);

        assertThat(flowExecution.getStartDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime()).isNotNull();
        assertThat(flowExecution.getEndDateTime())
                .isAfterOrEqualTo(flowExecution.getStartDateTime());

        assertThat(flowExecution.getContext()).isNull();

        assertThat(flowExecution.getInput()).isNotNull();
        assertThat(flowExecution.getInput())
                .extracting("key", "resumeType")
                .containsExactly("22222", "NOT_RESUME");

        assertThat(flowExecution.getActionExecutions())
                .hasSize(3)
                .containsKeys("CREATE_CONTEXT_ACTION", "SEND_CONTEXT_ACTION", "END");

        ActionExecutionOrder create = flowExecution.getActionExecutions().get("CREATE_CONTEXT_ACTION");
        ActionExecutionOrder send = flowExecution.getActionExecutions().get("SEND_CONTEXT_ACTION");
        ActionExecutionOrder end = flowExecution.getActionExecutions().get("END");

        assertThat(create).isNotNull();
        assertThat(create.getExecutionOrder()).isEqualTo(1);
        assertThat(create.getActionExecution().getActionName()).isEqualTo("CREATE_CONTEXT_ACTION");
        assertThat(create.getActionExecution().getExecuted()).isTrue();
        assertThat(create.getActionExecution().getError()).isNull();
        assertThat(create.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(create.getActionExecution().getContext()).isNull();

        assertThat(send).isNotNull();
        assertThat(send.getExecutionOrder()).isEqualTo(2);
        assertThat(send.getActionExecution().getActionName()).isEqualTo("SEND_CONTEXT_ACTION");
        assertThat(send.getActionExecution().getExecuted()).isTrue();
        assertThat(send.getActionExecution().getError()).isNull();
        assertThat(send.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(send.getActionExecution().getContext()).isNull();

        assertThat(end).isNotNull();
        assertThat(end.getExecutionOrder()).isEqualTo(1);
        assertThat(end.getActionExecution().getActionName()).isEqualTo("END");
        assertThat(end.getActionExecution().getExecuted()).isTrue();
        assertThat(end.getActionExecution().getError()).isNull();
        assertThat(end.getActionExecution().getExecutionDatetime()).isNotNull();
        assertThat(end.getActionExecution().getContext()).isNull();

        assertThat(create.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(send.getActionExecution().getExecutionDatetime());

        assertThat(send.getActionExecution().getExecutionDatetime())
                .isBeforeOrEqualTo(end.getActionExecution().getExecutionDatetime());

        assertThat(flowExecution.getEndDateTime())
                .isEqualTo(end.getActionExecution().getExecutionDatetime());
    }







}
