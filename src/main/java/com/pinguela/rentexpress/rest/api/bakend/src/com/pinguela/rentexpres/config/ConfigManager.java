package com.pinguela.rentexpres.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase para cargar y gestionar la configuración desde el archivo
 * config.properties.
 */
public class ConfigManager {

	private static final Logger logger = LogManager.getLogger(ConfigManager.class);
	private static final Properties propertiesCfg = new Properties();
	private static final Path uploadDir = initializeUploadDir();
	static {
		try {
			// Carga del archivo config.properties
			InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties");

			if (inputStream == null) {
				logger.fatal("El archivo de configuración no se encontró.");
				throw new RuntimeException("El archivo de configuración no se encontró.");
			}

			propertiesCfg.load(inputStream);
			logger.info("Configuración cargada correctamente.");

		} catch (Exception e) {
			logger.fatal("No se pudo cargar el archivo de configuración: " + e.getMessage(), e);
			throw new RuntimeException("Error al cargar configuración", e);
		}
	}

	/**
	 * Obtiene el valor de una clave de configuración como String (puede devolver
	 * null si no existe).
	 */
	public static String getValue(String parameterName) {
		return propertiesCfg.getProperty(parameterName);
	}

	public static Path getUploadDir() {
		return uploadDir;
	}

	/**
	 * Inicializa el directorio base de subidas. Usa base.image.path de
	 * config.properties o un valor por defecto.
	 */
	private static Path initializeUploadDir() {
		String configuredPath = getValue("base.image.path");
		Path basePath;

		if (configuredPath == null || configuredPath.isEmpty()) {
			basePath = Paths.get(System.getProperty("user.dir"), "resources", "uploads");
		} else {
			Path configured = Paths.get(configuredPath);
			if (configured.isAbsolute()) {
				basePath = configured;
			} else {
				basePath = Paths.get(System.getProperty("user.dir"), configuredPath);
			}
		}

		try {
			Files.createDirectories(basePath);
		} catch (IOException e) {
			logger.error("No se pudo crear el directorio base de subidas: {}", basePath, e);
		}

		return basePath.toAbsolutePath().normalize();
	}


	/**
	 * Obtiene el valor de una clave de configuración como String, pero si no
	 * existe, devuelve cadena vacía y loguea un error.
	 */
	public static String getStringValue(String key) {
		String value = propertiesCfg.getProperty(key);
		if (value == null) {
			logger.error("La clave '" + key + "' no existe en config.properties");
			return "";
		}
		return value;
	}
}
