package io.github.spring.middleware.orchestrator.infra.engine.scheduler;

import io.github.spring.middleware.orchestrator.core.domain.TimeoutDefinition;
import io.github.spring.middleware.orchestrator.core.port.TimeoutScheduler;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecutionTimeout;
import io.github.spring.middleware.orchestrator.infra.engine.registry.PersistedContextDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTimeoutScheduler implements TimeoutScheduler {

    public static final String ERROR_TIMEOUT_RESOLVER = "errorTimeout";

    @Value("${orchestrator.timeout.default-max-seconds-context-persisted:3600}")
    private Long defaultMaxSecondsContextPersisted;


    private ConcurrentHashMap<UUID, PersistedContextDateTime> persistedContextMap = new ConcurrentHashMap<>();

    public void scheduleTimeout(UUID flowExecutionId, TimeoutDefinition timeoutDefinition) {
        PersistedContextDateTime persistedContextDateTime = PersistedContextDateTime.builder()
                .timeoutDefinition(resolveTimeoutDefinition(timeoutDefinition))
                .dateTime(LocalDateTime.now())
                .build();
        persistedContextMap.put(flowExecutionId, persistedContextDateTime);
    }

    public void removeTimeout(UUID flowExecutionId) {
        persistedContextMap.remove(flowExecutionId);
    }


    private TimeoutDefinition resolveTimeoutDefinition(TimeoutDefinition timeout) {

        if (timeout == null) {
            return TimeoutDefinition.builder()
                    .timeoutSeconds(defaultMaxSecondsContextPersisted)
                    .onTimeoutResolver(ERROR_TIMEOUT_RESOLVER) // o lo que uses ahora
                    .build();
        }

        Long timeoutSeconds = Optional.ofNullable(timeout.getTimeoutSeconds())
                .orElse(defaultMaxSecondsContextPersisted);

        String onTimeoutResolver = Optional.ofNullable(timeout.getOnTimeoutResolver())
                .orElse(ERROR_TIMEOUT_RESOLVER);

        return TimeoutDefinition.builder()
                .timeoutSeconds(timeoutSeconds)
                .onTimeoutResolver(onTimeoutResolver)
                .build();
    }

    @Override
    public Collection<FlowExecutionTimeout> getFlowExecutionTimeoutByDateTime(LocalDateTime dateTime) {
        return persistedContextMap.entrySet().stream()
                .filter(entry -> entry.getValue()
                        .getDateTime()
                        .plusSeconds(entry.getValue().getTimeoutDefinition().getTimeoutSeconds())
                        .isBefore(dateTime))
                .map(Map.Entry::getKey)
                .map(this::removeAndBuildTimeout)
                .filter(Objects::nonNull)
                .toList();
    }

    private FlowExecutionTimeout removeAndBuildTimeout(UUID flowExecutionId) {
        PersistedContextDateTime persistedContextDateTime = persistedContextMap.remove(flowExecutionId);
        if (persistedContextDateTime == null) {
            return null;
        }
        return new FlowExecutionTimeout(flowExecutionId, persistedContextDateTime.getTimeoutDefinition());
    }


}

