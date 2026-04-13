package io.github.spring.middleware.orchestrator.demo.flows.simple;

import lombok.Data;

import java.util.List;

@Data
public class SimplePayload {

    private String name;
    private int number;
    private List<Integer> array;

    public String toString() {
        return STR."SimplePayload{name='\{name}', number=\{number}, array=\{array}}";
    }
}
