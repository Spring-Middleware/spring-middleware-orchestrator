package io.github.spring.middleware.orchestrator.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"io.github.spring.middleware"})
@EnableMongoRepositories(basePackages = "io.github.spring.middleware.orchestrator.infra.engine.repository")
@EnableScheduling
public class OrchestratorDemoApplication {

    static void main(String[] args) {
        SpringApplication.run(OrchestratorDemoApplication.class, args);
    }

}
