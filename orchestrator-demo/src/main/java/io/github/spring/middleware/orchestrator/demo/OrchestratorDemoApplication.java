package io.github.spring.middleware.orchestrator.demo;

import io.github.spring.middleware.kafka.core.registrar.EnableMiddlewareKafkaListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"io.github.spring.middleware"})
@EnableMiddlewareKafkaListeners(basePackages = {"io.github.spring.middleware.orchestrator.demo.flows.context"})
@EnableMongoRepositories(basePackages = "io.github.spring.middleware.orchestrator.infra.engine.repository")
@EnableScheduling
public class OrchestratorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorDemoApplication.class, args);
    }

}
