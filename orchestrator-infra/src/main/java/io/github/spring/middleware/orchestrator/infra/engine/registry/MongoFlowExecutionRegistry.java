package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.config.PropertyNames;
import io.github.spring.middleware.orchestrator.core.domain.FlowId;
import io.github.spring.middleware.orchestrator.core.engine.FlowExecutionFactory;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.infra.engine.repository.FlowExecutionDocument;
import io.github.spring.middleware.orchestrator.infra.engine.repository.MongoFlowExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MongoFlowExecutionRegistry implements FlowExecutionRegistry {

    private final MongoFlowExecutionRepository mongoFlowExecutionRepository;
    private final FlowExecutionFactory flowExecutionFactory;

    @Override
    public Optional<FlowExecution> findById(UUID flowExecutionId) {
        return mongoFlowExecutionRepository.findById(flowExecutionId)
                .map(this::toFlowExecution);
    }

    @Override
    public <T> FlowExecution createFlowExecution(String flowId, T context) {
        String requestId = MDC.get(PropertyNames.REQUEST_ID);
        FlowExecution flowExecution = flowExecutionFactory.createFlowExecution(new FlowId(flowId), context, requestId);
        FlowExecutionDocument flowExecutionDocument = toFlowExecutionDocument(flowExecution);
        mongoFlowExecutionRepository.save(flowExecutionDocument);
        return flowExecution;
    }

    @Override
    public FlowExecution addActionExecutionToFlowExecution(UUID flowExecutionId, ActionExecution actionExecution) {
        FlowExecutionDocument document = mongoFlowExecutionRepository.findById(flowExecutionId)
                .orElseThrow(() -> new IllegalStateException(
                        STR."FlowExecution not found with id: \{flowExecutionId}"));
        FlowExecution flowExecution = toFlowExecution(document);
        flowExecution.addActionExecution(actionExecution);
        FlowExecutionDocument updatedDocument = toFlowExecutionDocument(flowExecution);
        mongoFlowExecutionRepository.save(updatedDocument);
        return flowExecution;
    }


    private FlowExecutionDocument toFlowExecutionDocument(FlowExecution flowExecution) {
        return FlowExecutionDocument.builder()
                .id(flowExecution.getId())
                .flowId(flowExecution.getFlowId().value())
                .context(flowExecution.getContext())
                .startDateTime(flowExecution.getStartDateTime())
                .endDateTime(flowExecution.getEndDateTime())
                .requestId(flowExecution.getRequestId())
                .executionStatus(flowExecution.getExecutionStatus())
                .actionExecutions(flowExecution.getActionExecutions())
                .build();
    }

    private FlowExecution toFlowExecution(FlowExecutionDocument flowExecutionDocument) {
        return FlowExecution.builder()
                .id(flowExecutionDocument.getId())
                .flowId(new FlowId(flowExecutionDocument.getFlowId()))
                .context(flowExecutionDocument.getContext())
                .startDateTime(flowExecutionDocument.getStartDateTime())
                .endDateTime(flowExecutionDocument.getEndDateTime())
                .requestId(flowExecutionDocument.getRequestId())
                .executionStatus(flowExecutionDocument.getExecutionStatus())
                .actionExecutions(flowExecutionDocument.getActionExecutions())
                .build();
    }

}
