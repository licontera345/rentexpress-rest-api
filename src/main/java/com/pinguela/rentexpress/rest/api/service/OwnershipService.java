package com.pinguela.rentexpress.rest.api.service;

public interface OwnershipService {

    boolean checkOwnership(String entityName, String resourceId, String ownerId);
}
