package io.github.spring.middleware.orchestrator.demo;

import io.github.spring.middleware.orchestrator.core.port.FlowExecutionRegistry;
import io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus;
import io.github.spring.middleware.orchestrator.core.runtime.FlowExecution;
import io.github.spring.middleware.orchestrator.core.runtime.FlowTrigger;
import io.github.spring.middleware.orchestrator.demo.flows.chained.ChainedType;
import io.github.spring.middleware.orchestrator.demo.flows.resolver.FlowInput;
import io.github.spring.middleware.orchestrator.demo.flows.resume.ContextInput;
import io.github.spring.middleware.orchestrator.demo.flows.resume.ResumeType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus.ERROR;
import static io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus.EXECUTED;
import static io.github.spring.middleware.orchestrator.core.runtime.ExecutionStatus.SUSPENDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public class OrchestratorDemoIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FlowExecutionRegistry flowExecutionRegistry;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

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
    void should_have_orchestrator_subscriber_running() {

        assertThat(registry.getListenerContainers())
                .isNotEmpty();

        var container = registry.getListenerContainers().stream()
                .filter(c -> "orchestrator-subscriber".equals(c.getListenerId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Listener orchestrator-subscriber not found"));

        assertThat(container.isRunning())
                .as("Listener orchestrator-subscriber should be running")
                .isTrue();
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("provideFlows")
    void givenFlowTrigger_whenExecuteFlows_thenFlowsAsserts(FlowTestCase flowTestCase) {

        UUID executionId = webTestClient.post()
                .uri("/flows")
                .bodyValue(flowTestCase.flowTrigger())
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(UUID.class)
                .returnResult()
                .getResponseBody();

        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    FlowExecution execution = flowExecutionRegistry.findById(executionId).orElseThrow();
                    assertThat(execution.getExecutionStatus()).isEqualTo(flowTestCase.executionStatus());
                });

        FlowExecution flowExecution = webTestClient.get().uri("/flows/{executionId}", executionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FlowExecution.class)
                .returnResult()
                .getResponseBody();

        flowTestCase.assertFlow().accept(flowExecution, executionId);
    }


    private static Stream<FlowTestCase> provideFlows() {
        return Stream.of(
                new FlowTestCase("Simple flow", EXECUTED, createFlow("SIMPLE_FLOW"), FlowExecutionAssertions::assertSimpleFlowExecuted),
                new FlowTestCase("Chained flow with success", EXECUTED, createFlow("CHAINED_FLOW", ChainedType.SUCCESS), FlowExecutionAssertions::assertChainedSuccessFlowExecuted),
                new FlowTestCase("Chained flow with fail", ERROR, createFlow("CHAINED_FLOW", ChainedType.FAIL), FlowExecutionAssertions::assertChainedErrorFlowExecuted),
                new FlowTestCase("Resolver flow", EXECUTED, createFlow("RESOLVER_FLOW", createFlowInput()), FlowExecutionAssertions::assertResolverFlowExecuted),
                new FlowTestCase("Resume flow with RESUME", EXECUTED, createFlow("RESUME_FLOW", createContextInput("11111",ResumeType.RESUME)),FlowExecutionAssertions::assertResumeFlowExecuted),
                new FlowTestCase("Resume flow with SUSPEND", SUSPENDED, createFlow("RESUME_FLOW", createContextInput("22222",ResumeType.NOT_RESUME)),FlowExecutionAssertions::assertResumeFlowSuspended),
                new FlowTestCase("Timeout flow to ERROR", ERROR, createFlow("TIMEOUT_ERROR_FLOW", createContextInput("11111",ResumeType.NOT_RESUME)),FlowExecutionAssertions::assertTimeoutErrorFlowExecuted),
                new FlowTestCase("Timeout flow to END", EXECUTED, createFlow("TIMEOUT_END_FLOW", createContextInput("22222",ResumeType.NOT_RESUME)),FlowExecutionAssertions::assertTimeoutEndFlowExecuted)
        );
    }

    private static ContextInput createContextInput(String key, ResumeType resumeType) {
        ContextInput contextInput = new ContextInput();
        contextInput.setKey(key);
        contextInput.setResumeType(resumeType);
        return contextInput;
    }

    private static FlowInput createFlowInput() {
        FlowInput flowInput = new FlowInput();
        flowInput.setValuesByAction(Map.of("FIRST_PROB_ACTION", "First Probabilistic Action", "SECOND_PROB_ACTION", "Second Probabilistic Action"));
        return flowInput;
    }

    private static <T> FlowTrigger createFlow(String flowId) {
        return createFlow(flowId, null);
    }


    private static <T> FlowTrigger createFlow(String flowId, T payload) {
        FlowTrigger<T> flowTrigger = new FlowTrigger<>();
        flowTrigger.setFlowId(flowId);
        flowTrigger.setPayload(payload);
        return flowTrigger;
    }
}

record FlowTestCase(String testCase, ExecutionStatus executionStatus, FlowTrigger flowTrigger,
                    BiConsumer<FlowExecution<?, ?>, UUID> assertFlow) {

    @Override
    public String toString() {
        return testCase;
    }
}
