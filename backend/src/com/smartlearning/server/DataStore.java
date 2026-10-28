package com.smartlearning.server;

import com.smartlearning.database.DatabaseManager;
import com.smartlearning.models.Assignment;
import com.smartlearning.models.AttendanceSession;
import com.smartlearning.models.StudyMaterial;

import java.io.File;
import java.util.*;

/**
 * DataStore backed by persistent SQLite Database via DatabaseManager.
 * Preserves clean service abstraction for handlers while delegating all operations to SQL.
 */
public class DataStore {
    private final DatabaseManager db;

    public DataStore(String baseDir) {
        this.db = new DatabaseManager(baseDir);
    }

    public File getUploadsDir() {
        return db.getUploadsDir();
    }

    /* =========================================================================
       ASSIGNMENTS
       ========================================================================= */

    public List<Assignment> getAssignments() {
        return db.getAllAssignments();
    }

    public Assignment addAssignment(Assignment a) {
        return db.addAssignment(a);
    }

    public boolean toggleAssignment(long id) {
        return db.toggleAssignment(id);
    }

    public boolean deleteAssignment(long id) {
        return db.deleteAssignment(id);
    }

    /* =========================================================================
       STUDY MATERIALS
       ========================================================================= */

    public List<StudyMaterial> getMaterials() {
        return db.getAllMaterials();
    }

    public StudyMaterial addMaterial(StudyMaterial m) {
        return db.addMaterial(m);
    }

    public boolean deleteMaterial(long id) {
        return db.deleteMaterial(id);
    }

    /* =========================================================================
       ATTENDANCE MANAGEMENT
       ========================================================================= */

    public List<AttendanceSession> getAttendanceSessions() {
        return db.getAllAttendanceSessions();
    }

    public AttendanceSession recordAttendance(AttendanceSession session) {
        return db.recordAttendance(session);
    }

    public boolean deleteAttendanceSession(String id) {
        return db.deleteAttendanceSession(id);
    }

    public Map<String, Object> getStudentAttendanceSummary(String studentId) {
        return db.getStudentAttendanceSummary(studentId);
    }
}
