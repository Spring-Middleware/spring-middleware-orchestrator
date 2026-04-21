package io.github.spring.middleware.orchestrator.demo.flows.resolver;

import io.github.spring.middleware.orchestrator.core.engine.NextActionResolver;
import io.github.spring.middleware.orchestrator.core.engine.NextActionResolverName;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionContext;
import io.github.spring.middleware.orchestrator.core.runtime.NextActionResolverResult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

@NextActionResolverName("PROB_RESOLVER")
public class ProbabilisticResolver implements NextActionResolver<FlowInput, ProbabilisticResolverParameters> {

    private final Random random = new Random();

    @Override
    public ProbabilisticResolverParameters parseParams(Map params) {
        List<?> probabilitiesActins = (List) params.get("probabilitiesActions");
        var parameters = new ProbabilisticResolverParameters();
        probabilitiesActins.stream()
                .map(Map.class::cast)
                .forEach(map -> {
                    String actionName = (String) map.get("actionName");
                    Double probability = (Double) map.get("probability");
                    parameters.addProbabilityAction(new ProbabilityAction(actionName, probability));
                });
        return parameters;
    }


    @Override
    public NextActionResolverResult resolveNextAction(ExecutionContext executionContext, FlowInput flowInput, ProbabilisticResolverParameters nextActionParams) {
        var actionsSortedByProb = nextActionParams.getProbabilityActions().stream().sorted(Comparator.comparing(ProbabilityAction::getProbability).reversed()).toList();
        double rand = random.nextDouble();
        executionContext.getRuntimeContext().put("rand", rand);
        String name = getActionByProbability(actionsSortedByProb, rand);
        String value = flowInput.getValuesByAction().get(name);
        var result = new NextActionResolverResult<String>();
        result.setNextAction(name);
        result.setResult(value);
        return result;
    }


    private String getActionByProbability(List<ProbabilityAction> actionsSortedByProb, double rand) {
        double cumulative = 0.0;
        for (ProbabilityAction action : actionsSortedByProb) {
            cumulative += action.getProbability();
            if (rand <= cumulative) {
                return action.getActionName();
            }
        }
        throw new ActionException(STR."No action selected for random value: \{rand}");
    }

}
