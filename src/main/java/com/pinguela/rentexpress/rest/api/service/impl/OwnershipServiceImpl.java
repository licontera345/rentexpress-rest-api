package com.pinguela.rentexpress.rest.api.service.impl;

import com.pinguela.rentexpress.rest.api.service.OwnershipService;

public class OwnershipServiceImpl implements OwnershipService {

    @Override
    public boolean checkOwnership(String entityName, String resourceId, String ownerId) {
        switch (entityName) {
        case "CLIENT_MATCH":
            return matchesOwner(resourceId, ownerId);
        case "EMPLOYEE_MATCH":
            return matchesOwner(resourceId, ownerId);
        default:
            return false;
        }
    }

    private boolean matchesOwner(String resourceId, String ownerId) {
        return resourceId != null && resourceId.equals(ownerId);
    }
}
