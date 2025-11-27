package com.pinguela.rentexpress.rest.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Utility class for request parameter validation in REST resources.
 */
public final class ResourceValidationUtils {

    private ResourceValidationUtils() {
    }

    public static boolean hasInvalidPagination(Integer pageNumber, Integer pageSize) {
        if (pageNumber != null && pageNumber.intValue() < 0) {
            return true;
        }
        if (pageSize != null && pageSize.intValue() <= 0) {
            return true;
        }
        return pageNumber != null && pageSize == null;
    }

    public static boolean isInvalidIntegerRange(Integer min, Integer max) {
        return min != null && max != null && min.intValue() > max.intValue();
    }

    public static boolean isInvalidDecimalRange(BigDecimal min, BigDecimal max) {
        return min != null && max != null && min.compareTo(max) > 0;
    }

    public static boolean isInvalidDateRange(LocalDateTime from, LocalDateTime to) {
        return from != null && to != null && from.isAfter(to);
    }
}
