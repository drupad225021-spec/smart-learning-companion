package com.smartlearning.handlers;

import com.smartlearning.auth.SessionManager;
import com.smartlearning.models.AttendanceRecord;
import com.smartlearning.models.AttendanceSession;
import com.smartlearning.models.User;
import com.smartlearning.server.DataStore;
import com.smartlearning.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AttendanceHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final DataStore dataStore;

    public AttendanceHandler(SessionManager sessionManager, DataStore dataStore) {
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
            if ("/api/attendance/sessions".equals(path) && "GET".equalsIgnoreCase(method)) {
                // Anyone authenticated can view sessions list / calendar
                if (token == null || sessionManager.getUserByToken(token) == null) {
                    HttpHelper.sendError(exchange, 401, "Unauthorized: Please log in to view attendance");
                    return;
                }
                handleGetSessions(exchange);
            } else if ("/api/attendance/submit".equals(path) && "POST".equalsIgnoreCase(method)) {
                // Only Teacher can submit or take attendance
                User teacher = sessionManager.authorizeRole(token, "teacher");
                handleSubmitAttendance(exchange, teacher);
            } else if ("/api/attendance/student".equals(path) && "GET".equalsIgnoreCase(method)) {
                // Student or Parent or Teacher can view student attendance summary
                User currentUser = sessionManager.getUserByToken(token);
                if (currentUser == null) {
                    HttpHelper.sendError(exchange, 401, "Unauthorized: Please log in to view attendance");
                    return;
                }
                handleGetStudentAttendance(exchange, currentUser);
            } else if ("/api/attendance/session".equals(path) && "DELETE".equalsIgnoreCase(method)) {
                // Only Teacher can delete an attendance session
                sessionManager.authorizeRole(token, "teacher");
                handleDeleteSession(exchange);
            } else {
                HttpHelper.sendError(exchange, 404, "Attendance endpoint not found");
            }
        } catch (SecurityException se) {
            HttpHelper.sendError(exchange, 403, se.getMessage());
        } catch (Exception e) {
            HttpHelper.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetSessions(HttpExchange exchange) throws IOException {
        List<AttendanceSession> sessions = dataStore.getAttendanceSessions();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (AttendanceSession s : sessions) {
            mapList.add(s.toMap());
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("count", sessions.size());
        resp.put("sessions", mapList);
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    @SuppressWarnings("unchecked")
    private void handleSubmitAttendance(HttpExchange exchange, User teacher) throws IOException {
        String body = HttpHelper.readBody(exchange);
        Map<String, Object> req = JsonUtil.parseObject(body);

        String id = (String) req.get("id");
        String date = (String) req.get("date");
        String classId = (String) req.get("classId");
        String className = (String) req.get("className");
        String subject = (String) req.get("subject");
        String period = (String) req.get("period");

        if (date == null || date.trim().isEmpty()) {
            date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());
        }
        if (classId == null || classId.trim().isEmpty()) {
            classId = "s3_csb";
        }
        if (className == null || className.trim().isEmpty()) {
            className = "S3 CSB";
        }
        if (subject == null || subject.trim().isEmpty()) {
            subject = "Computer Science";
        }
        if (period == null || period.trim().isEmpty()) {
            period = "Period 1 (09:00 - 10:00 AM)";
        }

        List<AttendanceRecord> records = new ArrayList<>();
        Object rawRecords = req.get("records");
        if (rawRecords instanceof List) {
            for (Object item : (List<Object>) rawRecords) {
                if (item instanceof Map) {
                    records.add(AttendanceRecord.fromMap((Map<String, Object>) item));
                }
            }
        }

        if (records.isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Attendance submission must include student records");
            return;
        }

        AttendanceSession session = new AttendanceSession(
                id,
                date.trim(),
                classId.trim(),
                className.trim(),
                subject.trim(),
                period.trim(),
                teacher.getName(),
                LocalDateTime.now().toString(),
                records
        );

        AttendanceSession saved = dataStore.recordAttendance(session);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Attendance saved and distributed successfully");
        resp.put("session", saved.toMap());
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleGetStudentAttendance(HttpExchange exchange, User currentUser) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String studentId = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "studentId".equalsIgnoreCase(pair[0])) {
                    studentId = pair[1].trim();
                }
            }
        }

        // Default to student's own ID or Drupad (SLC-9041) for parent view
        if (studentId == null || studentId.isEmpty()) {
            if ("student".equalsIgnoreCase(currentUser.getRole())) {
                studentId = currentUser.getId();
            } else {
                studentId = "SLC-9041";
            }
        }

        Map<String, Object> summary = dataStore.getStudentAttendanceSummary(studentId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("summary", summary);
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleDeleteSession(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String id = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "id".equalsIgnoreCase(pair[0])) {
                    id = pair[1].trim();
                }
            }
        }

        if (id == null || id.isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Missing session id parameter");
            return;
        }

        boolean deleted = dataStore.deleteAttendanceSession(id);
        if (!deleted) {
            HttpHelper.sendError(exchange, 404, "Attendance session not found");
            return;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Attendance session removed successfully");
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }
}
