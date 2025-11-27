package com.pinguela.rentexpress.rest.api.util;

/**
 * Utility class to resolve the requested language iso code from different sources.
 */
public final class LanguageResolver {

    private LanguageResolver() {
    }

    /**
     * Determines the iso code to use based on the query parameter or the Accept-Language header.
     *
     * @param isoCodeParam     iso code provided as query parameter.
     * @param acceptLanguage   Accept-Language header value.
     * @return the resolved iso code or {@code null} if none was provided.
     */
    public static String resolveIsoCode(String isoCodeParam, String acceptLanguage) {
        if (isoCodeParam != null && !isoCodeParam.trim().isEmpty()) {
            return isoCodeParam.trim();
        }

        if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
            String value = acceptLanguage.trim();
            int commaIndex = value.indexOf(',');
            if (commaIndex > -1) {
                value = value.substring(0, commaIndex);
            }
            int semicolonIndex = value.indexOf(';');
            if (semicolonIndex > -1) {
                value = value.substring(0, semicolonIndex);
            }
            value = value.trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        return null;
    }
}
