package com.pinguela.rentexpres.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.FileService;

public class FileServiceImpl implements FileService {

	private static final Logger logger = LogManager.getLogger(FileServiceImpl.class);

	// === Keys de configuración ===
	private static final String BASE_DIR_IMAGES_VEHICLE = "base.image.path.vehicle";
	private static final String BASE_DIR_IMAGES_USER = "base.image.path.user";
	private static final String BASE_DIR_IMAGES_EMPLOYEE = "base.image.path.employee";

	// Opcionales (ruta absoluta en FS) — si no están, se intentan defaults de
	// classpath
	private static final String DEFAULT_IMG_USER_PATH = "base.image.default.user";
	private static final String DEFAULT_IMG_EMPLOYEE_PATH = "base.image.default.employee";

	// Nombre fijo para avatar
	private static final String AVATAR_NAME = "profile.jpg";

	// Extensiones permitidas
	private static final Pattern IMG_EXT = Pattern.compile("(?i).*\\.(jpg|jpeg|png|gif)$");

	// ==========================
	// VEHICLE
	// ==========================
	@Override
	public List<File> getImagesByVehicleId(Integer vehicleId) throws RentexpresException {
		if (vehicleId == null)
			return Collections.emptyList();

		File dir = new File(getRequired(BASE_DIR_IMAGES_VEHICLE), vehicleId.toString());
		if (!dir.exists() || !dir.isDirectory())
			return Collections.emptyList();

		File[] files = dir.listFiles((d, name) -> IMG_EXT.matcher(name).matches());
		if (files == null || files.length == 0)
			return Collections.emptyList();

		// Orden natural por nombre (1.jpg, 2.jpg, …)
		Arrays.sort(files, Comparator.comparing(File::getName));
		return Arrays.asList(files);
	}

	@Override
	public void uploadImagesByVehicleId(List<File> imagenes, Integer vehicleId) throws RentexpresException {
		if (vehicleId == null)
			return;

		File base = ensureDir(getRequired(BASE_DIR_IMAGES_VEHICLE));
		File folder = ensureDir(new File(base, vehicleId.toString()));

		// 1) Borrar lo que ya no está
		Set<String> incomingAbs = new HashSet<>();
		if (imagenes != null) {
			for (File f : imagenes) {
				if (f != null)
					incomingAbs.add(f.getAbsolutePath());
			}
		}
		File[] current = folder.listFiles((d, name) -> IMG_EXT.matcher(name).matches());
		if (current != null) {
			for (File existing : current) {
				// Si el fichero existente no está en la lista entrante (por ruta absoluta), se
				// borra
				boolean remains = false;
				for (String in : incomingAbs) {
					if (sameFile(existing, new File(in))) {
						remains = true;
						break;
					}
				}
				if (!remains)
					safeDelete(existing);
			}
		}

		// 2) Copiar/actualizar imágenes nuevas (si vienen de fuera del directorio de
		// destino)
		if (imagenes != null && !imagenes.isEmpty()) {
			for (File img : imagenes) {
				if (img == null)
					continue;
				if (!IMG_EXT.matcher(img.getName()).matches()) {
					logger.warn("Archivo no imagen omitido: {}", img.getName());
					continue;
				}
				// Si ya está dentro del folder del vehículo, no hacemos nada
				try {
					Path targetDir = folder.toPath().toRealPath();
					Path srcPath = img.toPath().toRealPath();

					if (!srcPath.startsWith(targetDir)) {
						// nuevo nombre secuencial: 1.jpg, 2.jpg, ...
						String next = getNextNameSequential(folder);
						File target = new File(folder, next + ".jpg");
						Files.copy(img.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					logger.error("Error copiando imagen de vehículo {}", img, e);
					throw new RentexpresException("Error subiendo imágenes de vehículo", e);
				}
			}
		}
	}

	// ==========================
	// USER (avatar único)
	// ==========================
	@Override
	public File getImageByUserId(Integer userId) throws RentexpresException {
		if (userId == null)
			return getDefaultUserAvatar();

		File dir = new File(getRequired(BASE_DIR_IMAGES_USER), userId.toString());
		File avatar = new File(dir, AVATAR_NAME);
		if (avatar.exists())
			return avatar;

		return getDefaultUserAvatar();
	}

	@Override
	public void uploadImageByUserId(File imagen, Integer userId) throws RentexpresException {
		if (userId == null)
			return;

		File base = ensureDir(getRequired(BASE_DIR_IMAGES_USER));
		File dir = ensureDir(new File(base, userId.toString()));
		File target = new File(dir, AVATAR_NAME);

		saveAvatarOrDefault(imagen, target, DEFAULT_IMG_USER_PATH, "/imagenes/default_user.png");
	}

	// ==========================
	// EMPLOYEE (avatar único)
	// ==========================
	@Override
	public File getImageByEmployeeId(Integer employeeId) throws RentexpresException {
		if (employeeId == null)
			return getDefaultEmployeeAvatar();

		File dir = new File(getRequired(BASE_DIR_IMAGES_EMPLOYEE), employeeId.toString());
		File avatar = new File(dir, AVATAR_NAME);
		if (avatar.exists())
			return avatar;

		return getDefaultEmployeeAvatar();
	}

	@Override
	public void uploadImageByEmployeeId(File imagen, Integer employeeId) throws RentexpresException {
		if (employeeId == null)
			return;

		File base = ensureDir(getRequired(BASE_DIR_IMAGES_EMPLOYEE));
		File dir = ensureDir(new File(base, employeeId.toString()));
		File target = new File(dir, AVATAR_NAME);

		saveAvatarOrDefault(imagen, target, DEFAULT_IMG_EMPLOYEE_PATH, "/imagenes/default_employee.png");
	}

	// ==========================
	// Helpers
	// ==========================

	private String getRequired(String key) throws RentexpresException {
		String v = ConfigManager.getValue(key);
		if (v == null || v.isEmpty()) {
			throw new RentexpresException("Config key missing: " + key);
		}
		return v;
	}

	private File ensureDir(String path) throws RentexpresException {
		return ensureDir(new File(path));
	}

	private File ensureDir(File dir) throws RentexpresException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RentexpresException("No se puede crear directorio: " + dir.getAbsolutePath());
		}
		if (!dir.isDirectory()) {
			throw new RentexpresException("Ruta no es directorio: " + dir.getAbsolutePath());
		}
		return dir;
	}

	private boolean sameFile(File a, File b) {
		try {
			return a.getCanonicalPath().equals(b.getCanonicalPath());
		} catch (IOException e) {
			return a.getAbsolutePath().equals(b.getAbsolutePath());
		}
	}

	private void safeDelete(File f) {
		try {
			Files.deleteIfExists(f.toPath());
		} catch (IOException e) {
			logger.warn("No se pudo borrar: {}", f.getAbsolutePath(), e);
		}
	}

	/**
	 * Obtiene el siguiente número libre escaneando nombres "N.extension" en el
	 * directorio. Acepta .jpg/.jpeg/.png/.gif; devolvemos el siguiente entero.
	 */
	private String getNextNameSequential(File dir) {
		File[] files = dir.listFiles((d, name) -> IMG_EXT.matcher(name).matches());
		int max = 0;
		if (files != null) {
			for (File f : files) {
				String name = f.getName(); // p.ej. "12.jpg"
				int dot = name.lastIndexOf('.');
				String base = (dot > 0) ? name.substring(0, dot) : name;
				try {
					int n = Integer.parseInt(base);
					if (n > max)
						max = n;
				} catch (NumberFormatException ignore) {
					// ignora nombres no numéricos
				}
			}
		}
		return String.valueOf(max + 1);
	}

	private void saveAvatarOrDefault(File imagen, File target, String defaultFsKey, String classpathFallback)
			throws RentexpresException {

		try {
			if (imagen != null && IMG_EXT.matcher(imagen.getName()).matches()) {
				Files.copy(imagen.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return;
			}

			// Si la imagen es nula o inválida → intentar default del FS
			String defPath = ConfigManager.getValue(defaultFsKey);
			if (defPath != null && !defPath.isEmpty()) {
				File def = new File(defPath);
				if (def.exists()) {
					Files.copy(def.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					return;
				}
			}

			// Último recurso → default en classpath
			try (InputStream in = getClass().getResourceAsStream(classpathFallback)) {
				if (in != null) {
					Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					logger.error("Default no encontrada en classpath: {}", classpathFallback);
				}
			}
		} catch (IOException e) {
			logger.error("Error guardando avatar {}", target.getAbsolutePath(), e);
			throw new RentexpresException("Error guardando avatar", e);
		}
	}

	private File getDefaultUserAvatar() {
		File f = defaultFromFs(DEFAULT_IMG_USER_PATH);
		return (f != null) ? f : defaultFromClasspath("/imagenes/default_user.png");
	}

	private File getDefaultEmployeeAvatar() {
		File f = defaultFromFs(DEFAULT_IMG_EMPLOYEE_PATH);
		return (f != null) ? f : defaultFromClasspath("/imagenes/default_employee.png");
	}

	private File defaultFromFs(String key) {
		String p = ConfigManager.getValue(key);
		if (p != null && !p.isEmpty()) {
			File f = new File(p);
			if (f.exists())
				return f;
		}
		return null;
	}

        private File defaultFromClasspath(String resource) {
                try (InputStream in = getClass().getResourceAsStream(resource)) {
                        if (in == null)
                                return null;
                        // Copia temporal en /tmp para poder devolver File (API usa File)
                        Path tmp = Files.createTempFile("default-", "-" + Paths.get(resource).getFileName().toString());
                        Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                        return tmp.toFile();
                } catch (IOException e) {
                        logger.warn("No se pudo cargar default de classpath: {}", resource, e);
                        return null;
                }
        }

        @Override
        public String getImageUrl(Integer vehicleId, String imageName) {
                return "/uploads/vehicle/" + vehicleId + "/" + imageName;
        }

        @Override
        public String getUserAvatarUrl(Integer userId) {
                return "/uploads/user/" + userId + "/profile.jpg";
        }

        @Override
        public String getEmployeeAvatarUrl(Integer employeeId) {
                return "/uploads/employee/" + employeeId + "/profile.jpg";
        }

}