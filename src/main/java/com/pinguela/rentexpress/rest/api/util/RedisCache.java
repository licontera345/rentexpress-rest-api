package com.pinguela.rentexpress.rest.api.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * Utilidad de caching con Redis. Almacena objetos serializados en JSON con TTL.
 * Usa JedisPool para conexiones seguras entre hilos y reconexión ante fallos.
 */
public final class RedisCache {

    private static final Logger logger = LogManager.getLogger(RedisCache.class);

    // Redis Cloud (free-tier) - para pruebas; en producción usar variables de entorno
    private static final String HOST = "redis-10445.c135.eu-central-1-1.ec2.cloud.redislabs.com";
    private static final int PORT = 10445;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final String USERNAME = "default";
    private static final String PASSWORD = "VXUaRXC3mUU62l0F00sIaG388hDU6ha8";

    /** TTL por defecto para datos cacheados (segundos). */
    public static final int DEFAULT_TTL_SECONDS = 20;

    private static volatile JedisPool pool;
    private static final Object LOCK = new Object();

    private RedisCache() {
        throw new IllegalStateException("Utility class");
    }

    private static JedisPool getPool() {
        if (pool == null) {
            synchronized (LOCK) {
                if (pool == null) {
                    try {
                        JedisPoolConfig config = new JedisPoolConfig();
                        config.setMaxTotal(20);
                        config.setMaxIdle(10);
                        config.setMinIdle(2);
                        config.setTestOnBorrow(true);
                        config.setTestWhileIdle(true);
                        pool = new JedisPool(config, HOST, PORT, CONNECT_TIMEOUT_MS, USERNAME, PASSWORD);
                        try (Jedis j = pool.getResource()) {
                            j.ping();
                        }
                        logger.info("Redis cache pool connected to {}:{}", HOST, PORT);
                    } catch (Exception e) {
                        logger.warn("Redis connection failed (cache disabled): {}", e.getMessage());
                        return null;
                    }
                }
            }
        }
        return pool;
    }

    /** Invalida el pool para forzar reconexión tras "broken connection". */
    private static void invalidatePool() {
        synchronized (LOCK) {
            if (pool != null) {
                try {
                    pool.close();
                } catch (Exception ignored) {}
                pool = null;
            }
        }
    }

    private static boolean isConnectionFailure(Exception e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("broken connection") || msg.contains("Connection") || msg.contains("Unexpected end of stream"));
    }

    /**
     * Almacena un objeto en caché con TTL.
     *
     * @param key         clave (ej. "vehicles:_:1:10:1:10")
     * @param value       objeto a serializar a JSON
     * @param ttlSeconds  tiempo de vida en segundos
     */
    public static <T> void setObject(String key, T value, int ttlSeconds) {
        JedisPool p = getPool();
        if (p == null) return;
        try (Jedis j = p.getResource()) {
            String json = JsonUtils.getGson().toJson(value);
            j.setex(key, ttlSeconds, json);
        } catch (Exception e) {
            if (isConnectionFailure(e)) invalidatePool();
            logger.warn("Redis set failed for key {}: {}", key, e.getMessage());
        }
    }

    /**
     * Recupera un objeto desde caché.
     *
     * @param key  clave
     * @param type tipo para deserialización (ej. new TypeToken&lt;Results&lt;VehicleDTO&gt;&gt;(){}.getType())
     * @return el objeto o null si no existe o hay error
     */
    public static <T> T getObject(String key, Type type) {
        JedisPool p = getPool();
        if (p == null) return null;
        try (Jedis j = p.getResource()) {
            String json = j.get(key);
            if (json == null) return null;
            return JsonUtils.getGson().fromJson(json, type);
        } catch (Exception e) {
            if (isConnectionFailure(e)) invalidatePool();
            logger.warn("Redis get failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Borra todas las claves que empiecen por el prefijo dado (invalidación por prefijo).
     * Usa SCAN para no bloquear Redis.
     *
     * @param keyPrefix ej. "vehicles:" o "reservations:"
     */
    public static void deleteByPrefix(String keyPrefix) {
        String pattern = keyPrefix.endsWith("*") ? keyPrefix : keyPrefix + "*";
        JedisPool p = getPool();
        if (p == null) return;
        try (Jedis j = p.getResource()) {
            List<String> toDelete = new ArrayList<>();
            ScanParams params = new ScanParams().match(pattern).count(100);
            String cursor = "0";
            do {
                ScanResult<String> scanResult = j.scan(cursor, params);
                toDelete.addAll(scanResult.getResult());
                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));

            if (!toDelete.isEmpty()) {
                j.del(toDelete.toArray(new String[0]));
                logger.info("Redis cache invalidated: {} keys with prefix {}", toDelete.size(), keyPrefix);
            }
        } catch (Exception e) {
            if (isConnectionFailure(e)) invalidatePool();
            logger.warn("Redis deleteByPrefix failed for {}: {}", keyPrefix, e.getMessage());
        }
    }

    /**
     * Cierra el pool Redis (llamar en shutdown de la aplicación si procede).
     */
    public static void close() {
        synchronized (LOCK) {
            if (pool != null) {
                try {
                    pool.close();
                } catch (Exception e) {
                    logger.warn("Redis pool close: {}", e.getMessage());
                }
                pool = null;
            }
        }
    }

    /**
     * Construye un segmento de clave para un valor: si es null o vacío devuelve "_", si no el string del valor.
     */
    public static String keyPart(Object value) {
        if (value == null) return "_";
        String s = value.toString().trim();
        return s.isEmpty() ? "_" : s;
    }
}
