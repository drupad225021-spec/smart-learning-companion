package com.smartlearning;

import com.smartlearning.server.HttpServerApp;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ignored) {}
        }

        // Determine workspace directory (parent of 'backend' if running from workspace or backend)
        String currentDir = System.getProperty("user.dir");
        File base = new File(currentDir);
        File checkIndex = new File(base, "index.html");
        String workspaceDir = base.getAbsolutePath();

        if (!checkIndex.exists()) {
            File parent = base.getParentFile();
            if (parent != null && new File(parent, "index.html").exists()) {
                workspaceDir = parent.getAbsolutePath();
            }
        }

        try {
            HttpServerApp app = new HttpServerApp(port, workspaceDir);
            app.start();

            // Keep main thread alive
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Fatal error starting Smart Learning Companion backend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
