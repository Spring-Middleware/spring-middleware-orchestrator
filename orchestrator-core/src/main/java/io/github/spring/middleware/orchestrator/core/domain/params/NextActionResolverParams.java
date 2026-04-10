package io.github.spring.middleware.orchestrator.core.domain.params;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "nextActionSupplierParamsType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedNextActionResolverParams.class, name = "FIXED"),
        @JsonSubTypes.Type(value = VoidNextActionResolverParams.class, name = "VOID")
})
public abstract class NextActionResolverParams {

    private NextActionResolverParamsType nextActionResolverParamsType;

    public abstract NextActionResolverParamsType getNextActionSupplierParamsType();

    public void setNextActionSupplierParamsType(
            NextActionResolverParamsType nextActionResolverParamsType) {

        this.nextActionResolverParamsType = nextActionResolverParamsType;
    }
}

