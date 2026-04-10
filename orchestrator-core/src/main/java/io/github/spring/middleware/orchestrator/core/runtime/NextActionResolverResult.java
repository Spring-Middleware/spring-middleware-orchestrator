package io.github.spring.middleware.orchestrator.core.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextActionResolverResult<T> {

    private T result;
    private String nextAction;

}