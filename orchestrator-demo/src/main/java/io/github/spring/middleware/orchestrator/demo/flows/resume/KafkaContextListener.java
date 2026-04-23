package io.github.spring.middleware.orchestrator.demo.flows.resume;

import io.github.spring.middleware.kafka.api.annotations.MiddlewareKafkaListener;
import io.github.spring.middleware.kafka.api.data.EventEnvelope;
import io.github.spring.middleware.orchestrator.core.engine.FlowExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaContextListener {

    private final FlowExecutor flowExecutor;

    @MiddlewareKafkaListener("orchestrator-subscriber")
    public void handleContextPayload(EventEnvelope<ContextEvent> eventEnvelope) {
        log.info("Received Kafka event with payload: {}", eventEnvelope.getPayload());
        ContextEvent contextEvent = eventEnvelope.getPayload();
        if (contextEvent.getResumeType() == ResumeType.RESUME) {
            flowExecutor.resumeFlow(contextEvent.getFlowExecutionId(), "RESUME_CONTEXT_ACTION");
        }
    }
}


