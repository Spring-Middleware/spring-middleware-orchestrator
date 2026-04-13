package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.engine.TimeoutRedirectResolver;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutRedirectResolverRegistry;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutResolverName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultTimeoutRedirectResolverRegistry implements TimeoutRedirectResolverRegistry {

    private final Map<String, TimeoutRedirectResolver> resolvers;

    private static final String DEFAULT_TIMEOUT_REDIRECT_RESOLVER = "errorTimeout";

    public DefaultTimeoutRedirectResolverRegistry(List<TimeoutRedirectResolver> resolverList) {
        this.resolvers = resolverList.stream()
                .collect(Collectors.toMap(
                        r -> resolveName(r),
                        Function.identity()
                ));
    }

    private String resolveName(TimeoutRedirectResolver resolver) {
        TimeoutResolverName annotation = resolver.getClass().getAnnotation(TimeoutResolverName.class);
        return annotation.value();
    }

    @Override
    public TimeoutRedirectResolver get(String name) {
        return Optional.ofNullable(resolvers.get(name)).orElse(resolvers.get(DEFAULT_TIMEOUT_REDIRECT_RESOLVER));
    }

}
