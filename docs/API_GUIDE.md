# Guía REST API – RentExpress

## Estructura del proyecto

```
rentexpress-rest-api/
├── src/main/java/com/pinguela/rentexpress/rest/api/
│   ├── adapter/           # Adapters para APIs externas (Weather, Groq) con timeouts
│   │   ├── WeatherApiAdapter.java, WeatherApiException.java
│   │   ├── GroqApiAdapter.java, GroqApiException.java
│   │   └── impl/
│   ├── base/              # BaseCrudResource (genéricos CRUD)
│   ├── dto/               # DTOs de request/response
│   ├── inject/            # RestApiBinder (HK2), ChatSocketInitFilter
│   ├── param/             # Convertidores y ExceptionMappers de parámetros
│   ├── security/          # CORS, JWT, @Secured, RateLimitFilter (por rol)
│   ├── service/           # WeatherService, GroqService (OwnershipService en middleware)
│   ├── util/              # Redis, JWT, fechas
│   ├── ws/                # ChatSocket, ChatSocketServiceHolder
│   ├── api.user/          # UserResource
│   ├── api.auth/          # AccesoResource
│   ├── api.rental/        # RentalResource, RentalStatusResource
│   ├── api.vehicle/       # VehicleResource (ETag en GET), VehicleCategoryResource, VehicleStatusResource
│   ├── api.reservation/   # ReservationResource, ReservationStatusResource
│   ├── api.config/        # WeatherResource
│   ├── api.chat/          # ConversationResource
│   ├── api.address/       # AddressResource, CityResource, ProvinceResource
│   ├── api.headquarters/  # HeadquartersResource
│   ├── api.employee/      # EmployeeResource
│   ├── api.role/          # RoleResource
│   ├── api.statistics/    # StatisticsResource
│   ├── api.media/         # ImageResource, CloudinaryResource
│   ├── api.recommendation/# RecommendationResource
│   ├── GenericExceptionMapper.java
│   ├── RentexpresExceptionMapper.java
│   └── RestApiApplication.java
├── docs/
│   ├── API_GUIDE.md       # Esta guía
│   ├── PACKAGE_STRUCTURE.md # Estructura por dominios y DI
│   └── FILE_DOWNLOADS.md  # Descargas seguras
├── src/main/webapp/       # Swagger UI
└── pom.xml
```

Ver **docs/PACKAGE_STRUCTURE.md** para la tabla de dominios y uso de BaseCrudResource/CrudService.

La API depende del **middleware** (`rentexpress-middleware`) para servicios de dominio (usuarios, reservas, vehículos, imágenes, etc.).

## Cómo ejecutar

1. **Requisitos:** JDK 11+, Maven 3.x, MySQL (o el DS configurado en el servidor).
2. **Configuración:** `config.properties` (o variables de entorno) con BD, JWT, opcionalmente Redis, Weather/Groq API keys.
3. **Build:**  
   `mvn clean package`  
   Genera `target/rentexpress-rest-api.war`.
4. **Despliegue:** Desplegar el WAR en Tomcat (u otro contenedor Servlet 3.x). El contexto por defecto suele ser `rentexpress-rest-api`.
5. **Base URL:** `http://localhost:8081/rentexpress-rest-api/api` (ajustar host/puerto/contexto según el entorno).

## Documentación Swagger / OpenAPI

- **Swagger UI:**  
  `http://localhost:8081/rentexpress-rest-api/swagger-ui/index.html`  
  (Ruta típica; puede variar según cómo esté montado el webapp.)
- **OpenAPI JSON:**  
  `http://localhost:8081/rentexpress-rest-api/api/openapi.json`  
  (Tras arrancar la aplicación, el recurso `OpenApiResource` expone la spec generada desde las anotaciones.)

### Endpoints documentados (con @Tag y @Operation)

Los siguientes Resources están anotados con `@Tag`, `@Operation` y `@ApiResponse` y aparecen en Swagger:

| Tag / Recurso      | Descripción breve                          |
|--------------------|--------------------------------------------|
| Addresses          | CRUD de direcciones                        |
| Cities             | CRUD de ciudades                           |
| Cloudinary         | Firma para subida de imágenes              |
| Conversations      | Chat de soporte (conversaciones, mensajes) |
| Employees          | Empleados, criterios, autenticación       |
| Headquarters       | CRUD de sedes                              |
| Images             | Imágenes de vehículos, usuarios, empleados; descarga segura |
| Recommendations    | Recomendaciones IA (Groq)                  |
| Reservations       | Reservas y estimaciones                    |
| Rental Statuses    | Estados de alquiler                        |
| Rentals            | Alquileres                                 |
| Reservation Statuses | Estados de reserva                       |
| Roles              | Roles de usuario                           |
| Statistics         | Estadísticas / dashboard                   |
| Users              | Usuarios, 2FA                              |
| Vehicle Categories  | Categorías de vehículos                   |
| Vehicles           | Vehículos, mantenimiento                   |
| Vehicle Statuses   | Estados de vehículo                        |
| Weather            | Clima por ciudad (API externa)             |

## Convenciones de errores

### ErrorResponseDTO

Las respuestas de error en JSON siguen este formato:

```json
{
  "code": "BAD_REQUEST",
  "message": "Mensaje legible para el cliente",
  "fieldErrors": { "campo": "mensaje" }
}
```

- **code:** Código de error (ej. `BAD_REQUEST`, `NOT_FOUND`, `INTERNAL`, `SERVICE_UNAVAILABLE`, `FORBIDDEN`).
- **message:** Mensaje genérico; en 500 no se expone detalle interno ni stack.
- **fieldErrors:** Opcional; solo en validaciones por campo.

### Códigos HTTP y códigos de error

| HTTP   | code típico         | Uso                                      |
|--------|---------------------|------------------------------------------|
| 400    | BAD_REQUEST         | Parámetros inválidos, validación         |
| 401    | -                   | No autenticado                           |
| 403    | FORBIDDEN           | Sin permiso para el recurso              |
| 404    | NOT_FOUND           | Recurso no encontrado                    |
| 409    | CONFLICT            | Conflicto de negocio                     |
| 500    | INTERNAL            | Error inesperado; mensaje genérico       |
| 503    | SERVICE_UNAVAILABLE | Servicio externo no disponible           |

Los **ExceptionMappers** (`GenericExceptionMapper`, `RentexpresExceptionMapper`, `ParamConversionExceptionMapper`, `GroqApiExceptionMapper`, `WeatherApiExceptionMapper`) devuelven siempre un mensaje seguro al cliente en 500/503 (nunca stack ni mensaje interno). El detalle se registra solo en el servidor.

### Seguridad y depuración

- **Rate limiting:** El límite de peticiones por minuto es **por JVM (en memoria)**. Con una sola instancia el comportamiento es el esperado. Si se despliegan varias instancias detrás de un balanceador, cada instancia mantiene su propio contador; para un límite global entre instancias hay que usar un almacén compartido (por ejemplo Redis): implementar `RateLimitStore` (paquete `security`) con Redis y registrarlo en `RestApiBinder` en lugar de `InMemoryRateLimitStore`. Ver en el filtro la configuración `rate.limit.*` y en `config.properties.template` el comentario sobre `rate.limit.store`.
- **En respuestas 4xx/5xx** no se expone nunca el stack trace ni mensajes internos de excepciones; solo `ErrorResponseDTO` con `code` y `message` seguros.
- **Logs en servidor:** no incluir contraseñas, tokens JWT, códigos 2FA ni valores de `pickupCode` en los mensajes de log. Usar mensajes genéricos (ej. "Invalid or expired pickup code").

## Descargas de ficheros

El endpoint de **descarga de imágenes** (`GET /api/images/{imageId}/download`) valida permisos antes de redirigir a la URL de la imagen:

- **Usuario (USER):** el cliente solo puede descargar sus propias imágenes; ADMIN/EMPLOYEE pueden ver cualquiera.
- **Vehículo (VEHICLE) / Empleado (EMPLOYEE):** solo ADMIN/EMPLOYEE, o el propio empleado para sus imágenes.

Ver `docs/FILE_DOWNLOADS.md` para más detalle.
