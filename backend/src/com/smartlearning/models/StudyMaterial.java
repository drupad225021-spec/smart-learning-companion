package com.smartlearning.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class StudyMaterial {
    private long id;
    private String title;
    private String subject;
    private String category;    // "CS", "STEM"
    private String description;
    private String filename;
    private String originalFilename;
    private String fileSize;
    private String uploadedBy;
    private String uploadedAt;
    private String downloadUrl;

    public StudyMaterial() {}

    public StudyMaterial(long id, String title, String subject, String category, String description,
                         String filename, String originalFilename, String fileSize,
                         String uploadedBy, String uploadedAt, String downloadUrl) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.category = category;
        this.description = description;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.downloadUrl = downloadUrl;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("subject", subject);
        map.put("category", category != null ? category : "General");
        map.put("description", description != null ? description : "");
        map.put("filename", filename);
        map.put("originalFilename", originalFilename);
        map.put("fileSize", fileSize != null ? fileSize : "Unknown");
        map.put("uploadedBy", uploadedBy != null ? uploadedBy : "Teacher");
        map.put("uploadedAt", uploadedAt != null ? uploadedAt : "");
        map.put("downloadUrl", downloadUrl != null ? downloadUrl : "");
        return map;
    }

    public static StudyMaterial fromMap(Map<String, Object> map) {
        long id = 0;
        if (map.get("id") instanceof Number) {
            id = ((Number) map.get("id")).longValue();
        }
        String title = (String) map.get("title");
        String subject = (String) map.get("subject");
        String category = (String) map.get("category");
        String description = (String) map.get("description");
        String filename = (String) map.get("filename");
        String originalFilename = (String) map.get("originalFilename");
        String fileSize = (String) map.get("fileSize");
        String uploadedBy = (String) map.get("uploadedBy");
        String uploadedAt = (String) map.get("uploadedAt");
        String downloadUrl = (String) map.get("downloadUrl");
        return new StudyMaterial(id, title, subject, category, description, filename, originalFilename, fileSize, uploadedBy, uploadedAt, downloadUrl);
    }
}
