package com.pinguela.rentexpress.rest.api.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementación en memoria del almacén de rate limit (por JVM).
 * No es compartida entre instancias; para múltiples instancias usar un store
 * compartido (por ejemplo Redis) e inyectarlo en el Binder.
 *
 * @see RateLimitStore
 * @see RateLimitFilter
 */
public class InMemoryRateLimitStore implements RateLimitStore {

    private static final class Window {
        final long windowStartMs;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStartMs) {
            this.windowStartMs = windowStartMs;
        }
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public int incrementAndGet(String key, long windowStartMs) {
        Window window = windows.compute(key, (k, w) -> {
            if (w == null || w.windowStartMs != windowStartMs) {
                return new Window(windowStartMs);
            }
            return w;
        });
        return window.count.incrementAndGet();
    }

    @Override
    public int getCurrentCount(String key) {
        Window w = windows.get(key);
        return w == null ? 0 : w.count.get();
    }

    @Override
    public void reset(String key) {
        windows.remove(key);
    }
}
