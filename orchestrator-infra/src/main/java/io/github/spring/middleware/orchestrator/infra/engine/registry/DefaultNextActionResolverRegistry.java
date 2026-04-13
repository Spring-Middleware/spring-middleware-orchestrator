package io.github.spring.middleware.orchestrator.infra.engine.registry;

import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolverName;
import io.github.spring.middleware.orchestrator.core.port.NextActionResolverRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultNextActionResolverRegistry<T, P extends NextActionResolverParams> implements NextActionResolverRegistry<T, P> {

    private final List<NextActionResolver<?, ?>> nextActionResolvers;

    private final Map<String, NextActionResolver<?, ?>> resolversByName = new HashMap<>();

    @PostConstruct
    public void init() {
        for (NextActionResolver<?, ?> resolver : nextActionResolvers) {
            Class<?> resolverClass = resolver.getClass();

            NextActionResolverName annotation = resolverClass.getAnnotation(NextActionResolverName.class);
            if (annotation == null) {
                throw new IllegalStateException(
                        STR."NextActionResolver \{resolverClass.getName()} must be annotated with @NextActionResolverName");
            }

            String resolverName = annotation.value();

            if (resolversByName.containsKey(resolverName)) {
                throw new IllegalStateException(
                        STR."Duplicate NextActionResolverName detected: \{resolverName}");
            }

            resolversByName.put(resolverName.toUpperCase(), resolver);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public NextActionResolver<T, P> getNextActionResolver(String resolverName) {
        NextActionResolver<?, ?> resolver = resolversByName.get(resolverName.toUpperCase());

        if (resolver == null) {
            throw new IllegalArgumentException(
                    STR."No NextActionResolver found for name: \{resolverName}");
        }

        return (NextActionResolver<T, P>) resolver;
    }
}