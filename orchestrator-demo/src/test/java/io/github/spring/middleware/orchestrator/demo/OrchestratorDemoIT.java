package io.github.spring.middleware.orchestrator.demo;

import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.UUID;

import static io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus.EXECUTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public class OrchestratorDemoIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FlowExecutionRegistry flowExecutionRegistry;

    @Container
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:7.0");

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("middleware.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void givenSimpleFlow_whenExecuteFlows_thenFlowsExecutedSuccessfully() {

        FlowTrigger flowTrigger = new FlowTrigger();
        flowTrigger.setFlowId("SIMPLE_FLOW");

        UUID executionId = webTestClient.post()
                .uri("/flows")
                .bodyValue(flowTrigger)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(UUID.class)
                .returnResult()
                .getResponseBody();

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    FlowExecution execution = flowExecutionRegistry.findById(executionId).orElseThrow();
                    assertThat(execution.getExecutionStatus()).isEqualTo(EXECUTED);
                });

        FlowExecution flowExecution = webTestClient.get().uri("/flows/{executionId}", executionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FlowExecution.class)
                .returnResult()
                .getResponseBody();

        FlowExecutionAssertions.assertSimpleFlowExecuted(flowExecution, executionId);

    }

}
