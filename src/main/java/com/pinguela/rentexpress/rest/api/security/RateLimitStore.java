package com.pinguela.rentexpress.rest.api.security;

/**
 * Almacén de contadores para rate limiting.
 * <p>
 * Permite desacoplar la lógica del filtro del almacenamiento. La implementación por defecto
 * es en memoria (por JVM). Para despliegue con varias instancias detrás de un balanceador,
 * implementar un almacén compartido (por ejemplo {@code RedisRateLimitStore}) y registrarlo
 * en el Binder para que el límite sea global.
 * </p>
 *
 * @see InMemoryRateLimitStore
 * @see RateLimitFilter
 */
public interface RateLimitStore {

    /**
     * Incrementa el contador para la clave en la ventana indicada y devuelve el nuevo valor.
     * Si la ventana actual para la clave es distinta de {@code windowStartMs} (o no existe),
     * se considera ventana nueva: se asocia la clave a {@code windowStartMs} con contador 1
     * y se devuelve 1.
     *
     * @param key           clave del cliente (p. ej. "ip:1.2.3.4" o "user:email@example.com")
     * @param windowStartMs inicio de la ventana en milisegundos (epoch)
     * @return el contador tras el incremento (≥ 1)
     */
    int incrementAndGet(String key, long windowStartMs);

    /**
     * Devuelve el contador actual para la clave sin incrementar.
     * Si no hay ventana activa para la clave, devuelve 0.
     *
     * @param key clave del cliente
     * @return contador actual o 0
     */
    int getCurrentCount(String key);

    /**
     * Elimina el contador asociado a la clave (útil para pruebas o administración).
     *
     * @param key clave del cliente
     */
    void reset(String key);
}
