package com.pinguela.rentexpress.rest.api;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

public class ApiEndpointCoverageTest {

    private static final List<Class<?>> RESOURCE_CLASSES = Arrays.asList(
            FileResource.class,
            ZOpenAddressResourse.class,
            ZOpenCityResourse.class,
            ZOpenEmployeeResourse.class,
            ZOpenHeadquartersResourse.class,
            ZOpenProvinceResourse.class,
            ZOpenRentalResourse.class,
            ZOpenRentalStatusResourse.class,
            ZOpenReservationResourse.class,
            ZOpenReservationStatusResourse.class,
            ZOpenRoleResourse.class,
            ZOpenUserResourse.class,
            ZOpenVehicleCategoryResourse.class,
            ZOpenVehicleResourse.class,
            ZOpenVehicleStatusResourse.class
    );

    private static final List<EndpointExpectation> EXPECTATIONS = Arrays.asList(
            expect("GET", "/file/vehicle/{vehicleId}"),
            expect("GET", "/file/vehicle/{vehicleId}/{imageName}"),
            expect("POST", "/file/vehicle/{vehicleId}"),
            expect("DELETE", "/file/vehicle/{vehicleId}/{imageName}"),
            expect("GET", "/file/user-avatar/{userId}"),
            expect("POST", "/file/user-avatar/{userId}"),
            expect("GET", "/file/employee-avatar/{employeeId}"),
            expect("POST", "/file/employee-avatar/{employeeId}"),

            expect("GET", "/addresses/{id}"),
            expect("POST", "/addresses"),
            expectWithTrailingParam("PUT", "/addresses"),
            expectWithTrailingParam("DELETE", "/addresses"),

            expect("GET", "/cities"),
            expect("GET", "/cities/{id}"),
            expect("GET", "/cities/province/{provinceId}"),
            expect("POST", "/cities"),
            expectWithTrailingParam("PUT", "/cities"),
            expectWithTrailingParam("DELETE", "/cities"),

            expect("GET", "/employees/{id}"),
            expect("POST", "/employees"),
            expectWithTrailingParam("PUT", "/employees"),
            expect("DELETE", "/employees/{id}"),
            expect("POST", "/employees/search"),
            expect("POST", "/employees/authenticate"),
            expect("POST", "/employees/{id}/activate"),

            expect("GET", "/headquarters"),
            expect("GET", "/headquarters/{id}"),
            expect("POST", "/headquarters"),
            expectWithTrailingParam("PUT", "/headquarters"),
            expect("DELETE", "/headquarters/{id}"),

            expect("GET", "/provinces"),
            expect("GET", "/provinces/{id}"),
            expect("POST", "/provinces"),
            expectWithTrailingParam("PUT", "/provinces"),
            expect("DELETE", "/provinces/{id}"),

            expect("GET", "/rentals/{id}"),
            expect("POST", "/rentals"),
            expectWithTrailingParam("PUT", "/rentals"),
            expect("DELETE", "/rentals/{id}"),
            expect("POST", "/rentals/search"),
            expect("GET", "/rentals/reservations/{reservationId}/exists"),
            expect("POST", "/rentals/from-reservation"),
            expect("POST", "/rentals/auto-convert"),

            expect("GET", "/rental-statuses"),
            expect("GET", "/rental-statuses/{id}"),

            expect("GET", "/reservations/{id}"),
            expect("POST", "/reservations"),
            expectWithTrailingParam("PUT", "/reservations"),
            expect("DELETE", "/reservations/{id}"),
            expect("POST", "/reservations/search"),

            expect("GET", "/reservation-statuses"),
            expect("GET", "/reservation-statuses/{id}"),

            expect("GET", "/roles"),
            expect("GET", "/roles/{id}"),

            expect("GET", "/users/{id}"),
            expect("POST", "/users"),
            expectWithTrailingParam("PUT", "/users"),
            expect("DELETE", "/users/{id}"),
            expect("POST", "/users/search"),
            expect("POST", "/users/authenticate"),
            expect("POST", "/users/{id}/activate"),

            expect("GET", "/vehicle-categories"),
            expect("GET", "/vehicle-categories/{id}"),

            expect("GET", "/vehicles"),
            expect("GET", "/vehicles/{id}"),
            expect("POST", "/vehicles"),
            expectWithTrailingParam("PUT", "/vehicles"),
            expect("DELETE", "/vehicles/{id}"),
            expect("POST", "/vehicles/search"),

            expect("GET", "/vehicle-statuses"),
            expect("GET", "/vehicle-statuses/{id}")
    );

    @org.junit.Test
    public void shouldExposeAllDocumentedEndpoints() {
        List<Endpoint> actualEndpoints = collectEndpoints();

        for (EndpointExpectation expectation : EXPECTATIONS) {
            boolean found = actualEndpoints.stream()
                    .anyMatch(endpoint -> endpoint.method.equals(expectation.method)
                            && expectation.pattern.matcher(endpoint.path).matches());

            assertTrue("Missing endpoint: " + expectation.method + " " + expectation.description, found);
        }
    }

    private static List<Endpoint> collectEndpoints() {
        List<Endpoint> endpoints = new ArrayList<>();
        for (Class<?> resourceClass : RESOURCE_CLASSES) {
            Path classPath = resourceClass.getAnnotation(Path.class);
            if (classPath == null) {
                continue;
            }
            String basePath = normalizePath(classPath.value());

            Arrays.stream(resourceClass.getDeclaredMethods())
                    .filter(ApiEndpointCoverageTest::hasHttpMethod)
                    .forEach(method -> {
                        String methodPath = "";
                        Path methodAnnotation = method.getAnnotation(Path.class);
                        if (methodAnnotation != null) {
                            methodPath = methodAnnotation.value();
                        }
                        String fullPath = joinPaths(basePath, methodPath);

                        for (String httpMethod : supportedHttpMethods(method)) {
                            endpoints.add(new Endpoint(httpMethod, fullPath));
                        }
                    });
        }
        return endpoints;
    }

    private static boolean hasHttpMethod(java.lang.reflect.Method method) {
        return method.isAnnotationPresent(GET.class)
                || method.isAnnotationPresent(POST.class)
                || method.isAnnotationPresent(PUT.class)
                || method.isAnnotationPresent(DELETE.class);
    }

    private static List<String> supportedHttpMethods(java.lang.reflect.Method method) {
        List<String> methods = new ArrayList<>();
        if (method.isAnnotationPresent(GET.class)) {
            methods.add("GET");
        }
        if (method.isAnnotationPresent(POST.class)) {
            methods.add("POST");
        }
        if (method.isAnnotationPresent(PUT.class)) {
            methods.add("PUT");
        }
        if (method.isAnnotationPresent(DELETE.class)) {
            methods.add("DELETE");
        }
        return methods;
    }

    private static String joinPaths(String basePath, String methodPath) {
        String normalizedBase = normalizePath(basePath);
        String normalizedMethod = normalizeMethodPath(methodPath);
        return normalizedBase + normalizedMethod;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String normalizeMethodPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static EndpointExpectation expect(String method, String path) {
        return new EndpointExpectation(method, buildPattern(path, false), path);
    }

    private static EndpointExpectation expectWithTrailingParam(String method, String path) {
        return new EndpointExpectation(method, buildPattern(path, true), path + " (allowing trailing parameter)");
    }

    private static Pattern buildPattern(String path, boolean allowTrailingParam) {
        String normalized = normalizePath(path);
        String regex = Pattern.quote(normalized);
        if (allowTrailingParam) {
            regex = regex + "(?:/\\{[^/]+\\})?";
        }
        return Pattern.compile("^" + regex + "$", Pattern.CASE_INSENSITIVE);
    }

    private static class Endpoint {
        private final String method;
        private final String path;

        Endpoint(String method, String path) {
            this.method = method;
            this.path = path;
        }
    }

    private static class EndpointExpectation {
        private final String method;
        private final Pattern pattern;
        private final String description;

        EndpointExpectation(String method, Pattern pattern, String description) {
            this.method = method;
            this.pattern = pattern;
            this.description = description;
        }
    }
}
