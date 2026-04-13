package io.github.spring.middleware.orchestrator.core.domain.params;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FixedNextActionResolverParams implements NextActionResolverParams {

    private String nextAction;


}
