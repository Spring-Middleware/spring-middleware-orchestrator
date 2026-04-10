package io.github.spring.middleware.orchestrator.core.engine;

public class FlowDefinitionsLoaderException extends RuntimeException
{
    public FlowDefinitionsLoaderException(String message) {
        super(message);
    }

     public FlowDefinitionsLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
