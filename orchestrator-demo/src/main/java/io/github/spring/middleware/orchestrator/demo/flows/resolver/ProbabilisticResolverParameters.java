package io.github.spring.middleware.orchestrator.demo.flows.resolver;

import io.github.spring.middleware.orchestrator.core.domain.params.NextActionResolverParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProbabilisticResolverParameters implements NextActionResolverParams {

    private List<ProbabilityAction> probabilityActions = new ArrayList<>();

    public void addProbabilityAction(ProbabilityAction probabilityAction) {
        this.probabilityActions.add(probabilityAction);
    }

}


