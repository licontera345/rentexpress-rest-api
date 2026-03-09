package com.pinguela.rentexpress.rest.api.util;

import com.pinguela.rentexpres.config.ConfigManager;

/**
 * Utility for reading integer configuration values used by API adapters (e.g. Groq, Weather).
 * Reads from {@link ConfigManager}; returns default if key is missing or invalid.
 */
public final class AdapterConfigUtil {

    private AdapterConfigUtil() {}

    /**
     * Gets an integer configuration value by key.
     *
     * @param key          configuration key (e.g. groq.api.connect.timeout.seconds)
     * @param defaultValue value to return if key is missing or not a valid integer
     * @return the parsed integer or defaultValue
     */
    public static int getIntConfig(String key, int defaultValue) {
        try {
            String v = ConfigManager.getValue(key);
            if (v != null && !v.trim().isEmpty()) {
                return Integer.parseInt(v.trim());
            }
        } catch (Exception e) {
            // use default
        }
        return defaultValue;
    }
}
