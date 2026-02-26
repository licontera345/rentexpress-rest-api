# Plan de Accion - Backend REST API: Clima + Recomendacion IA

## Objetivo
Crear dos endpoints proxy que ocultan los proveedores externos (OpenWeatherMap y Groq)
al frontend. El cliente React solo ve `/open/weather` y `/open/recommendations`.

---

## Tarea 1: Configuracion
- [ ] Anadir en `config.properties`:
  - `weather.api.key`, `weather.api.url`
  - `groq.api.key`, `groq.api.url`, `groq.model`

## Tarea 2: Servicio del Clima
- [ ] Crear `dto/WeatherDTO.java` (city, temp, tempMin, tempMax, humidity, description, icon)
- [ ] Crear `service/WeatherService.java` (HttpClient -> OpenWeatherMap, parseo con Gson)
- [ ] Crear `WeatherResource.java` - `GET /open/weather?city={city}` (publico, sin @Secured)

## Tarea 3: Servicio de Recomendacion IA
- [ ] Crear `dto/RecommendationRequestDTO.java` (destination, passengers, tripDuration, roadCondition, vehicles)
- [ ] Crear `dto/VehicleSummaryDTO.java` (vehicleId, brand, model, categoryName, dailyPrice)
- [ ] Crear `dto/RecommendationResponseDTO.java` (recommendedVehicleIds, explanation)
- [ ] Crear `service/GroqService.java` (HttpClient -> Groq API, prompt de experto, parseo JSON)
- [ ] Crear `RecommendationResource.java` - `POST /open/recommendations` (publico, sin @Secured)

## Principios
- NUNCA exponer nombre del proveedor ni API keys al frontend
- Errores externos se capturan y devuelven como mensajes genericos
- Los DTOs son modelos propios, sin estructura del proveedor
