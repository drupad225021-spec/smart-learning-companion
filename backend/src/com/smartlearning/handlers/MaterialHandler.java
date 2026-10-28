package com.smartlearning.handlers;

import com.smartlearning.auth.SessionManager;
import com.smartlearning.models.StudyMaterial;
import com.smartlearning.models.User;
import com.smartlearning.server.DataStore;
import com.smartlearning.util.JsonUtil;
import com.smartlearning.util.MultipartParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

public class MaterialHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final DataStore dataStore;

    public MaterialHandler(SessionManager sessionManager, DataStore dataStore) {
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
            if ("/api/materials".equals(path) && "GET".equalsIgnoreCase(method)) {
                // Anyone logged in can view materials
                if (token == null || sessionManager.getUserByToken(token) == null) {
                    HttpHelper.sendError(exchange, 401, "Unauthorized: Please log in to view study materials");
                    return;
                }
                handleGetMaterials(exchange);
            } else if ("/api/teacher/materials".equals(path) && "POST".equalsIgnoreCase(method)) {
                // Enforce Teacher Profile Separation
                User teacher = sessionManager.authorizeRole(token, "teacher");
                handleUploadMaterial(exchange, teacher);
            } else if ("/api/teacher/materials".equals(path) && "DELETE".equalsIgnoreCase(method)) {
                sessionManager.authorizeRole(token, "teacher");
                handleDeleteMaterial(exchange);
            } else if (path.startsWith("/api/download/")) {
                handleDownloadFile(exchange, path.substring("/api/download/".length()));
            } else {
                HttpHelper.sendError(exchange, 404, "Endpoint not found");
            }
        } catch (SecurityException se) {
            HttpHelper.sendError(exchange, 403, se.getMessage());
        } catch (Exception e) {
            HttpHelper.sendError(exchange, 500, "Error handling materials: " + e.getMessage());
        }
    }

    private void handleGetMaterials(HttpExchange exchange) throws IOException {
        List<StudyMaterial> list = dataStore.getMaterials();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (StudyMaterial m : list) {
            mapList.add(m.toMap());
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("count", list.size());
        resp.put("materials", mapList);
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private void handleUploadMaterial(HttpExchange exchange, User teacher) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

        String title = "Study Material";
        String subject = "Computer Science";
        String category = "CS";
        String description = "";
        String filename = "material-" + System.currentTimeMillis() + ".pdf";
        String originalFilename = filename;
        byte[] fileBytes = null;

        if (contentType != null && contentType.contains("multipart/form-data")) {
            List<MultipartParser.Part> parts = MultipartParser.parse(exchange.getRequestBody(), contentType);
            for (MultipartParser.Part part : parts) {
                if ("title".equals(part.getName())) {
                    title = part.getStringValue();
                } else if ("subject".equals(part.getName())) {
                    subject = part.getStringValue();
                } else if ("category".equals(part.getName())) {
                    category = part.getStringValue();
                } else if ("description".equals(part.getName())) {
                    description = part.getStringValue();
                } else if ("file".equals(part.getName()) || part.getFilename() != null) {
                    if (part.getFilename() != null && !part.getFilename().isEmpty()) {
                        originalFilename = part.getFilename();
                    }
                    fileBytes = part.getData();
                }
            }
        } else {
            // Support JSON with Base64 payload
            String body = HttpHelper.readBody(exchange);
            Map<String, Object> req = JsonUtil.parseObject(body);
            if (req.get("title") != null) title = (String) req.get("title");
            if (req.get("subject") != null) subject = (String) req.get("subject");
            if (req.get("category") != null) category = (String) req.get("category");
            if (req.get("description") != null) description = (String) req.get("description");
            if (req.get("filename") != null) {
                originalFilename = (String) req.get("filename");
            }
            if (req.get("fileBase64") != null) {
                String b64 = (String) req.get("fileBase64");
                if (b64.contains(",")) {
                    b64 = b64.substring(b64.indexOf(",") + 1);
                }
                fileBytes = Base64.getDecoder().decode(b64);
            }
        }

        if (title.trim().isEmpty()) {
            HttpHelper.sendError(exchange, 400, "Title is required for study material");
            return;
        }

        // Clean filename and create unique stored name
        String safeName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedFilename = System.currentTimeMillis() + "_" + safeName;
        File targetFile = new File(dataStore.getUploadsDir(), storedFilename);

        if (fileBytes != null && fileBytes.length > 0) {
            Files.write(targetFile.toPath(), fileBytes);
        } else {
            // Default placeholder text document if empty file uploaded
            String defaultText = "Study Material Handout: " + title + "\nSubject: " + subject + "\nUploaded by: " + teacher.getName() + "\n\n" + description;
            Files.write(targetFile.toPath(), defaultText.getBytes());
        }

        String fileSizeFormatted = formatFileSize(targetFile.length());
        String downloadUrl = "/api/download/" + storedFilename;

        StudyMaterial material = new StudyMaterial(
                0,
                title.trim(),
                subject.trim(),
                category.trim(),
                description.trim(),
                storedFilename,
                originalFilename,
                fileSizeFormatted,
                teacher.getName(),
                LocalDate.now().toString(),
                downloadUrl
        );

        StudyMaterial saved = dataStore.addMaterial(material);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Study material uploaded successfully");
        resp.put("material", saved.toMap());
        HttpHelper.sendJson(exchange, 201, JsonUtil.toJson(resp));
    }

    private void handleDownloadFile(HttpExchange exchange, String filename) throws IOException {
        // Sanitize to prevent path traversal
        filename = new File(filename).getName();
        File file = new File(dataStore.getUploadsDir(), filename);

        if (!file.exists() || !file.isFile()) {
            HttpHelper.sendError(exchange, 404, "File not found: " + filename);
            return;
        }

        HttpHelper.enableCors(exchange);
        String mime = getMimeType(filename);
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = fis.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
        }
    }

    private void handleDeleteMaterial(HttpExchange exchange) throws IOException {
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

        boolean deleted = dataStore.deleteMaterial(id);
        if (!deleted) {
            HttpHelper.sendError(exchange, 404, "Material not found");
            return;
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Study material deleted");
        HttpHelper.sendJson(exchange, 200, JsonUtil.toJson(resp));
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }
}
