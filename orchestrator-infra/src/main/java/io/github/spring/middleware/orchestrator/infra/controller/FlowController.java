package io.github.spring.middleware.orchestrator.infra.controller;

import io.github.spring.middleware.orchestrator.core.engine.FlowExecutor;
import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flows")
public class FlowController {

    private final FlowExecutor flowExecutor;
    private final FlowExecutionRegistry flowExecutionRegistry;

    @PostMapping
    public ResponseEntity<UUID> startFlow(@RequestBody FlowTrigger<?> flowTrigger) {
        UUID flowExecutionId = flowExecutor.startFlow(flowTrigger);
        return ResponseEntity.accepted().body(flowExecutionId);
    }

    @GetMapping("/{flowExecutionId}")
    public ResponseEntity<FlowExecution> getFlowExecution(@PathVariable UUID flowExecutionId) {
        return flowExecutionRegistry.findById(flowExecutionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
