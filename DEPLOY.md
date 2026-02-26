# RentExpress REST API

## Despliegue en Tomcat (Eclipse / IDE)

Para que la API responda en `http://localhost:8081/rentexpress-rest-api/api/...` y Swagger en `/rentexpress-rest-api/api/openapi.json`:

### 1. Compilar el WAR

```bash
mvn clean package
```

El WAR se genera en `target/rentexpress-rest-api.war`.

### 2. Configurar Tomcat en Eclipse

- **Servidores**: Ventana → Mostrar vista → Servidores. Añade Apache Tomcat 10 y apunta a tu instalación (puerto 8081 si lo cambiaste).
- **Añadir el proyecto**: Clic derecho en el servidor → Add and Remove… → añade **rentexpress-rest-api** (el proyecto Maven del API, no el middleware).
- **Context root**: El contexto debe ser **rentexpress-rest-api** (por defecto suele tomar el nombre del proyecto/WAR). Para comprobarlo: doble clic en el servidor → pestaña *Modules* → la aplicación debe tener *Path* = `/rentexpress-rest-api`.
- **Dependencias Maven en el despliegue**: Clic derecho en el proyecto **rentexpress-rest-api** → Properties → **Deployment Assembly**. Debe haber una entrada **Maven Dependencies** con *Deploy Path* = `WEB-INF/lib`. Si no está, Add… → Java Build Path Entries → Maven Dependencies → Finish. Así Jersey y el resto de JARs se copian al servidor y la API puede arrancar.

### 3. Recurso JNDI en Tomcat

La API espera un DataSource en JNDI: `jdbc/rentexpresDS`. Configúralo en el contexto de la aplicación (p. ej. en `conf/context.xml` o en la configuración del servidor en Eclipse).

### 4. Arrancar y comprobar

- Inicia el servidor desde Eclipse (o despliega el WAR en `webapps` si usas Tomcat standalone).
- En la consola de Tomcat debe aparecer el arranque de la aplicación (no solo el arranque del servidor).
- Prueba:
  - **API**: `http://localhost:8081/rentexpress-rest-api/api/openapi.json`
  - **Swagger UI** (si está configurado): `http://localhost:8081/rentexpress-rest-api/swagger-ui/`

### 5. Frontend (Vite)

Con el backend en 8081 y context `/rentexpress-rest-api`, el frontend en `rentexpress-react` usa en dev:

- `VITE_API_BASE_URL=/rentexpress-rest-api/api` (por defecto)
- Proxy en `vite.config.js`: `/rentexpress-rest-api` → `http://localhost:8081`

Así las peticiones desde `localhost:5173` se reenvían a Tomcat. Si la API no está desplegada o el contexto es otro, recibirás 404.
