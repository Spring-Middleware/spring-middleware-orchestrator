package io.github.spring.middleware.orchestrator.core.engine.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.spring.middleware.orchestrator.core.runtime.ActionException;
import org.springframework.core.ResolvableType;

import java.util.Map;

public final class PayloadParserUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static <T> T parse(
            Object rawPayload,
            Object action,
            Class<?> targetInterface,
            int genericIndex
    ) {

        if (rawPayload == null) {
            return null;
        }

        ResolvableType type = ResolvableType.forClass(action.getClass())
                .as(targetInterface);

        Class<?> payloadType = type.getGeneric(genericIndex).resolve();

        if (payloadType == Void.class) {
            return null;
        }

        if (payloadType != null && payloadType.isInstance(rawPayload)) {
            return (T) rawPayload;
        }

        if (payloadType != null && rawPayload instanceof Map<?, ?> map) {
            return (T) OBJECT_MAPPER.convertValue(map, payloadType);
        }

        return (T) rawPayload;
    }
}
