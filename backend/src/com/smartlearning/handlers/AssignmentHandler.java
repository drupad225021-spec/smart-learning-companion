package com.smartlearning.handlers;

import com.smartlearning.auth.SessionManager;
import com.smartlearning.models.Assignment;
import com.smartlearning.models.User;
import com.smartlearning.server.DataStore;
import com.smartlearning.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class AssignmentHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final DataStore dataStore;

    public AssignmentHandler(SessionManager sessionManager, DataStore dataStore) {
        this.sessionManager = sessionManager;
        this.dataStore = dataStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String token = HttpHelper.extractToken(exchange);

        try {
            if ("/api/assignments".equals(path) && "GET".equalsIgnoreCase(method)) {
                // Must be logged in (any role can view assignments)
                if (token == null || sessionManager.getUserByToken(token) == null) {
                    HttpHelper.sendError(exchange, 401, "Unauthorized: Please log in to view assignments");
                    return;
                }
                handleGetAssignments(exchange);
            } else if ("/api/teacher/assignments".equals(path) && "POST".equalsIgnoreCase(method)) {
                // Enforce Teacher Profile Separation
                User teacher = sessionManager.authorizeRole(token, "teacher");
                handleCreateAssignment(exchange, teacher);
            } else if ("/api/teacher/assignments".equals(path) && "DELETE".equalsIgnoreCase(method)) {
                // Enforce Teacher Profile Separation
                sessionManager.authorizeRole(token, "teacher");
                handleDeleteAssignment(exchange);
            } else if ("/api/student/assignments/toggle".equals(path) && "POST".equalsIgnoreCase(method)) {
                // Enforce Student or Teacher can toggle
                if (token == null || sessionManager.getUserByToken(token) == null) {
                    HttpHelper.sendError(exchange, 401, "Unauthorized: Please log in to complete assignments");
                    return;
                }
                handleToggleAssignment(exchange);
            } else {
                HttpHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (SecurityException se) {
            HttpHelper.sendError(exchange, 403, se.getMessage());
        } catch (Exception e) {
            HttpHelper.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAssignments(HttpExchange exchange) throws IOException {
        List<Assignment> list = dataStore.getAssignments();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Assignment a : list) {
            mapList.add(a.toMap());
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("count", list.size());
        resp.put("assignments", mapList);
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleCreateAssignment(HttpExchange exchange, User teacher) throws IOException {
        String body = HttpHelper.readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        String title = (String) req.get("title");
        String subject = (String) req.get("subject");
        String dueDate = (String) req.get("dueDate");
        String urgency = (String) req.get("urgency");
        String weight = (String) req.get("weight");
        String description = (String) req.get("description");

        if (title == null || title.trim().isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Assignment title is required");
            return;
        }

        if (subject == null || subject.trim().isEmpty()) {
            subject = "Computer Science • Prof. Jissmol";
        }
        if (dueDate == null || dueDate.trim().isEmpty()) {
            dueDate = "Next Week";
        }
        if (urgency == null || urgency.trim().isEmpty()) {
            urgency = "urgent";
        }
        if (weight == null || weight.trim().isEmpty()) {
            weight = "15% of Grade";
        }

        String urgencyText = "urgent".equalsIgnoreCase(urgency) ? "Due Soon" : "Upcoming";

        Assignment assignment = new Assignment(
                0,
                title.trim(),
                subject.trim(),
                dueDate.trim(),
                urgency.trim(),
                urgencyText,
                weight.trim(),
                false,
                description != null ? description.trim() : "",
                teacher.getName(),
                LocalDate.now().toString()
        );

        Assignment saved = dataStore.addAssignment(assignment);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Assignment published successfully");
        resp.put("assignment", saved.toMap());
        HttpHelper.sendJson(exchange, 201, JsonUtil.toJson(resp));
    }

    private void handleToggleAssignment(HttpExchange exchange) throws IOException {
        String body = HttpHelper.readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        long id = 0;
        if (req.get("id") instanceof Number) {
            id = ((Number) req.get("id")).longValue();
        }

        if (id <= 0) {
            HttpHelper.sendError(exchange, 400, "Valid assignment ID is required");
            return;
        }

        boolean toggled = dataStore.toggleAssignment(id);
        if (!toggled) {
            HttpHelper.sendError(exchange, 404, "Assignment not found");
            return;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Assignment status updated");
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleDeleteAssignment(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        long id = -1;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "id".equalsIgnoreCase(pair[0])) {
                    try { id = Long.parseLong(pair[1]); } catch (Exception ignored) {}
                }
            }
        }

        if (id <= 0) {
            HttpHelper.sendError(exchange, 400, "Missing or invalid id query parameter");
            return;
        }

        boolean deleted = dataStore.deleteAssignment(id);
        if (!deleted) {
            HttpHelper.sendError(exchange, 404, "Assignment not found");
            return;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Assignment removed successfully");
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }
}
