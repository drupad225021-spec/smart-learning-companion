package com.smartlearning.server;

import com.smartlearning.auth.SessionManager;
import com.smartlearning.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServerApp {
    private final int port;
    private final String workspaceDir;
    private HttpServer server;

    public HttpServerApp(int port, String workspaceDir) {
        this.port = port;
        this.workspaceDir = workspaceDir;
    }

    public void start() throws IOException {
        String backendDir = new File(workspaceDir, "backend").getAbsolutePath();
        SessionManager sessionManager = new SessionManager();
        DataStore dataStore = new DataStore(backendDir);

        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Handlers
        AuthHandler authHandler = new AuthHandler(sessionManager);
        AssignmentHandler assignmentHandler = new AssignmentHandler(sessionManager, dataStore);
        MaterialHandler materialHandler = new MaterialHandler(sessionManager, dataStore);
        ProfileHandler profileHandler = new ProfileHandler(sessionManager);
        AttendanceHandler attendanceHandler = new AttendanceHandler(sessionManager, dataStore);
        StaticFileHandler staticFileHandler = new StaticFileHandler(new File(workspaceDir));

        // Route Contexts
        server.createContext("/api/login", authHandler);
        server.createContext("/api/logout", authHandler);
        server.createContext("/api/me", authHandler);

        server.createContext("/api/assignments", assignmentHandler);
        server.createContext("/api/teacher/assignments", assignmentHandler);
        server.createContext("/api/student/assignments/toggle", assignmentHandler);

        server.createContext("/api/materials", materialHandler);
        server.createContext("/api/teacher/materials", materialHandler);
        server.createContext("/api/download", materialHandler);

        server.createContext("/api/attendance", attendanceHandler);

        server.createContext("/api/profile", profileHandler);

        // Static files root (HTML, JS, CSS)
        server.createContext("/", staticFileHandler);

        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("=================================================");
        System.out.println("🎓 Smart Learning Companion - Java Backend Server");
        System.out.println("=================================================");
        System.out.println("-> HTTP Server running on: http://localhost:" + port);
        System.out.println("-> Static frontend served from: " + workspaceDir);
        System.out.println("-> Study materials stored at: " + new File(backendDir, "uploads").getAbsolutePath());
        System.out.println("-> Role Separation: ENFORCED (Student / Teacher / Parents)");
        System.out.println("=================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
