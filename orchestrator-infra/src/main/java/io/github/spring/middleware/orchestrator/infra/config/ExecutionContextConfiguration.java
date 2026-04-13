package io.github.spring.middleware.orchestrator.infra.config;

import io.github.spring.middleware.orchestrator.core.engine.ExecutionContextManager;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextRegistry;
import io.github.spring.middleware.orchestrator.core.port.ExecutionContextStore;
import io.github.spring.middleware.orchestrator.core.port.TimeoutScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

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

    @Bean
    @Primary
    public Executor flowExecutorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("flow-exec-");
        executor.initialize();
        return executor;
    }

}
