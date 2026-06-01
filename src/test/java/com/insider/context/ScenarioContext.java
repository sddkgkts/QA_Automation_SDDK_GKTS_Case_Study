package com.insider.context;

import java.util.HashMap;
import java.util.Map;

/**
 * ScenarioContext
 *
 * Thread-safe shared state container for passing data between step definition classes.
 * Uses ThreadLocal to ensure each parallel test thread has its own isolated context.
 *
 * Usage:
 *   ScenarioContext.set("savedPetId", 12345L);
 *   Long id = ScenarioContext.get("savedPetId", Long.class);
 */
public class ScenarioContext {

    private static final ThreadLocal<Map<String, Object>> contextHolder =
        ThreadLocal.withInitial(HashMap::new);

    // ── Context key constants ────────────────────────────────────────────────
    public static final String SAVED_PET_ID  = "SAVED_PET_ID";
    public static final String CURRENT_PET   = "CURRENT_PET";
    public static final String API_RESPONSE  = "API_RESPONSE";
    public static final String SCENARIO_NAME = "SCENARIO_NAME";

    /** Utility class — no instantiation */
    private ScenarioContext() {}

    /**
     * Store a value in the context.
     *
     * @param key   Context key
     * @param value Value to store
     */
    public static void set(String key, Object value) {
        contextHolder.get().put(key, value);
    }

    /**
     * Retrieve a value from the context.
     *
     * @param key Context key
     * @return Stored value, or null if not present
     */
    public static Object get(String key) {
        return contextHolder.get().get(key);
    }

    /**
     * Retrieve a type-safe value from the context.
     *
     * @param key  Context key
     * @param type Expected class type
     * @return Cast value, or null if not present
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> type) {
        return (T) contextHolder.get().get(key);
    }

    /**
     * Check whether a key exists in the context.
     *
     * @param key Context key
     * @return true if key is present
     */
    public static boolean contains(String key) {
        return contextHolder.get().containsKey(key);
    }

    /**
     * Clear all entries from the context map (call between scenarios).
     */
    public static void clear() {
        contextHolder.get().clear();
    }

    /**
     * Remove the ThreadLocal entirely to prevent memory leaks (call after scenario teardown).
     */
    public static void remove() {
        contextHolder.remove();
    }
}
