package io.github.spring.middleware.orchestrator.core.engine.commons;

import io.github.spring.middleware.orchestrator.core.domain.params.FixedNextActionResolverParams;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolverName;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NextActionResolverName("FIXED_NEXT_ACTION")
public class FixedNextActionResolver<T> implements NextActionResolver<T, FixedNextActionResolverParams> {


    @Override
    public FixedNextActionResolverParams parseParams(Map<String, Object> params) {
        String nextAction = (String) params.get("nextAction");
        if (nextAction == null) {
            throw new IllegalArgumentException("Missing required parameter: nextAction");
        }
        return new FixedNextActionResolverParams(nextAction);
    }

    @Override
    public NextActionResolverResult resolveNextAction(ExecutionContext executionContext, T t, FixedNextActionResolverParams params) {
        return NextActionResolverResult.builder()
                .nextAction(params.getNextAction())
                .result(t)
                .build();
    }
}
