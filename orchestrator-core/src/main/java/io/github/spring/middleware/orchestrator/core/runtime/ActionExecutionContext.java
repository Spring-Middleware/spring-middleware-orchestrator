package io.github.spring.middleware.orchestrator.core.runtime;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ActionExecutionContext {

    private Map<String, Object> data = new HashMap<>();

    public void put(String propertyName, Object value) {

        data.put(propertyName, value);
    }

    public <T> T get(String propertyName) {

        return (T) data.get(propertyName);
    }

    public boolean isEmpty() {

        return data.isEmpty();
    }

}
