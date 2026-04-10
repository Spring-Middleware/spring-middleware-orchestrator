package io.github.spring.middleware.orchestrator.infra.config;

import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextRegistry;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextStore;
import io.github.spring.middleware.orchestrator.core.port.TimeoutScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutionContextConfiguration {

    @Bean
    public ExecutionContextManager executionContextManager(
            ExecutionContextRegistry executionContextRegistry,
            ExecutionContextStore executionContextStore,
            TimeoutScheduler timeoutScheduler) {

        return new ExecutionContextManager(
                executionContextRegistry,
                executionContextStore,
                timeoutScheduler
        );
    }
}
