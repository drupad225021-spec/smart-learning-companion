package com.smartlearning.handlers;

import com.smartlearning.auth.SessionManager;
import com.smartlearning.models.User;
import com.smartlearning.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final SessionManager sessionManager;

    public AuthHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/api/login".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleLogin(exchange);
        } else if ("/api/logout".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleLogout(exchange);
        } else if ("/api/me".equals(path) && "GET".equalsIgnoreCase(method)) {
            handleMe(exchange);
        } else {
            HttpHelper.sendError(exchange, 404, "Endpoint not found");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);
        String role = (String) req.get("role");
        String password = (String) req.get("password");

        if (role == null || role.trim().isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Role is required");
            return;
        }

        Map<String, Object> authData = sessionManager.authenticate(role, password);
        if (authData == null) {
            HttpHelper.sendError(exchange, 401, "Invalid password for role " + role);
            return;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("token", authData.get("token"));
        resp.put("user", authData.get("user"));
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = HttpHelper.extractToken(exchange);
        if (token != null) {
            sessionManager.invalidateToken(token);
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Logged out successfully");
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        String token = HttpHelper.extractToken(exchange);
        if (token == null) {
            HttpHelper.sendError(exchange, 401, "Unauthorized: No token provided");
            return;
        }
        User user = sessionManager.getUserByToken(token);
        if (user == null) {
            HttpHelper.sendError(exchange, 401, "Unauthorized: Session invalid or expired");
            return;
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("user", user.toProfileMap());
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }
}
