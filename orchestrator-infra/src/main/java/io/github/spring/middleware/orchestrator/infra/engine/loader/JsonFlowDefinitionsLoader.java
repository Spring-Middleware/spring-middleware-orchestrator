package io.github.spring.middleware.orchestrator.infra.engine.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spring.middleware.orchestrator.core.domain.FlowDefinition;
import io.github.spring.middleware.orchestrator.core.engine.FlowDefinitionsLoader;
import io.github.spring.middleware.orchestrator.core.engine.FlowDefinitionsLoaderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonFlowDefinitionsLoader implements FlowDefinitionsLoader {

    @Value("${orchestrator.flows-location:classpath*:flows/*.json}")
    private String flowsLocation;

    private final ObjectMapper objectMapper;

    @Override
    public Collection<FlowDefinition> loadFlowDefinitions() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(flowsLocation);

            List<FlowDefinition> flowDefinitions = new ArrayList<>();
            for (Resource resource : resources) {
                try {
                    FlowDefinition flowDefinition = loadFlowDefinition(resource);
                    flowDefinition.buildActionDefinitionMap();
                    if (flowDefinition.getFirstAction() == null) {
                        log.warn("Flow definition in resource {} does not have a valid firstAction, skipping...", safeDescription(resource));
                        continue;
                    }
                    flowDefinitions.add(flowDefinition);
                } catch (Exception ex) {
                    log.error("Error loading flow definition from resource {}, skipping...", safeDescription(resource), ex);
                }
            }

            return flowDefinitions;
        } catch (Exception ex) {
            log.error("Error loading flow definitions from location {}", flowsLocation, ex);
            throw new FlowDefinitionsLoaderException(
                    STR."Error loading flow definitions from location \{flowsLocation}", ex
            );
        }
    }

    private FlowDefinition loadFlowDefinition(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, FlowDefinition.class);
        }
    }

    private String safeDescription(Resource resource) {
        try {
            return resource.getURI().toString();
        } catch (Exception ex) {
            return resource.getDescription();
        }
    }
}
