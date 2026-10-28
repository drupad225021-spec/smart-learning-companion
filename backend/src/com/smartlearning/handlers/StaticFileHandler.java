package com.smartlearning.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StaticFileHandler implements HttpHandler {
    private final File webRoot;

    public StaticFileHandler(File webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpHelper.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        if (path == null || "/".equals(path) || path.isEmpty()) {
            path = "/index.html";
        }

        // Avoid directory traversal
        File requestedFile = new File(webRoot, path.substring(1)).getCanonicalFile();
        if (!requestedFile.getPath().startsWith(webRoot.getCanonicalPath())) {
            HttpHelper.sendError(exchange, 403, "Access denied");
            return;
        }

        if (!requestedFile.exists() || requestedFile.isDirectory()) {
            HttpHelper.sendError(exchange, 404, "File not found: " + path);
            return;
        }

        HttpHelper.enableCors(exchange);
        String mime = getMimeType(requestedFile.getName());
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, requestedFile.length());

        try (FileInputStream fis = new FileInputStream(requestedFile);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = fis.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
