package io.github.spring.middleware.orchestrator.core.engine;

public interface TimeoutRedirectResolver<T> {

    T get();

    String redirectAction();

}
