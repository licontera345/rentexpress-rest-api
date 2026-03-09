package com.pinguela.rentexpress.rest.api.base;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.pinguela.rentexpres.exception.RentexpresException;
import com.pinguela.rentexpres.service.CrudService;
import com.pinguela.rentexpress.rest.api.util.ErrorResponseHelper;
import com.pinguela.rentexpress.rest.api.util.RedisCache;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Clase base para Resources que exponen CRUD estándar (findById, create, update, delete).
 * Reduce duplicación de validación de id/dto y mapeo de RentexpresException a Response.
 *
 * @param <D> tipo del DTO de dominio
 * @param <S> tipo del servicio que implementa CrudService&lt;D, Integer&gt;
 */
public abstract class BaseCrudResource<D, S extends CrudService<D, Integer>> {

    /**
     * Respuesta estándar para findById: 400 si id nulo, 404 si no encontrado, 200 con el DTO.
     */
    protected Response doFindById(Integer id, S service) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "ID is required");
        }
        D dto = service.findById(id);
        if (dto == null) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Not found");
        }
        return Response.ok(dto).build();
    }

    /**
     * Respuesta estándar para create: 400 si dto nulo o no creado, 201 con el DTO creado.
     * Invalida caché con el prefijo indicado.
     */
    protected Response doCreate(D dto, S service, Function<D, Integer> idGetter, String cachePrefix) throws RentexpresException {
        if (dto == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Data is required");
        }
        boolean created = service.create(dto);
        if (!created) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "Could not be created");
        }
        Integer id = idGetter.apply(dto);
        D createdDto = id != null ? service.findById(id) : dto;
        if (cachePrefix != null && !cachePrefix.isEmpty()) {
            RedisCache.deleteByPrefix(cachePrefix);
        }
        return Response.status(Status.CREATED).entity(createdDto).build();
    }

    /**
     * Respuesta estándar para update: 400 si id/dto nulos, aplica id al DTO, 404 si no actualizado, 200 con el DTO.
     * Invalida caché con el prefijo indicado.
     */
    protected Response doUpdate(Integer id, D dto, S service, BiConsumer<D, Integer> idSetter, String cachePrefix) throws RentexpresException {
        if (id == null || dto == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "ID and data are required");
        }
        idSetter.accept(dto, id);
        boolean updated = service.update(dto);
        if (!updated) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Not found or not updated");
        }
        D updatedDto = service.findById(id);
        if (cachePrefix != null && !cachePrefix.isEmpty()) {
            RedisCache.deleteByPrefix(cachePrefix);
        }
        return Response.ok(updatedDto).build();
    }

    /**
     * Respuesta estándar para delete: 400 si id nulo, 404 si no eliminado, 200 con mensaje.
     * Invalida caché con el prefijo indicado.
     */
    protected Response doDelete(Integer id, S service, String cachePrefix) throws RentexpresException {
        if (id == null) {
            return ErrorResponseHelper.badRequest("BAD_REQUEST", "ID is required");
        }
        boolean deleted = service.delete(id);
        if (!deleted) {
            return ErrorResponseHelper.notFound("NOT_FOUND", "Not found");
        }
        if (cachePrefix != null && !cachePrefix.isEmpty()) {
            RedisCache.deleteByPrefix(cachePrefix);
        }
        return ErrorResponseHelper.ok("OK", "Deleted successfully");
    }
}
