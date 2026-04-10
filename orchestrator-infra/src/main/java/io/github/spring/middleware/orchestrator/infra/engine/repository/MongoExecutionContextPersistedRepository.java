package io.github.spring.middleware.orchestrator.infra.engine.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MongoExecutionContextPersistedRepository extends MongoRepository<ExecutionContextPersistedDocument, UUID> {

    void deleteById(UUID id);

    Optional<ExecutionContextPersistedDocument> findById(UUID id);
}
