package io.github.spring.middleware.orchestrator.core.domain.params;

public class VoidNextActionResolverParams extends NextActionResolverParams {


    @Override
    public NextActionResolverParamsType getNextActionSupplierParamsType() {

        return NextActionResolverParamsType.VOID;
    }
}
