package io.github.spring.middleware.orchestrator.demo.flows.resume;

import io.github.spring.middleware.kafka.api.interf.KafkaPublisher;
import io.github.spring.middleware.kafka.core.registry.KafkaPublisherRegistry;
import io.github.spring.middleware.orchestrator.core.engine.action.ActionName;
import io.github.spring.middleware.orchestrator.core.engine.action.ConsumerAction;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ActionExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActionName("SEND_CONTEXT_ACTION")
@RequiredArgsConstructor
public class SendContextAction implements ConsumerAction<ContextPayload> {

    private final KafkaPublisherRegistry kafkaPublisherRegistry;


    @Override
    public void consume(ExecutionContext executionContext, ActionExecutionContext actionContext, ContextPayload contextPayload) throws ActionException {
        contextPayload.setFlowExecutionId(executionContext.getFlowExecution().getId());
        KafkaPublisher<ContextEvent, String> publisher = kafkaPublisherRegistry.getPublisher("orchestrator-publisher");
        publisher.publishWithKey(new ContextEvent(executionContext.getFlowExecution().getId(), contextPayload.getResumeType()), contextPayload.getKey()).thenAccept(result -> {
            actionContext.put("eventId", result.getEvent().getEventId());
        }).exceptionally(ex -> {
            log.error("Failed to publish message to Kafka topic", ex);
            return null;
        });
    }
}
