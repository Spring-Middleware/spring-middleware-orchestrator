package io.github.spring.middleware.orchestrator.core.engine;

import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExecutorRedirectScheduler {

    private final ExecutionContextManager executionContextManager;
    private final FlowExecutor flowExecutor;

    @Scheduled(initialDelayString = "${orchestrator.redirect-scheduler.initial-delay-string:5000}", fixedRateString = "${orchestrator.redirect-scheduler.fixed-rate-string:30000}")
    public void run() {
        Collection<FlowExecutionTimeout> flowExecutionTimeouts = executionContextManager.getFlowExecutionTimeouts(LocalDateTime.now());
        for (FlowExecutionTimeout flowExecutionTimeout : flowExecutionTimeouts) {
            try {
                flowExecutor.redirectFlow(flowExecutionTimeout);
            } catch (Exception ex) {
                log.error("Error redirecting flow execution on timeout: {}", flowExecutionTimeout.flowExecutionId(), ex);
            }
        }
    }

}
