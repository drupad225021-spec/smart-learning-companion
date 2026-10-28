package com.smartlearning.models;

import java.util.*;

public class AttendanceSession {
    private String id;
    private String date;         // e.g. "2026-10-15"
    private String classId;      // e.g. "grade10a"
    private String className;    // e.g. "Class 10-A"
    private String subject;      // e.g. "Computer Science"
    private String period;       // e.g. "Period 1 (09:00 - 10:00 AM)"
    private String recordedBy;   // e.g. "Prof. Jissmol"
    private String recordedAt;   // ISO or timestamp string
    private List<AttendanceRecord> records = new ArrayList<>();

    public AttendanceSession() {}

    public AttendanceSession(String id, String date, String classId, String className,
                             String subject, String period, String recordedBy, String recordedAt,
                             List<AttendanceRecord> records) {
        this.id = id;
        this.date = date;
        this.classId = classId;
        this.className = className;
        this.subject = subject;
        this.period = period;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt;
        if (records != null) {
            this.records = records;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }

    public List<AttendanceRecord> getRecords() { return records; }
    public void setRecords(List<AttendanceRecord> records) { this.records = records; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("date", date);
        map.put("classId", classId);
        map.put("className", className);
        map.put("subject", subject);
        map.put("period", period);
        map.put("recordedBy", recordedBy);
        map.put("recordedAt", recordedAt);

        List<Map<String, Object>> recordsMap = new ArrayList<>();
        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;
        int excusedCount = 0;

        for (AttendanceRecord r : records) {
            recordsMap.add(r.toMap());
            String s = r.getStatus() != null ? r.getStatus().toLowerCase() : "present";
            if ("present".equals(s)) presentCount++;
            else if ("absent".equals(s)) absentCount++;
            else if ("late".equals(s)) lateCount++;
            else if ("excused".equals(s)) excusedCount++;
        }
        map.put("records", recordsMap);
        map.put("totalStudents", records.size());
        map.put("presentCount", presentCount);
        map.put("absentCount", absentCount);
        map.put("lateCount", lateCount);
        map.put("excusedCount", excusedCount);
        double rate = records.isEmpty() ? 100.0 : ((double) (presentCount + lateCount) / records.size()) * 100.0;
        map.put("attendanceRate", Math.round(rate * 10.0) / 10.0);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static AttendanceSession fromMap(Map<String, Object> map) {
        String id = (String) map.get("id");
        String date = (String) map.get("date");
        String classId = (String) map.get("classId");
        String className = (String) map.get("className");
        String subject = (String) map.get("subject");
        String period = (String) map.get("period");
        String recordedBy = (String) map.get("recordedBy");
        String recordedAt = (String) map.get("recordedAt");

        List<AttendanceRecord> records = new ArrayList<>();
        Object recs = map.get("records");
        if (recs instanceof List) {
            for (Object item : (List<Object>) recs) {
                if (item instanceof Map) {
                    records.add(AttendanceRecord.fromMap((Map<String, Object>) item));
                }
            }
        }
        return new AttendanceSession(id, date, classId, className, subject, period, recordedBy, recordedAt, records);
    }
}
