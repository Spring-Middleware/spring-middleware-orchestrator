package io.github.spring.middleware.orchestrator.core.domain.params;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FixedNextActionResolverParams extends NextActionResolverParams {

    private String nextAction;

    @Override
    public NextActionResolverParamsType getNextActionSupplierParamsType() {

        return NextActionResolverParamsType.FIXED;
    }
}
