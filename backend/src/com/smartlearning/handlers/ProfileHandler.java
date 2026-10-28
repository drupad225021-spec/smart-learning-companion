package com.smartlearning.handlers;

import com.smartlearning.auth.SessionManager;
import com.smartlearning.models.User;
import com.smartlearning.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileHandler implements HttpHandler {
    private final SessionManager sessionManager;

    public ProfileHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String token = HttpHelper.extractToken(exchange);

        if (!"GET".equalsIgnoreCase(method)) {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            if ("/api/profile/student".equals(path)) {
                User user = sessionManager.authorizeRole(token, "student");
                sendProfileResponse(exchange, user, "Student Profile: " + user.getName());
            } else if ("/api/profile/teacher".equals(path)) {
                User user = sessionManager.authorizeRole(token, "teacher");
                sendProfileResponse(exchange, user, "Teacher Console: " + user.getName());
            } else if ("/api/profile/parents".equals(path)) {
                User user = sessionManager.authorizeRole(token, "parents");
                sendProfileResponse(exchange, user, "Parent Portal: " + user.getName());
            } else {
                HttpHelper.sendError(exchange, 404, "Profile endpoint not found");
            }
        } catch (SecurityException se) {
            HttpHelper.sendError(exchange, 403, se.getMessage());
        } catch (Exception e) {
            HttpHelper.sendError(exchange, 500, "Error fetching profile: " + e.getMessage());
        }
    }

    private void sendProfileResponse(HttpExchange exchange, User user, String message) throws IOException {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", message);
        resp.put("profile", user.toProfileMap());
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }
}
