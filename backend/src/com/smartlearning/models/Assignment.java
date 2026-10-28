package com.smartlearning.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Assignment {
    private long id;
    private String title;
    private String subject;
    private String dueDate;
    private String urgency;     // "urgent", "soon", "done"
    private String urgencyText; // e.g. "Due Tomorrow", "Due in 3 Days"
    private String weight;      // e.g. "20% of Grade"
    private boolean completed;
    private String description;
    private String createdBy;
    private String createdAt;

    public Assignment() {}

    public Assignment(long id, String title, String subject, String dueDate, String urgency, String urgencyText, String weight, boolean completed, String description, String createdBy, String createdAt) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.urgency = urgency;
        this.urgencyText = urgencyText;
        this.weight = weight;
        this.completed = completed;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getUrgencyText() { return urgencyText; }
    public void setUrgencyText(String urgencyText) { this.urgencyText = urgencyText; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("subject", subject);
        map.put("dueDate", dueDate);
        map.put("urgency", urgency);
        map.put("urgencyText", urgencyText);
        map.put("weight", weight);
        map.put("completed", completed);
        map.put("description", description != null ? description : "");
        map.put("createdBy", createdBy != null ? createdBy : "");
        map.put("createdAt", createdAt != null ? createdAt : "");
        return map;
    }

    public static Assignment fromMap(Map<String, Object> map) {
        long id = 0;
        if (map.get("id") instanceof Number) {
            id = ((Number) map.get("id")).longValue();
        }
        String title = (String) map.get("title");
        String subject = (String) map.get("subject");
        String dueDate = (String) map.get("dueDate");
        String urgency = (String) map.get("urgency");
        String urgencyText = (String) map.get("urgencyText");
        String weight = (String) map.get("weight");
        boolean completed = Boolean.TRUE.equals(map.get("completed"));
        String description = (String) map.get("description");
        String createdBy = (String) map.get("createdBy");
        String createdAt = (String) map.get("createdAt");
        return new Assignment(id, title, subject, dueDate, urgency, urgencyText, weight, completed, description, createdBy, createdAt);
    }
}
