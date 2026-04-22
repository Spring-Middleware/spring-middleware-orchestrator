package io.github.spring.middleware.orchestrator.core.engine.action.exec;

import io.github.spring.middleware.orchestrator.core.domain.ActionDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionExecutorUtilsTest {

    public static class DummyAction {
        private String configString;
        private Boolean flag;
        private Integer count;

        public String getConfigString() {
            return configString;
        }

        public void setConfigString(String configString) {
            this.configString = configString;
        }

        public Boolean getFlag() {
            return flag;
        }

        public void setFlag(Boolean flag) {
            this.flag = flag;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    public static class ErrorProneAction {
        public void setBadConfig(String value) {
            throw new RuntimeException("Simulated setter error");
        }
    }

    @Test
    void configureAction_ShouldSetProperties_WhenSettersExist() {
        DummyAction action = new DummyAction();
        ActionDefinition actionDefinition = new ActionDefinition();

        Map<String, Object> config = new HashMap<>();
        config.put("configString", "testValue");
        config.put("flag", true);
        config.put("count", 42);
        actionDefinition.setConfiguration(config);

        ActionExecutorUtils.configureAction(action, actionDefinition);

        assertEquals("testValue", action.getConfigString());
        assertEquals(true, action.getFlag());
        assertEquals(42, action.getCount());
    }

    @Test
    void configureAction_ShouldIgnoreProperties_WhenNoSetterExists() {
        DummyAction action = new DummyAction();
        ActionDefinition actionDefinition = new ActionDefinition();

        Map<String, Object> config = new HashMap<>();
        config.put("nonExistentConfig", "unknown");
        actionDefinition.setConfiguration(config);

        ActionExecutorUtils.configureAction(action, actionDefinition);

        // State remains intact
        assertNull(action.getConfigString());
        assertNull(action.getFlag());
        assertNull(action.getCount());
    }

    @Test
    void configureAction_ShouldContinue_WhenSetterThrowsException() {
        ErrorProneAction action = new ErrorProneAction();
        ActionDefinition actionDefinition = new ActionDefinition();

        Map<String, Object> config = new HashMap<>();
        config.put("badConfig", "triggerError");
        actionDefinition.setConfiguration(config);

        // Should securely eat the exception via its try-catch and log
        ActionExecutorUtils.configureAction(action, actionDefinition);
    }

    @Test
    void configureAction_ShouldDoNothing_WhenConfigurationIsNull() {
        DummyAction action = new DummyAction();
        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setConfiguration(null);

        ActionExecutorUtils.configureAction(action, actionDefinition);

        assertNull(action.getConfigString());
    }

    @Test
    void configureAction_ShouldDoNothing_WhenConfigurationIsEmpty() {
        DummyAction action = new DummyAction();
        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setConfiguration(new HashMap<>());

        ActionExecutorUtils.configureAction(action, actionDefinition);

        assertNull(action.getConfigString());
    }
}
