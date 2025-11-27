package com.pinguela.rentexpress.rest.api.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.pinguela.rentexpres.model.Results;
import com.pinguela.rentexpres.model.UserDTO;
import com.pinguela.rentexpres.service.UserService;
import com.pinguela.rentexpress.rest.api.UserResource;
import com.pinguela.rentexpress.rest.api.ZOpenUserResource;
import com.pinguela.rentexpress.rest.api.auth.security.AppSecurityContext;
import com.pinguela.rentexpress.rest.api.auth.security.UserAuth;
import com.pinguela.rentexpress.rest.api.auth.util.JwtUtil;
import com.pinguela.rentexpress.rest.api.support.JavaTimeParamConverterProvider;

import io.jsonwebtoken.Claims;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

public class UserResourceTest extends JerseyTest {

    @Mock
    private UserService userService;

    private AutoCloseable mocks;

    @Override
    protected Application configure() {
        mocks = MockitoAnnotations.openMocks(this);

        UserResource resource = new UserResource();
        ZOpenUserResource openResource = new ZOpenUserResource();
        injectMock(resource, "userService", userService);
        injectMock(openResource, "userService", userService);

        ResourceConfig rc = new ResourceConfig();
        rc.registerInstances(resource, openResource);
        rc.register(JavaTimeParamConverterProvider.class);
        rc.register(TestAuthorizationFilter.class);

        return rc;
    }

    @Override
    protected org.glassfish.jersey.test.spi.TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    @AfterEach
    public void tearDownMocks() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void findByIdReturnsOk() throws Exception {
        when(userService.findById(1)).thenReturn(new UserDTO());

        Response response = target("api/user/1").request()
                .header(HttpHeaders.AUTHORIZATION, buildUserToken("USER", Integer.valueOf(1)))
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void createReturnsOk() throws Exception {
        UserDTO user = new UserDTO();
        when(userService.create(Mockito.any(UserDTO.class))).thenReturn(true);
        when(userService.findById(null)).thenReturn(null);

        Response response = target("api/user").request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void updateReturnsOk() throws Exception {
        UserDTO user = new UserDTO();
        when(userService.update(Mockito.any(UserDTO.class))).thenReturn(true);
        when(userService.findById(1)).thenReturn(user);

        Response response = target("api/user/1").request()
                .header(HttpHeaders.AUTHORIZATION, buildUserToken("USER", Integer.valueOf(1)))
                .put(Entity.entity(user, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void deleteReturnsOk() throws Exception {
        when(userService.delete(1)).thenReturn(true);

        Response response = target("api/user/1").request()
                .header(HttpHeaders.AUTHORIZATION, buildUserToken("EMPLOYEE", Integer.valueOf(99)))
                .delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void findByCriteriaReturnsOk() throws Exception {
        Results<UserDTO> results = new Results<UserDTO>();
        results.setResults(Collections.singletonList(new UserDTO()));
        when(userService.findByCriteria(Mockito.any())).thenReturn(results);

        Response response = target("api/user/search")
                .queryParam("pageNumber", 1)
                .request()
                .header(HttpHeaders.AUTHORIZATION, buildUserToken("EMPLOYEE", Integer.valueOf(99)))
                .get();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void authenticateReturnsOk() throws Exception {
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("login", "user");
        credentials.put("password", "pass");

        when(userService.authenticate("user", "pass"))
                .thenReturn(new UserDTO());

        Response response = target("api/user/authenticate").request()
                .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    public void activateReturnsOk() throws Exception {
        when(userService.activate(1)).thenReturn(true);

        Response response = target("api/user/1/activate")
                .request()
                .header(HttpHeaders.AUTHORIZATION, buildUserToken("EMPLOYEE", Integer.valueOf(99)))
                .post(null);

        assertEquals(200, response.getStatus());
    }

    private void injectMock(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildUserToken(String role, Integer userId) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("roles", role);
        claims.put("userId", userId);
        String token = JwtUtil.generateToken("testUser", claims);
        return "Bearer " + token;
    }

    @Provider
    public static class TestAuthorizationFilter implements ContainerRequestFilter {

        public void filter(ContainerRequestContext requestContext) {
            String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return;
            }
            String token = authorization.substring("Bearer ".length()).trim();
            Set<String> roles = new HashSet<String>();
            try {
                Claims claims = JwtUtil.parseClaims(token);
                Object rolesClaim = claims.get("roles");
                if (rolesClaim != null) {
                    roles.add(rolesClaim.toString());
                }
                UserAuth principal = new UserAuth("testUser", roles);
                requestContext.setSecurityContext(new AppSecurityContext(principal, false));
            } catch (Exception e) {
                // Ignore for tests
            }
        }
    }
}
