package com.smartlearning.auth;

import com.smartlearning.models.User;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final String TEACHER_PASSWORD = "JissmolMissUyir";
    private static final String PARENT_PASSWORD = "JissmolMissUyir";

    private final Map<String, User> usersByRole = new HashMap<>();
    private final Map<String, User> activeSessions = new ConcurrentHashMap<>();

    public SessionManager() {
        // Pre-configured profiles
        usersByRole.put("student", new User(
            "SLC-9041", "drupad", "Drupad", "student", "DN", "S3 CSB • Roll No: 9041", ""
        ));
        usersByRole.put("teacher", new User(
            "TCH-4012", "teacher", "Prof. Jissmol", "teacher", "JM", "Department Head • CS & Java", TEACHER_PASSWORD
        ));
        usersByRole.put("parents", new User(
            "PAR-8021", "parent", "Drupad's Guardian", "parents", "PM", "Parent Observation Portal", PARENT_PASSWORD
        ));
    }

    public Map<String, Object> authenticate(String role, String password) {
        if (role == null) return null;
        role = role.toLowerCase().trim();
        User user = usersByRole.get(role);
        if (user == null) return null;

        // Verify credentials
        if ("teacher".equals(role)) {
            if (password == null || !TEACHER_PASSWORD.equals(password.trim())) {
                return null;
            }
        } else if ("parents".equals(role)) {
            if (password == null || !PARENT_PASSWORD.equals(password.trim())) {
                return null;
            }
        }

        // Generate session token
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", user.toProfileMap());
        return result;
    }

    public User getUserByToken(String token) {
        if (token == null) return null;
        return activeSessions.get(token);
    }

    public void invalidateToken(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    /**
     * Checks if token is valid and matches the requested role.
     * Returns User if permitted, throws SecurityException with specific message if denied.
     */
    public User authorizeRole(String token, String requiredRole) throws SecurityException {
        if (token == null || token.isEmpty()) {
            throw new SecurityException("Unauthorized: Missing authentication token");
        }
        User user = activeSessions.get(token);
        if (user == null) {
            throw new SecurityException("Unauthorized: Invalid or expired session");
        }
        if (requiredRole != null && !requiredRole.equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Forbidden: Access denied. " + user.getRole().toUpperCase() +
                    " profile cannot access " + requiredRole.toUpperCase() + " resources.");
        }
        return user;
    }
}
