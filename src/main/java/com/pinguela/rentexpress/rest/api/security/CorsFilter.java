package com.pinguela.rentexpress.rest.api.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        // Permitir peticiones desde cualquier origen (para desarrollo)
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        
        // Métodos HTTP permitidos
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        
        // Headers permitidos
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        
        // Permitir credenciales (cookies, authorization headers)
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        
        // Tiempo de caché para preflight requests
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
    }
}