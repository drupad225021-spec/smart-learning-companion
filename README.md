# Smart Learning Companion 🎓

A professional, minimal, and modern frontend application built for the **Smart Learning Companion** platform. Designed with clean typography, responsive layouts, subtle micro-animations, and seamless stakeholder views for **Students**, **Teachers**, and **Parents**.

---

## 🚀 Key Features

### 1. Role-Based Login Portal
- **Role Selector Cards:** Instant switching between **Student**, **Teacher**, and **Parents** portals.
- **Teacher & Parents Credential Guard:**
  - Password is required for Teacher and Parents roles: `JissmolMissUyir`.
  - **Always-Visible Password Setting:** Designed with the show-password option enabled by default (`type="text"` and active visibility indicator), alongside a dedicated `"Show password text as you type"` control.
  - One-click auto-fill convenience button.
  - Instant error detection with clean feedback.
- **Student One-Click Entry:** Seamless passwordless access for student profile (Drupad).

---

### 2. Student Profile Workspace
- **Topics to Learn:**
  - Dynamic subject cards (Computer Science, Artificial Intelligence, Applied Mathematics, Modern Physics).
  - Pacing indicators, progress bar, difficulty badges, and estimated hours.
  - Interactive **"Continue Learning"** modal with submodule check-offs that dynamically update progress.
  - Filter by category (*All*, *Mathematics & Science*, *Computer Science*).
- **Your Pending Assignments:**
  - Interactive deadline tracker with color-coded urgency badges (*Due Tomorrow*, *Due in 3 Days*, *Graded*).
  - Clickable status check to complete/reopen assignments.
  - Instant counters for pending and completed tasks.
- **Voice Recorded Sessions:**
  - Embedded smart audio player with dynamic animated waveform visualizer.
  - Real-time time counter, interactive scrub bar, and fast-forward/rewind controls.
  - Adjustable playback speed options (`1.0x`, `1.25x`, `1.5x`, `2.0x`).
  - Synced whiteboard notes and archive list of past lectures by Prof. Jissmol and Dr. David Ross.

---

### 3. Teacher Profile (Prof. Jissmol's Instructional Console)
- **Classroom Selector:** Switch between cohorts (e.g., *Class 10-A*, *Class 10-B*, *Advanced Algorithms*).
- **Executive Metric Cards:** Enrolled Students, Average Mastery Score (84.6%), Class Modules Covered (9 of 12), and Pending Reviews.
- **Student Learning Graph:**
  - Interactive Chart.js graph tracking class mastery curve over time against top cohort benchmarks (Drupad, Enza, Elizabeth).
  - Filterable by *Mastery Index*, *Study Hours*, and *Retention %*.
- **Number of Modules Covered by Students:**
  - Granular progress bars visualizing syllabus completion per module (e.g. *Foundations: 32/32*, *Graph Theory: 29/32*, *Neural Attention: 24/32*).
- **Class Roster & Action Hub:**
  - Searchable student table featuring **Drupad**, **Enza**, **Elizabeth**, **Anlin**, **Milan**, **Sandra**, **Kevin**, **Meera**, and **Roshan** with individual module counts, mastery scores, and engagement status.
  - Instant teacher feedback transmission action.

---

### 4. Parents Profile (Drupad's Academic Progress & Marks)
- **Cumulative GPA & Attendance:**
  - Real-time scorecards: 3.92/4.0 Cumulative GPA, 96.8% Class Attendance, 19/20 on-time submissions.
- **Subject-Wise Marks & Academic Progress:**
  - Detailed gradebook table displaying Mid-Term Exam, Unit Tests, Assignment Scores, Total Percentage, and Letter Grades (`A+`, `A`).
- **Marks Progression Benchmark Chart:**
  - Comparative bar chart charting Drupad's subject scores vs. the class average.
- **Teacher Commendations & Behavioral Notes:**
  - Direct observations timeline from Prof. Jissmol and Dr. David Ross.
- **Interactive Parent Actions:**
  - "Download Report Card" trigger for printable transcript.
  - "Message Teacher" modal to directly send inquiries to Prof. Jissmol.

---

## 🎨 Design Philosophy
- **Aesthetic:** Minimalist, professional Scandinavian / Apple design language with generous spacing and crisp borders (`#E2E8F0`).
- **Typography:** Google Fonts (`Plus Jakarta Sans` & `Space Grotesk`).
- **Dark Mode Support:** Clean light/dark toggle in the header navigation.
- **Zero Build Dependencies:** Works out of the box in any browser by opening `index.html`.

---

## 📁 Project Directory
```
C:\Users\dkmen\.gemini\antigravity-ide\scratch\smart-learning-companion\
├── index.html        # Clean semantic markup for all 3 portals & modals
├── styles.css        # Vanilla CSS design system with theme tokens
├── app.js            # State management, auth, audio player, & Chart.js logic
└── README.md         # Documentation
```
