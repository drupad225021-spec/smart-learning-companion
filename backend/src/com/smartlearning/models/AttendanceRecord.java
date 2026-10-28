package com.smartlearning.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttendanceRecord {
    private String studentId;
    private String studentName;
    private String status; // "present", "absent", "late", "excused"
    private String remarks;

    public AttendanceRecord() {}

    public AttendanceRecord(String studentId, String studentName, String status, String remarks) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.status = status;
        this.remarks = remarks != null ? remarks : "";
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("studentId", studentId);
        map.put("studentName", studentName);
        map.put("status", status);
        map.put("remarks", remarks);
        return map;
    }

    public static AttendanceRecord fromMap(Map<String, Object> map) {
        String sId = (String) map.get("studentId");
        String sName = (String) map.get("studentName");
        String status = (String) map.get("status");
        String remarks = (String) map.get("remarks");
        return new AttendanceRecord(sId, sName, status, remarks);
    }
}
