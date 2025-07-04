package com.bnpparibas.bp2s.combo.comboservices.library.kafka.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NoArgsConstructor;

/**
 * Thread local context to allow
 */
@NoArgsConstructor
public class KafkaErrorMetadataContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Optional<Object> get(String key) {
        return Optional.ofNullable(CONTEXT.get().get(key));
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Map<String, Object> getAll() {
        return new HashMap<>(CONTEXT.get());
    }
}
