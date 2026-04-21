package io.github.spring.middleware.orchestrator.core.runtime;

public class ActionException extends RuntimeException {

    public ActionException(String message) {
        super(message);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
