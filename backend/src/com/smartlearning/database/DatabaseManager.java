package com.smartlearning.database;

import com.smartlearning.models.Assignment;
import com.smartlearning.models.AttendanceRecord;
import com.smartlearning.models.AttendanceSession;
import com.smartlearning.models.StudyMaterial;
import com.smartlearning.models.User;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQLite Database Manager.
 * Handles schema initialization, tables creation, and persistent SQL queries.
 */
public class DatabaseManager {
    private final String dbUrl;
    private final File uploadsDir;

    public DatabaseManager(String baseDir) {
        File dataDir = new File(baseDir, "data");
        this.uploadsDir = new File(baseDir, "uploads");
        if (!dataDir.exists()) dataDir.mkdirs();
        if (!uploadsDir.exists()) uploadsDir.mkdirs();

        File dbFile = new File(dataDir, "smartlearning.db");
        this.dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }

        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public File getUploadsDir() {
        return uploadsDir;
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable WAL mode & foreign keys
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA foreign_keys=ON;");

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    avatar TEXT,
                    subtitle TEXT,
                    password TEXT
                );
            """);

            // 2. Assignments Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS assignments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    due_date TEXT,
                    urgency TEXT,
                    urgency_text TEXT,
                    weight TEXT,
                    completed INTEGER DEFAULT 0,
                    description TEXT,
                    created_by TEXT,
                    created_at TEXT
                );
            """);

            // 3. Study Materials Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS study_materials (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    category TEXT,
                    description TEXT,
                    filename TEXT NOT NULL,
                    original_filename TEXT,
                    file_size TEXT,
                    uploaded_by TEXT,
                    uploaded_at TEXT,
                    download_url TEXT
                );
            """);

            // 4. Attendance Sessions Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance_sessions (
                    id TEXT PRIMARY KEY,
                    date TEXT NOT NULL,
                    class_id TEXT NOT NULL,
                    class_name TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    period TEXT NOT NULL,
                    recorded_by TEXT,
                    recorded_at TEXT
                );
            """);

            // 5. Attendance Records Table (Per Student)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    student_id TEXT NOT NULL,
                    student_name TEXT NOT NULL,
                    status TEXT NOT NULL,
                    remarks TEXT,
                    FOREIGN KEY(session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE
                );
            """);

            // Seed initial data if tables are empty
            seedUsersIfEmpty(conn);
            seedAssignmentsIfEmpty(conn);
            seedMaterialsIfEmpty(conn);
            seedAttendanceIfEmpty(conn);

            System.out.println("-> SQLite Database connected & schema verified: " + dbUrl);
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /* =========================================================================
       USERS SEEDING & QUERIES
       ========================================================================= */

    private void seedUsersIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertUser(new User("SLC-9041", "drupad", "Drupad", "student", "DN", "S3 CSB • Roll No: 9041", ""));
                insertUser(new User("TCH-4012", "teacher", "Prof. Jissmol", "teacher", "JM", "Department Head • CS & Java", "JissmolMissUyir"));
                insertUser(new User("PAR-8021", "parent", "Drupad's Guardian", "parents", "PM", "Parent Observation Portal", "JissmolMissUyir"));
            }
        }
    }

    public void insertUser(User user) {
        String sql = "INSERT OR REPLACE INTO users (id, username, name, role, avatar, subtitle, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getAvatar());
            pstmt.setString(6, user.getSubtitle());
            pstmt.setString(7, user.getPassword());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }

    public User getUserByRole(String role) {
        String sql = "SELECT id, username, name, role, avatar, subtitle, password FROM users WHERE LOWER(role) = LOWER(?) LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("avatar"),
                        rs.getString("subtitle"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by role: " + e.getMessage());
        }
        return null;
    }

    /* =========================================================================
       ASSIGNMENTS QUERIES
       ========================================================================= */

    public List<Assignment> getAllAssignments() {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT id, title, subject, due_date, urgency, urgency_text, weight, completed, description, created_by, created_at FROM assignments ORDER BY id DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Assignment(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("subject"),
                    rs.getString("due_date"),
                    rs.getString("urgency"),
                    rs.getString("urgency_text"),
                    rs.getString("weight"),
                    rs.getInt("completed") == 1,
                    rs.getString("description"),
                    rs.getString("created_by"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching assignments: " + e.getMessage());
        }
        return list;
    }

    public Assignment addAssignment(Assignment a) {
        String sql = "INSERT INTO assignments (title, subject, due_date, urgency, urgency_text, weight, completed, description, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, a.getTitle());
            pstmt.setString(2, a.getSubject());
            pstmt.setString(3, a.getDueDate());
            pstmt.setString(4, a.getUrgency());
            pstmt.setString(5, a.getUrgencyText());
            pstmt.setString(6, a.getWeight());
            pstmt.setInt(7, a.isCompleted() ? 1 : 0);
            pstmt.setString(8, a.getDescription());
            pstmt.setString(9, a.getCreatedBy());
            pstmt.setString(10, a.getCreatedAt());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    a.setId(generatedKeys.getLong(1));
                }
            }
            return a;
        } catch (SQLException e) {
            System.err.println("Error adding assignment: " + e.getMessage());
            return a;
        }
    }

    public boolean toggleAssignment(long id) {
        String checkSql = "SELECT completed FROM assignments WHERE id = ?";
        String updateSql = "UPDATE assignments SET completed = ?, urgency = ?, urgency_text = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
            checkPstmt.setLong(1, id);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next()) {
                    boolean current = rs.getInt("completed") == 1;
                    boolean newState = !current;
                    updatePstmt.setInt(1, newState ? 1 : 0);
                    updatePstmt.setString(2, newState ? "done" : "urgent");
                    updatePstmt.setString(3, newState ? "Marked Completed" : "Pending Review");
                    updatePstmt.setLong(4, id);
                    return updatePstmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error toggling assignment: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteAssignment(long id) {
        String sql = "DELETE FROM assignments WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting assignment: " + e.getMessage());
            return false;
        }
    }

    /* =========================================================================
       STUDY MATERIALS QUERIES
       ========================================================================= */

    public List<StudyMaterial> getAllMaterials() {
        List<StudyMaterial> list = new ArrayList<>();
        String sql = "SELECT id, title, subject, category, description, filename, original_filename, file_size, uploaded_by, uploaded_at, download_url FROM study_materials ORDER BY id DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new StudyMaterial(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("subject"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("filename"),
                    rs.getString("original_filename"),
                    rs.getString("file_size"),
                    rs.getString("uploaded_by"),
                    rs.getString("uploaded_at"),
                    rs.getString("download_url")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching materials: " + e.getMessage());
        }
        return list;
    }

    public StudyMaterial addMaterial(StudyMaterial m) {
        String sql = "INSERT INTO study_materials (title, subject, category, description, filename, original_filename, file_size, uploaded_by, uploaded_at, download_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, m.getTitle());
            pstmt.setString(2, m.getSubject());
            pstmt.setString(3, m.getCategory());
            pstmt.setString(4, m.getDescription());
            pstmt.setString(5, m.getFilename());
            pstmt.setString(6, m.getOriginalFilename());
            pstmt.setString(7, m.getFileSize());
            pstmt.setString(8, m.getUploadedBy());
            pstmt.setString(9, m.getUploadedAt());
            pstmt.setString(10, m.getDownloadUrl());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    m.setId(generatedKeys.getLong(1));
                }
            }
            return m;
        } catch (SQLException e) {
            System.err.println("Error adding study material: " + e.getMessage());
            return m;
        }
    }

    public boolean deleteMaterial(long id) {
        String sql = "DELETE FROM study_materials WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting material: " + e.getMessage());
            return false;
        }
    }

    /* =========================================================================
       ATTENDANCE QUERIES
       ========================================================================= */

    public List<AttendanceSession> getAllAttendanceSessions() {
        List<AttendanceSession> sessions = new ArrayList<>();
        String sql = "SELECT id, date, class_id, class_name, subject, period, recorded_by, recorded_at FROM attendance_sessions ORDER BY date DESC, id DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String sId = rs.getString("id");
                List<AttendanceRecord> records = getRecordsForSession(conn, sId);
                sessions.add(new AttendanceSession(
                    sId,
                    rs.getString("date"),
                    rs.getString("class_id"),
                    rs.getString("class_name"),
                    rs.getString("subject"),
                    rs.getString("period"),
                    rs.getString("recorded_by"),
                    rs.getString("recorded_at"),
                    records
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance sessions: " + e.getMessage());
        }
        return sessions;
    }

    private List<AttendanceRecord> getRecordsForSession(Connection conn, String sessionId) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT student_id, student_name, status, remarks FROM attendance_records WHERE session_id = ? ORDER BY id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                        rs.getString("student_id"),
                        rs.getString("student_name"),
                        rs.getString("status"),
                        rs.getString("remarks")
                    ));
                }
            }
        }
        return list;
    }

    public synchronized AttendanceSession recordAttendance(AttendanceSession session) {
        if (session.getId() == null || session.getId().trim().isEmpty()) {
            session.setId("ATT-" + System.currentTimeMillis());
        }

        String insertSessionSql = """
            INSERT INTO attendance_sessions (id, date, class_id, class_name, subject, period, recorded_by, recorded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                date=excluded.date,
                class_id=excluded.class_id,
                class_name=excluded.class_name,
                subject=excluded.subject,
                period=excluded.period,
                recorded_by=excluded.recorded_by,
                recorded_at=excluded.recorded_at;
        """;

        String deleteOldRecordsSql = "DELETE FROM attendance_records WHERE session_id = ?";
        String insertRecordSql = "INSERT INTO attendance_records (session_id, student_id, student_name, status, remarks) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Upsert session
                try (PreparedStatement pstmt = conn.prepareStatement(insertSessionSql)) {
                    pstmt.setString(1, session.getId());
                    pstmt.setString(2, session.getDate());
                    pstmt.setString(3, session.getClassId());
                    pstmt.setString(4, session.getClassName());
                    pstmt.setString(5, session.getSubject());
                    pstmt.setString(6, session.getPeriod());
                    pstmt.setString(7, session.getRecordedBy());
                    pstmt.setString(8, session.getRecordedAt());
                    pstmt.executeUpdate();
                }

                // Delete old records for this session
                try (PreparedStatement pstmt = conn.prepareStatement(deleteOldRecordsSql)) {
                    pstmt.setString(1, session.getId());
                    pstmt.executeUpdate();
                }

                // Insert all student records
                try (PreparedStatement pstmt = conn.prepareStatement(insertRecordSql)) {
                    for (AttendanceRecord r : session.getRecords()) {
                        pstmt.setString(1, session.getId());
                        pstmt.setString(2, r.getStudentId());
                        pstmt.setString(3, r.getStudentName());
                        pstmt.setString(4, r.getStatus());
                        pstmt.setString(5, r.getRemarks());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error recording attendance in DB: " + e.getMessage());
        }
        return session;
    }

    public boolean deleteAttendanceSession(String id) {
        String sql = "DELETE FROM attendance_sessions WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance session: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getStudentAttendanceSummary(String studentId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        int totalSessions = 0;
        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;
        int excusedCount = 0;
        List<Map<String, Object>> sessionHistory = new ArrayList<>();

        String sql = """
            SELECT s.id as session_id, s.date, s.class_name, s.subject, s.period, s.recorded_by,
                   r.status, r.remarks
            FROM attendance_records r
            JOIN attendance_sessions s ON r.session_id = s.id
            WHERE LOWER(r.student_id) = LOWER(?) OR LOWER(r.student_name) = LOWER(?)
               OR (LOWER(?) = 'slc-9041' AND LOWER(r.student_name) = 'drupad')
            ORDER BY s.date DESC, s.id DESC
        """;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, studentId);
            pstmt.setString(3, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    totalSessions++;
                    String st = rs.getString("status") != null ? rs.getString("status").toLowerCase() : "present";
                    if ("present".equals(st)) presentCount++;
                    else if ("absent".equals(st)) absentCount++;
                    else if ("late".equals(st)) lateCount++;
                    else if ("excused".equals(st)) excusedCount++;

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("sessionId", rs.getString("session_id"));
                    entry.put("date", rs.getString("date"));
                    entry.put("className", rs.getString("class_name"));
                    entry.put("subject", rs.getString("subject"));
                    entry.put("period", rs.getString("period"));
                    entry.put("status", st);
                    entry.put("remarks", rs.getString("remarks"));
                    entry.put("recordedBy", rs.getString("recorded_by"));
                    sessionHistory.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting student attendance summary: " + e.getMessage());
        }

        double rate = totalSessions == 0 ? 100.0 : ((double) (presentCount + lateCount) / totalSessions) * 100.0;
        summary.put("studentId", studentId);
        summary.put("totalSessions", totalSessions);
        summary.put("presentCount", presentCount);
        summary.put("absentCount", absentCount);
        summary.put("lateCount", lateCount);
        summary.put("excusedCount", excusedCount);
        summary.put("attendancePercentage", Math.round(rate * 10.0) / 10.0);
        summary.put("history", sessionHistory);
        return summary;
    }

    /* =========================================================================
       INITIAL SEEDS
       ========================================================================= */

    private void seedAssignmentsIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM assignments")) {
            if (rs.next() && rs.getInt(1) == 0) {
                addAssignment(new Assignment(101, "Implement Dijkstra's Algorithm in Python", "Computer Science • Prof. Jissmol", "Tomorrow at 11:59 PM", "urgent", "Due Tomorrow", "20% of Grade", false, "Graph relaxation using min-heaps in adjacency lists.", "Prof. Jissmol", "2026-10-12"));
                addAssignment(new Assignment(102, "Wave Mechanics Problem Set 4 (Questions 1 - 12)", "Modern Physics • Dr. David Ross", "Oct 5, 2026", "soon", "Due in 3 Days", "15% of Grade", false, "1D infinite potential wells and transmission probabilities.", "Dr. David Ross", "2026-10-10"));
                addAssignment(new Assignment(103, "Lagrangian Multiplier Proofs & Matrix Formulations", "Mathematics • Prof. Jissmol", "Oct 8, 2026", "soon", "Due in 6 Days", "10% of Grade", false, "Constrained optimization and KKT stationarity conditions.", "Prof. Jissmol", "2026-10-09"));
                addAssignment(new Assignment(104, "Binary Search Tree Traversal Lab Evaluation", "Computer Science • Prof. Jissmol", "Submitted on Sep 29", "done", "Grade: 98/100", "Graded", true, "Recursive and iterative post-order traversal implementation.", "Prof. Jissmol", "2026-09-29"));
                addAssignment(new Assignment(105, "Electromagnetism Laboratory Report: Induction Fields", "Modern Physics • Dr. David Ross", "Submitted on Sep 24", "done", "Grade: 92/100", "Graded", true, "Faraday induction coil measurements and Lenz law analysis.", "Dr. David Ross", "2026-09-24"));
            }
        }
    }

    private void seedMaterialsIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM study_materials")) {
            if (rs.next() && rs.getInt(1) == 0) {
                addMaterial(new StudyMaterial(1, "Graph Theory & Shortest Path Comprehensive Guide", "Computer Science", "CS", "Complete algorithmic breakdown of Dijkstra, Bellman-Ford, and Priority Queue relaxation.", "Dijkstra_Graph_Algorithms_Notes.pdf", "Dijkstra_Graph_Algorithms_Notes.pdf", "2.4 MB", "Prof. Jissmol", "2026-10-11", "/api/download/Dijkstra_Graph_Algorithms_Notes.pdf"));
                addMaterial(new StudyMaterial(2, "Neural Attention Mechanisms & Transformer Blueprint", "Computer Science", "CS", "In-depth mathematical formulation of Scaled Dot-Product Attention and Transformer architectures.", "Neural_Attention_Transformers_Guide.pdf", "Neural_Attention_Transformers_Guide.pdf", "3.8 MB", "Prof. Jissmol", "2026-10-10", "/api/download/Neural_Attention_Transformers_Guide.pdf"));
                addMaterial(new StudyMaterial(3, "Convex Optimization, KKT Duality & Quadratic Forms", "Applied Mathematics", "STEM", "Proof sheets and problem walkthroughs for constrained optimization and Lagrange duality.", "Convex_Optimization_KKT_Handout.pdf", "Convex_Optimization_KKT_Handout.pdf", "1.9 MB", "Prof. Jissmol", "2026-10-08", "/api/download/Convex_Optimization_KKT_Handout.pdf"));
                addMaterial(new StudyMaterial(4, "Quantum Wavepackets & Tunneling Mechanics Handout", "Modern Physics", "STEM", "Full derivation of wave packet dispersion, boundary value problems, and alpha decay tunneling.", "Quantum_Wavepackets_Physics_Notes.pdf", "Quantum_Wavepackets_Physics_Notes.pdf", "4.1 MB", "Dr. David Ross", "2026-10-06", "/api/download/Quantum_Wavepackets_Physics_Notes.pdf"));
            }
        }
    }

    private void seedAttendanceIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM attendance_sessions")) {
            if (rs.next() && rs.getInt(1) == 0) {
                List<AttendanceRecord> s1 = new ArrayList<>();
                s1.add(new AttendanceRecord("SLC-9041", "Drupad", "present", "Active participation in graph traversal discussion"));
                s1.add(new AttendanceRecord("SLC-9042", "Enza", "present", ""));
                s1.add(new AttendanceRecord("SLC-9043", "Elizabeth", "present", ""));
                s1.add(new AttendanceRecord("SLC-9044", "Anlin", "present", ""));
                s1.add(new AttendanceRecord("SLC-9045", "Milan", "late", "Arrived 10 mins late"));
                s1.add(new AttendanceRecord("SLC-9046", "Sandra", "present", ""));
                s1.add(new AttendanceRecord("SLC-9047", "Kevin", "present", ""));
                s1.add(new AttendanceRecord("SLC-9048", "Meera", "present", ""));
                s1.add(new AttendanceRecord("SLC-9049", "Roshan", "absent", "Medical leave notice pending"));

                recordAttendance(new AttendanceSession(
                    "ATT-20261014-P1", "2026-10-14", "s3_csb", "S3 CSB",
                    "Computer Science & Java", "Period 1 (09:00 - 10:00 AM)",
                    "Prof. Jissmol", "2026-10-14T09:05:00", s1
                ));

                List<AttendanceRecord> s2 = new ArrayList<>();
                s2.add(new AttendanceRecord("SLC-9041", "Drupad", "present", "Excellent whiteboard proof contribution"));
                s2.add(new AttendanceRecord("SLC-9042", "Enza", "present", ""));
                s2.add(new AttendanceRecord("SLC-9043", "Elizabeth", "present", ""));
                s2.add(new AttendanceRecord("SLC-9044", "Anlin", "present", ""));
                s2.add(new AttendanceRecord("SLC-9045", "Milan", "present", ""));
                s2.add(new AttendanceRecord("SLC-9046", "Sandra", "present", ""));
                s2.add(new AttendanceRecord("SLC-9047", "Kevin", "absent", "Unexcused absence"));
                s2.add(new AttendanceRecord("SLC-9048", "Meera", "present", ""));
                s2.add(new AttendanceRecord("SLC-9049", "Roshan", "present", ""));

                recordAttendance(new AttendanceSession(
                    "ATT-20261013-P2", "2026-10-13", "s3_csb", "S3 CSB",
                    "Java Object Oriented Design", "Period 2 (10:15 - 11:15 AM)",
                    "Prof. Jissmol", "2026-10-13T10:20:00", s2
                ));
            }
        }
    }
}
