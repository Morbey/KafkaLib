package com.bnpparibas.bp2s.combo.comboservices.library.kafka.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.NoArgsConstructor;

/**
 * Utility storing metadata related to a Kafka message in a thread-local map.
 * Developers can add arbitrary key/value pairs that will be available to the
 * {@link com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorMapper}
 * when building the DLQ message.
 */
@NoArgsConstructor
public class KafkaErrorMetadataContext {


    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    /**
     * Store a value in the current thread context.
     *
     * @param key context key
     * @param value value to store
     */
    public static void put(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    /**
     * Retrieve a value from the context.
     *
     * @param key context key
     * @return optional value, empty if not present
     */
    public static Optional<Object> get(String key) {
        return Optional.ofNullable(CONTEXT.get().get(key));
    }

    /**
     * Clears the context for the current thread.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Obtain a copy of all stored values.
     *
     * @return map containing the context values
     */
    public static Map<String, Object> getAll() {
        return new HashMap<>(CONTEXT.get());
    }
}
