package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.engine.ResolverName;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutRedirectResolver;
import io.github.spring.middleware.orchestrator.core.engine.TimeoutRedirectResolverRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultTimeoutRedirectResolverRegistry implements TimeoutRedirectResolverRegistry {

    private final Map<String, TimeoutRedirectResolver> resolvers;

    public DefaultTimeoutRedirectResolverRegistry(List<TimeoutRedirectResolver> resolverList) {
        this.resolvers = resolverList.stream()
                .collect(Collectors.toMap(
                        r -> resolveName(r),
                        Function.identity()
                ));
    }

    private String resolveName(TimeoutRedirectResolver resolver) {
        ResolverName annotation = resolver.getClass().getAnnotation(ResolverName.class);
        return annotation.value();
    }

    @Override
    public TimeoutRedirectResolver get(String name) {
        return resolvers.get(name);
    }

}
