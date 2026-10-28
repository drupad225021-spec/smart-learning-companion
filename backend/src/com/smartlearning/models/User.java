package com.smartlearning.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class User {
    private String id;
    private String username;
    private String name;
    private String role; // "student", "teacher", "parents"
    private String avatar;
    private String subtitle;
    private String password;

    public User(String id, String username, String name, String role, String avatar, String subtitle, String password) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.avatar = avatar;
        this.subtitle = subtitle;
        this.password = password;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getSubtitle() { return subtitle; }
    public String getPassword() { return password; }

    public Map<String, Object> toProfileMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("username", username);
        map.put("name", name);
        map.put("role", role);
        map.put("avatar", avatar);
        map.put("subtitle", subtitle);
        return map;
    }
}
