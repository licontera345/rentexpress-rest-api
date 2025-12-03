package com.pinguela.rentexpressweb.controller;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@WebServlet(urlPatterns = {"/public/nero/consumesapi"})
public class ConsumesApi extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Client client;
    private ObjectMapper mapper;
    private String NERO_BASE;

    @Override
    public void init() {
        client = ClientBuilder.newClient()
                .register(org.glassfish.jersey.jackson.JacksonFeature.class);
        mapper = new ObjectMapper();
        NERO_BASE = getServletContext().getInitParameter("nero.api.base");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            req.getRequestDispatcher("/public/nero/nero_login.jsp").forward(req, resp);
            return;
        }
        if ("new".equals(action)) {
            if (req.getSession().getAttribute("neroClient") == null) {
                resp.sendRedirect(req.getContextPath() + "/public/nero/consumesapi");
                return;
            }
            req.getRequestDispatcher("/public/nero/nero_appointment_create.jsp").forward(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("login".equals(action)) {
            login(req, resp);
            return;
        }
        if ("create_appointment".equals(action)) {
            createAppointment(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String user = req.getParameter("usernameOrEmail");
        String pass = req.getParameter("password");
        if (isBlank(user) || isBlank(pass)) {
            req.setAttribute("loginError", "Usuario/Email y contraseña requeridos.");
            req.getRequestDispatcher("/public/nero/nero_login.jsp").forward(req, resp);
            return;
        }

        WebTarget t = client.target(NERO_BASE)
                .path("client").path("athenticate").path(user)
                .queryParam("password", pass);

        Response r = t.request(MediaType.APPLICATION_JSON).get();
        if (r.getStatus() == 200) {
            String json = r.readEntity(String.class);
            JsonNode neroClient = mapper.readTree(json);
            req.getSession(true).setAttribute("neroClient", neroClient);
            resp.sendRedirect(req.getContextPath() + "/public/nero/consumesapi?action=new");
        } else if (r.getStatus() == 404) {
            req.setAttribute("loginError", "Credenciales incorrectas o cliente no existe.");
            req.getRequestDispatcher("/public/nero/nero_login.jsp").forward(req, resp);
        } else {
            req.setAttribute("loginError", "Error de login. Código: " + r.getStatus());
            req.getRequestDispatcher("/public/nero/nero_login.jsp").forward(req, resp);
        }
        r.close();
    }

    private void createAppointment(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JsonNode clientNode = (JsonNode) req.getSession().getAttribute("neroClient");
        if (clientNode == null) {
            resp.sendRedirect(req.getContextPath() + "/public/nero/consumesapi");
            return;
        }

        String headquarters = req.getParameter("headquarters");
        String veterinarians = req.getParameter("veterinarians");
        String animals = req.getParameter("animals");
        String dateTime = req.getParameter("dateTime");
        String details = req.getParameter("details");

        if (isBlank(headquarters) || isBlank(dateTime)) {
            req.setAttribute("error", "Selecciona sede y fecha/hora.");
            req.getRequestDispatcher("/public/nero/nero_appointment_create.jsp").forward(req, resp);
            return;
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("headquartersId", parseInt(headquarters));
        payload.put("dateTime", dateTime);
        payload.put("details", details == null ? "" : details);

        int clientId = clientNode.path("id").asInt(0);
        if (clientId > 0) {
            payload.put("clientId", clientId);
        }
        if (!isBlank(animals)) {
            payload.put("animalId", parseInt(animals));
        }
        if (!isBlank(veterinarians)) {
            payload.put("veterinarianId", parseInt(veterinarians));
        }

        WebTarget t = client.target(NERO_BASE).path("open").path("appointment");
        Response r = t.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

        if (r.getStatus() == 200 || r.getStatus() == 201) {
            req.setAttribute("success", "Cita creada correctamente.");
            req.setAttribute("confirmationJson", r.readEntity(String.class));
        } else {
            req.setAttribute("error", "No se pudo crear la cita. Código: " + r.getStatus());
            req.setAttribute("confirmationJson", safe(r));
        }
        r.close();
        req.getRequestDispatcher("/public/nero/nero_appointment_create.jsp").forward(req, resp);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String safe(Response r) {
        try {
            return r.readEntity(String.class);
        } catch (Exception e) {
            return "";
        }
    }
}
