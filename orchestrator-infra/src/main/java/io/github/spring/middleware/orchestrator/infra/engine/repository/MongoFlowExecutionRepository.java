package io.github.spring.middleware.orchestrator.infra.engine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MongoFlowExecutionRepository
        extends MongoRepository<FlowExecutionDocument, UUID> {
}
