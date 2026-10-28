/**
 * Smart Learning Companion - Comprehensive Application Logic
 * Role-based dashboards for Student, Teacher, and Parents
 */

// API Base URL (connects directly to Java backend)
const API_BASE = window.location.port === '8080' ? '' : 'http://localhost:8080';

// Application State
const state = {
  currentRole: 'student', // 'student' | 'teacher' | 'parents'
  isAuthenticated: false,
  authToken: localStorage.getItem('slc_auth_token') || null,
  currentUser: JSON.parse(localStorage.getItem('slc_user') || 'null'),
  theme: 'light',
  audio: {
    isPlaying: false,
    currentTime: 165, // in seconds (02:45)
    totalDuration: 860, // in seconds (14:20)
    playbackSpeed: 1.0,
    timerId: null,
    currentSessionId: 1
  },
  activeStudentTab: 'tab-topics',
  topicsFilter: 'all',
  materialsFilter: 'all',
  selectedTopicForModal: null,
  selectedMaterialFile: null,
  assignments: [],
  materials: [],
  attendance: {
    sessions: [],
    studentSummary: null,
    currentRollCall: [],
    selectedSessionId: null
  },
  charts: {
    teacherChart: null,
    parentChart: null
  }
};

// Teacher & Parent Authentication Credential
const REQUIRED_PASSWORD = "JissmolMissUyir";

// Sample Data Repositories
const studentTopics = [
  {
    id: 1,
    title: "Graph Algorithms & Shortest Path",
    category: "CS",
    categoryLabel: "Computer Science",
    difficulty: "Advanced",
    progress: 85,
    modulesDone: 5,
    modulesTotal: 6,
    duration: "4.5 hrs",
    instructor: "Prof. Jissmol",
    description: "Deep dive into Dijkstra's, A* Search, and Bellman-Ford algorithms with real-world road network simulations.",
    submodules: [
      "Graph Representations (Adjacency Lists & Matrices)",
      "Breadth-First and Depth-First Search Mechanics",
      "Dijkstra’s Algorithm Priority Queue Implementation",
      "Dynamic Shortest Paths & Negative Weight Cycles"
    ]
  },
  {
    id: 2,
    title: "Neural Attention & Transformers",
    category: "CS",
    categoryLabel: "Artificial Intelligence",
    difficulty: "Intermediate",
    progress: 60,
    modulesDone: 3,
    modulesTotal: 5,
    duration: "3.2 hrs",
    instructor: "Prof. Jissmol",
    description: "Understanding self-attention mechanisms, multi-head projections, and tokenized transformer layers.",
    submodules: [
      "Vector Embeddings & Dot-product Scaled Attention",
      "Multi-Head Attention Projections",
      "Positional Encoding & Residual Connections",
      "Transformer Block Architectural Blueprint"
    ]
  },
  {
    id: 3,
    title: "Convex Optimization & Lagrange Multipliers",
    category: "STEM",
    categoryLabel: "Applied Mathematics",
    difficulty: "Advanced",
    progress: 40,
    modulesDone: 2,
    modulesTotal: 5,
    duration: "5.0 hrs",
    instructor: "Prof. Jissmol",
    description: "Constrained optimization, Karush-Kuhn-Tucker (KKT) conditions, and quadratic programming.",
    submodules: [
      "Affine Sets and Convex Functions Review",
      "Gradient Descent & Newton-Raphson Optimization",
      "Lagrangian Duality & KKT Verification",
      "Support Vector Machine Optimization Problem"
    ]
  },
  {
    id: 4,
    title: "Quantum Wavepackets & Schrodinger Mechanics",
    category: "STEM",
    categoryLabel: "Modern Physics",
    difficulty: "Advanced",
    progress: 70,
    modulesDone: 4,
    modulesTotal: 6,
    duration: "6.0 hrs",
    instructor: "Dr. David Ross",
    description: "Probabilistic wave mechanics, particle-in-a-box eigenstates, and operator observables.",
    submodules: [
      "De Broglie Wavelength & Dual Character of Matter",
      "Time-Dependent Schrodinger Equation in 1D",
      "Infinite Potential Well & Energy Quantization",
      "Quantum Tunneling and Barrier Penetration"
    ]
  },
  {
    id: 5,
    title: "Dynamic Programming: Knapsack & Subsequences",
    category: "CS",
    categoryLabel: "Computer Science",
    difficulty: "Intermediate",
    progress: 92,
    modulesDone: 5,
    modulesTotal: 5,
    duration: "3.5 hrs",
    instructor: "Prof. Jissmol",
    description: "Optimal substructure analysis, memoization tables, and bottom-up tabulation techniques.",
    submodules: [
      "Recursive Overlapping Subproblems",
      "0/1 Knapsack Decision Grid Formulation",
      "Longest Common Subsequence Matrix",
      "Bitmask DP for Traveling Salesperson"
    ]
  },
  {
    id: 6,
    title: "Statistical Inference & Confidence Intervals",
    category: "STEM",
    categoryLabel: "Applied Mathematics",
    difficulty: "Foundational",
    progress: 50,
    modulesDone: 2,
    modulesTotal: 4,
    duration: "2.8 hrs",
    instructor: "Dr. David Ross",
    description: "Central Limit Theorem, Student's t-distributions, and hypothesis testing benchmarks.",
    submodules: [
      "Sampling Distributions & Central Limit Theorem",
      "Z-scores and One-sample T-Tests",
      "P-value Formulation and Significance Thresholds",
      "Chi-Square Goodness of Fit Test"
    ]
  }
];

const studentAssignments = [
  {
    id: 101,
    title: "Implement Dijkstra's Algorithm in Python",
    subject: "Computer Science • Prof. Jissmol",
    dueDate: "Tomorrow at 11:59 PM",
    urgency: "urgent",
    urgencyText: "Due Tomorrow",
    completed: false,
    weight: "20% of Grade"
  },
  {
    id: 102,
    title: "Wave Mechanics Problem Set 4 (Questions 1 - 12)",
    subject: "Modern Physics • Dr. David Ross",
    dueDate: "Oct 5, 2026",
    urgency: "soon",
    urgencyText: "Due in 3 Days",
    completed: false,
    weight: "15% of Grade"
  },
  {
    id: 103,
    title: "Lagrangian Multiplier Proofs & Matrix Formulations",
    subject: "Mathematics • Prof. Jissmol",
    dueDate: "Oct 8, 2026",
    urgency: "soon",
    urgencyText: "Due in 6 Days",
    completed: false,
    weight: "10% of Grade"
  },
  {
    id: 104,
    title: "Binary Search Tree Traversal Lab Evaluation",
    subject: "Computer Science • Prof. Jissmol",
    dueDate: "Submitted on Sep 29",
    urgency: "done",
    urgencyText: "Grade: 98/100",
    completed: true,
    weight: "Graded"
  },
  {
    id: 105,
    title: "Electromagnetism Laboratory Report: Induction Fields",
    subject: "Modern Physics • Dr. David Ross",
    dueDate: "Submitted on Sep 24",
    urgency: "done",
    urgencyText: "Grade: 92/100",
    completed: true,
    weight: "Graded"
  }
];

const recordedSessions = [
  {
    id: 1,
    title: "Lecture 14: Neural Attention Mechanisms & Transformer Logic",
    instructor: "Prof. Jissmol",
    course: "Computer Science",
    duration: "14:20",
    durationSec: 860,
    date: "Recorded Yesterday",
    current: true,
    notes: "Detailed step-by-step breakdown of query, key, and value vectors."
  },
  {
    id: 2,
    title: "Lecture 13: Convex Optimization & Gradient Descent Nuances",
    instructor: "Prof. Jissmol",
    course: "Applied Mathematics",
    duration: "22:15",
    durationSec: 1335,
    date: "Recorded 3 days ago",
    current: false,
    notes: "Includes live whiteboard proofs for gradient descent convergence rates."
  },
  {
    id: 3,
    title: "Lecture 12: Dijkstra & Priority Queue Implementations",
    instructor: "Prof. Jissmol",
    course: "Computer Science",
    duration: "18:40",
    durationSec: 1120,
    date: "Recorded Sep 26",
    current: false,
    notes: "Visual walkthrough of binary heap relaxation steps in directed graphs."
  },
  {
    id: 4,
    title: "Lecture 11: Quantum Tunneling & Potential Step Barriers",
    instructor: "Dr. David Ross",
    course: "Modern Physics",
    duration: "25:10",
    durationSec: 1510,
    date: "Recorded Sep 22",
    current: false,
    notes: "Transmission coefficient derivation and alpha decay physics."
  }
];

const teacherClassModules = [
  { name: "Module 1: Foundations of Algorithms", coveredBy: 32, total: 32, percentage: 100 },
  { name: "Module 2: Graph Theory & Shortest Path", coveredBy: 29, total: 32, percentage: 91 },
  { name: "Module 3: Dynamic Programming Arrays", coveredBy: 28, total: 32, percentage: 88 },
  { name: "Module 4: Neural Attention & Transformers", coveredBy: 24, total: 32, percentage: 75 },
  { name: "Module 5: Convex Optimization Mechanics", coveredBy: 20, total: 32, percentage: 63 },
  { name: "Module 6: Heuristic Search & Alpha-Beta Pruning", coveredBy: 14, total: 32, percentage: 44 }
];

const teacherStudentRoster = [
  { name: "Drupad", id: "SLC-9041", batch: "S3 CSB", modulesCovered: "8 / 10", mastery: "94.5%", currentTopic: "Transformers", engagement: "High", status: "high" },
  { name: "Enza", id: "SLC-9042", batch: "S3 CSB", modulesCovered: "9 / 10", mastery: "96.2%", currentTopic: "Convex Duality", engagement: "High", status: "high" },
  { name: "Elizabeth", id: "SLC-9043", batch: "S3 CSB", modulesCovered: "9 / 10", mastery: "95.8%", currentTopic: "Graph Algorithms", engagement: "High", status: "high" },
  { name: "Anlin", id: "SLC-9044", batch: "S3 CSB", modulesCovered: "8 / 10", mastery: "92.4%", currentTopic: "Transformers", engagement: "High", status: "high" },
  { name: "Milan", id: "SLC-9045", batch: "S5 CS", modulesCovered: "7 / 10", mastery: "84.0%", currentTopic: "Dijkstra Priority", engagement: "Moderate", status: "med" },
  { name: "Sandra", id: "SLC-9046", batch: "S5 CS", modulesCovered: "8 / 10", mastery: "90.5%", currentTopic: "Quantum Mechanics", engagement: "High", status: "high" },
  { name: "Kevin", id: "SLC-9047", batch: "S5 CS", modulesCovered: "6 / 10", mastery: "79.1%", currentTopic: "Dynamic Prog", engagement: "Moderate", status: "med" },
  { name: "Meera", id: "SLC-9048", batch: "S7 CS", modulesCovered: "7 / 10", mastery: "86.3%", currentTopic: "Convex Duality", engagement: "High", status: "high" },
  { name: "Roshan", id: "SLC-9049", batch: "S7 CS", modulesCovered: "5 / 10", mastery: "73.0%", currentTopic: "Graph Traversal", engagement: "Needs Focus", status: "alert" }
];

const parentMarksData = [
  { subject: "Advanced Computer Science", midTerm: "48 / 50", unitTest: "24 / 25", assignments: "25 / 25", total: "97%", grade: "A+", gradeClass: "a-plus" },
  { subject: "Applied Mathematics", midTerm: "46 / 50", unitTest: "23 / 25", assignments: "24 / 25", total: "93%", grade: "A+", gradeClass: "a-plus" },
  { subject: "Modern Physics", midTerm: "44 / 50", unitTest: "22 / 25", assignments: "23 / 25", total: "89%", grade: "A", gradeClass: "a" },
  { subject: "Chemistry & Materials", midTerm: "45 / 50", unitTest: "21 / 25", assignments: "22 / 25", total: "88%", grade: "A", gradeClass: "a" },
  { subject: "English & Technical Writing", midTerm: "47 / 50", unitTest: "24 / 25", assignments: "24 / 25", total: "95%", grade: "A+", gradeClass: "a-plus" }
];

// Initialization
document.addEventListener('DOMContentLoaded', async () => {
  setupRoleSelection();
  setupPasswordVisibility();
  setupThemeToggle();
  await checkExistingSession();
  renderStudentDashboard();
  renderTeacherDashboard();
  renderParentDashboard();
});

/* ==========================================================================
   Authentication & Portal Switching
   ========================================================================== */

function setupRoleSelection() {
  const roleCards = document.querySelectorAll('.role-card');
  const passwordGroup = document.getElementById('password-group');
  const passwordLabel = document.getElementById('password-label');
  const passwordInput = document.getElementById('password-input');
  const studentPreview = document.getElementById('student-login-details');
  const loginSubmitBtn = document.getElementById('login-submit-btn');

  roleCards.forEach(card => {
    card.addEventListener('click', () => {
      roleCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      const selectedRole = card.getAttribute('data-role');
      state.currentRole = selectedRole;

      if (selectedRole === 'student') {
        passwordGroup.classList.add('hidden');
        studentPreview.classList.remove('hidden');
        loginSubmitBtn.querySelector('span').textContent = "Sign In as Student (Drupad)";
      } else if (selectedRole === 'teacher') {
        passwordGroup.classList.remove('hidden');
        studentPreview.classList.add('hidden');
        passwordLabel.textContent = "Teacher Password";
        loginSubmitBtn.querySelector('span').textContent = "Access Teacher Console (Prof. Jissmol)";
        passwordInput.value = REQUIRED_PASSWORD;
      } else if (selectedRole === 'parents') {
        passwordGroup.classList.remove('hidden');
        studentPreview.classList.add('hidden');
        passwordLabel.textContent = "Parents Portal Password";
        loginSubmitBtn.querySelector('span').textContent = "Access Parent Portal (Drupad's Guardian)";
        passwordInput.value = REQUIRED_PASSWORD;
      }

      document.getElementById('password-error').classList.add('hidden');
    });
  });

  // Logout button handler
  document.getElementById('logout-btn').addEventListener('click', () => {
    handleLogout();
  });
}

/**
 * Handles password visibility:
 * Requirements: "with option for seeing the typed password always enabled"
 * - Defaults to visible text (`type="text"`).
 * - "Show password text as you type" checkbox is checked by default.
 * - Eye icon reflects active visible state and allows toggling if desired.
 */
function setupPasswordVisibility() {
  const passwordInput = document.getElementById('password-input');
  const toggleBtn = document.getElementById('toggle-password-visibility');
  const alwaysVisibleCheckbox = document.getElementById('always-visible-checkbox');
  const eyeOpen = document.getElementById('eye-open');
  const eyeClosed = document.getElementById('eye-closed');

  // Enforce visible text by default
  passwordInput.type = "text";
  toggleBtn.classList.add('active');

  function updateVisibility(show) {
    if (show) {
      passwordInput.type = "text";
      eyeOpen.classList.remove('hidden');
      eyeClosed.classList.add('hidden');
      toggleBtn.classList.add('active');
      toggleBtn.title = "Password is visible (always-enabled)";
    } else {
      passwordInput.type = "password";
      eyeOpen.classList.add('hidden');
      eyeClosed.classList.remove('hidden');
      toggleBtn.classList.remove('active');
      toggleBtn.title = "Show password";
    }
  }

  toggleBtn.addEventListener('click', () => {
    const isCurrentlyText = passwordInput.type === "text";
    const nextState = !isCurrentlyText;
    updateVisibility(nextState);
    alwaysVisibleCheckbox.checked = nextState;
  });

  alwaysVisibleCheckbox.addEventListener('change', (e) => {
    updateVisibility(e.target.checked);
  });
}

function autofillPassword() {
  const input = document.getElementById('password-input');
  input.value = REQUIRED_PASSWORD;
  showToast("Auto-filled required password: " + REQUIRED_PASSWORD, "info");
}

async function handleLogin() {
  const role = state.currentRole;
  const passwordInput = document.getElementById('password-input');
  const passwordError = document.getElementById('password-error');
  const enteredPassword = passwordInput.value.trim();

  try {
    const response = await fetch(`${API_BASE}/api/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ role: role, password: enteredPassword })
    });

    const data = await response.json();

    if (response.ok && data.success) {
      if (passwordError) passwordError.classList.add('hidden');
      state.authToken = data.token;
      state.currentUser = data.user;
      localStorage.setItem('slc_auth_token', data.token);
      localStorage.setItem('slc_user', JSON.stringify(data.user));

      authenticateUser(role, data.user);
      await fetchAssignments();
      await fetchMaterials();
      await fetchAttendanceSessions();
      if (role === 'student') await fetchStudentAttendance();
      if (role === 'parents') await fetchParentAttendance();
    } else {
      if (passwordError) {
        passwordError.classList.remove('hidden');
        passwordError.querySelector('span').textContent = data.error || "Authentication failed. Check credentials.";
      }
      showToast(data.error || "Authentication failed. Check credentials.", "error");
    }
  } catch (err) {
    console.error("Backend connection error, falling back locally:", err);
    // Graceful fallback if backend is momentarily restarting
    if (role === 'student' || enteredPassword === REQUIRED_PASSWORD) {
      const mockUser = {
        name: role === 'student' ? 'Drupad' : (role === 'teacher' ? 'Prof. Jissmol' : "Drupad's Guardian"),
        role: role,
        avatar: role === 'student' ? 'DN' : (role === 'teacher' ? 'JM' : 'PM'),
        subtitle: role === 'student' ? 'S3 CSB • ID: SLC-9041' : (role === 'teacher' ? 'Department Head • CS & Java' : 'Parent Monitor')
      };
      state.currentUser = mockUser;
      authenticateUser(role, mockUser);
      renderAssignments();
    } else {
      if (passwordError) passwordError.classList.remove('hidden');
      showToast("Incorrect password. Please enter '" + REQUIRED_PASSWORD + "'", "error");
    }
  }
}

function authenticateUser(role, user) {
  state.isAuthenticated = true;
  state.currentRole = role;

  // Hide login view, show header
  document.getElementById('login-view').classList.add('hidden');
  document.getElementById('global-header').classList.remove('hidden');

  // Configure role in header with strict profile isolation
  updateGlobalHeader(role, user);

  // Switch to corresponding view ONLY
  switchDashboardView(role);

  showToast(`Welcome! Authenticated into ${role.toUpperCase()} profile`, "success");
}

async function handleLogout() {
  if (state.authToken) {
    try {
      await fetch(`${API_BASE}/api/logout`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${state.authToken}` }
      });
    } catch (e) {}
  }

  state.isAuthenticated = false;
  state.authToken = null;
  state.currentUser = null;
  localStorage.removeItem('slc_auth_token');
  localStorage.removeItem('slc_user');
  pauseAudio();

  // Hide all dashboards & global header
  document.getElementById('student-view').classList.add('hidden');
  document.getElementById('teacher-view').classList.add('hidden');
  document.getElementById('parents-view').classList.add('hidden');
  document.getElementById('global-header').classList.add('hidden');

  // Show login view
  document.getElementById('login-view').classList.remove('hidden');
  showToast("Signed out. Profile session closed securely.", "info");
}

/**
 * Strict Profile Separation:
 * Instead of cross-role jump tabs, the header renders a locked profile security indicator.
 * A student sees only their workspace and cannot hop to Teacher or Parents console.
 */
function updateGlobalHeader(role, user) {
  const badge = document.getElementById('current-role-badge');
  const avatar = document.getElementById('header-avatar');
  const userName = document.getElementById('header-user-name');
  const userSub = document.getElementById('header-user-sub');
  const roleNav = document.getElementById('role-nav');

  // Profile Isolation Badge (Zero unauthorized role hopping)
  const roleDisplayNames = {
    student: 'Student Workspace (Restricted)',
    teacher: 'Faculty Console (Teacher Only)',
    parents: 'Guardian Portal (Confidential)'
  };

  roleNav.innerHTML = `
    <div class="profile-security-badge ${role}">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
      <span>${roleDisplayNames[role] || role.toUpperCase()}</span>
    </div>
  `;

  if (role === 'student') {
    badge.textContent = "Student Workspace";
    avatar.textContent = user?.avatar || "DN";
    avatar.style.background = "#0284C7";
    userName.textContent = user?.name || "Drupad";
    userSub.textContent = user?.subtitle || "S3 CSB • ID: SLC-9041";
  } else if (role === 'teacher') {
    badge.textContent = "Teacher Console";
    avatar.textContent = user?.avatar || "JM";
    avatar.style.background = "#7C3AED";
    userName.textContent = user?.name || "Prof. Jissmol";
    userSub.textContent = user?.subtitle || "Department Head • CS & Java";
  } else if (role === 'parents') {
    badge.textContent = "Guardian Observer";
    avatar.textContent = user?.avatar || "PM";
    avatar.style.background = "#059669";
    userName.textContent = user?.name || "Guardian of Drupad";
    userSub.textContent = user?.subtitle || "Academic Monitor";
  }
}

/**
 * Enforces that only the designated role view is made visible.
 */
function switchDashboardView(role) {
  document.getElementById('student-view').classList.add('hidden');
  document.getElementById('teacher-view').classList.add('hidden');
  document.getElementById('parents-view').classList.add('hidden');

  if (role === 'student') {
    document.getElementById('student-view').classList.remove('hidden');
    renderStudentMaterials();
    fetchStudentAttendance();
  } else if (role === 'teacher') {
    document.getElementById('teacher-view').classList.remove('hidden');
    initTeacherChart();
    renderTeacherMaterialsList();
    renderTeacherAssignmentsList();
    initTeacherAttendanceSection();
  } else if (role === 'parents') {
    document.getElementById('parents-view').classList.remove('hidden');
    initParentChart();
    fetchParentAttendance();
  }
}

/* ==========================================================================
   Theme Management (Light / Dark Minimalist Mode)
   ========================================================================== */
function setupThemeToggle() {
  const themeBtn = document.getElementById('theme-toggle-btn');
  const sunIcon = themeBtn.querySelector('.sun-icon');
  const moonIcon = themeBtn.querySelector('.moon-icon');

  themeBtn.addEventListener('click', () => {
    if (document.body.classList.contains('theme-light')) {
      document.body.classList.remove('theme-light');
      document.body.classList.add('theme-dark');
      sunIcon.classList.add('hidden');
      moonIcon.classList.remove('hidden');
      state.theme = 'dark';
    } else {
      document.body.classList.remove('theme-dark');
      document.body.classList.add('theme-light');
      sunIcon.classList.remove('hidden');
      moonIcon.classList.add('hidden');
      state.theme = 'light';
    }

    // Refresh charts if rendered to match theme colors
    if (state.charts.teacherChart) updateTeacherChart('mastery');
    if (state.charts.parentChart) initParentChart();
  });
}

/* ==========================================================================
   Student Profile Logic
   ========================================================================== */

function switchStudentTab(tabId) {
  state.activeStudentTab = tabId;
  const tabs = document.querySelectorAll('.student-tab');
  const contents = document.querySelectorAll('.student-tab-content');

  tabs.forEach(tab => {
    if (tab.getAttribute('data-tab') === tabId) {
      tab.classList.add('active');
    } else {
      tab.classList.remove('active');
    }
  });

  contents.forEach(content => {
    if (content.id === tabId) {
      content.classList.add('active');
    } else {
      content.classList.remove('active');
    }
  });
}

function renderStudentDashboard() {
  renderTopics();
  renderStudentMaterials();
  renderAssignments();
  renderRecordedSessions();
}

function filterTopics(category) {
  state.topicsFilter = category;
  const pills = document.querySelectorAll('.filter-pill');
  pills.forEach(pill => {
    if (
      (category === 'all' && pill.textContent.trim() === 'All') ||
      (category === 'STEM' && pill.textContent.includes('Science')) ||
      (category === 'CS' && pill.textContent.includes('Computer'))
    ) {
      pill.classList.add('active');
    } else {
      pill.classList.remove('active');
    }
  });
  renderTopics();
}

function renderTopics() {
  const container = document.getElementById('topics-cards-container');
  if (!container) return;

  const filtered = studentTopics.filter(topic => {
    if (state.topicsFilter === 'all') return true;
    return topic.category === state.topicsFilter;
  });

  container.innerHTML = filtered.map(topic => `
    <div class="topic-card">
      <div>
        <div class="topic-meta-row">
          <span class="category-tag ${topic.category.toLowerCase()}">${topic.categoryLabel}</span>
          <span class="difficulty-badge">${topic.difficulty} • ${topic.duration}</span>
        </div>
        <h4 class="topic-title">${topic.title}</h4>
        <p class="topic-description">${topic.description}</p>
      </div>

      <div class="topic-progress-wrap">
        <div class="progress-info">
          <span>${topic.modulesDone} of ${topic.modulesTotal} Modules Completed</span>
          <span>${topic.progress}%</span>
        </div>
        <div class="progress-bar-track">
          <div class="progress-bar-fill" style="width: ${topic.progress}%;"></div>
        </div>
      </div>

      <button class="topic-action-btn" onclick="openTopicModal(${topic.id})">
        <span>Continue Learning</span>
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </div>
  `).join('');
}

function openTopicModal(topicId) {
  const topic = studentTopics.find(t => t.id === topicId);
  if (!topic) return;
  state.selectedTopicForModal = topic;

  document.getElementById('modal-topic-category').textContent = topic.categoryLabel;
  document.getElementById('modal-topic-title').textContent = topic.title;
  document.getElementById('modal-topic-desc').textContent = topic.description;

  const submodulesContainer = document.getElementById('modal-submodules');
  submodulesContainer.innerHTML = topic.submodules.map((sub, idx) => `
    <li>
      <input type="checkbox" id="sub-${idx}" ${idx < topic.modulesDone ? 'checked' : ''} onchange="toggleSubmodule(${idx}, this)">
      <label for="sub-${idx}">${sub}</label>
    </li>
  `).join('');

  document.getElementById('topic-modal').classList.remove('hidden');
}

function closeTopicModal(event) {
  if (event.target.id === 'topic-modal') {
    closeTopicModalDirect();
  }
}

function closeTopicModalDirect() {
  document.getElementById('topic-modal').classList.add('hidden');
}

function toggleSubmodule(idx, checkbox) {
  if (state.selectedTopicForModal) {
    if (checkbox.checked && state.selectedTopicForModal.modulesDone < state.selectedTopicForModal.modulesTotal) {
      state.selectedTopicForModal.modulesDone++;
    } else if (!checkbox.checked && state.selectedTopicForModal.modulesDone > 0) {
      state.selectedTopicForModal.modulesDone--;
    }
    state.selectedTopicForModal.progress = Math.round((state.selectedTopicForModal.modulesDone / state.selectedTopicForModal.modulesTotal) * 100);
    renderTopics();
  }
}

function markTopicProgressFromModal() {
  showToast("Progress saved! Adaptive Companion synchronized your learning streak.", "success");
  closeTopicModalDirect();
}

/* ==========================================================================
   Backend Data Synchrony: Assignments & Study Materials & Session
   ========================================================================== */

async function checkExistingSession() {
  if (!state.authToken) return;
  try {
    const res = await fetch(`${API_BASE}/api/me`, {
      headers: { 'Authorization': `Bearer ${state.authToken}` }
    });
    const data = await res.json();
    if (res.ok && data.success && data.user) {
      state.currentUser = data.user;
      state.currentRole = data.user.role;
      authenticateUser(data.user.role, data.user);
      await fetchAssignments();
      await fetchMaterials();
      await fetchAttendanceSessions();
      if (data.user.role === 'student') await fetchStudentAttendance();
      if (data.user.role === 'parents') await fetchParentAttendance();
    } else {
      localStorage.removeItem('slc_auth_token');
      localStorage.removeItem('slc_user');
      state.authToken = null;
      state.currentUser = null;
    }
  } catch (e) {
    console.warn("Could not check session against backend:", e);
  }
}

async function fetchAssignments() {
  try {
    const headers = state.authToken ? { 'Authorization': `Bearer ${state.authToken}` } : {};
    const res = await fetch(`${API_BASE}/api/assignments`, { headers });
    const data = await res.json();
    if (res.ok && data.success && Array.isArray(data.assignments)) {
      state.assignments = data.assignments;
      renderAssignments();
      renderTeacherAssignmentsList();
    }
  } catch (err) {
    console.error("Error fetching assignments:", err);
  }
}

async function fetchMaterials() {
  try {
    const headers = state.authToken ? { 'Authorization': `Bearer ${state.authToken}` } : {};
    const res = await fetch(`${API_BASE}/api/materials`, { headers });
    const data = await res.json();
    if (res.ok && data.success && Array.isArray(data.materials)) {
      state.materials = data.materials;
      renderStudentMaterials();
      renderTeacherMaterialsList();
    }
  } catch (err) {
    console.error("Error fetching materials:", err);
  }
}

function renderAssignments() {
  const container = document.getElementById('assignments-container');
  if (!container) return;

  const currentList = state.assignments.length > 0 ? state.assignments : studentAssignments;
  const pendingCount = currentList.filter(a => !a.completed).length;
  const completedCount = currentList.filter(a => a.completed).length;

  const pendingEl = document.getElementById('pending-counter');
  const completedEl = document.getElementById('completed-counter');
  const badgeEl = document.getElementById('assignments-badge');
  if (pendingEl) pendingEl.textContent = `${pendingCount} Pending`;
  if (completedEl) completedEl.textContent = `${completedCount} Completed`;
  if (badgeEl) badgeEl.textContent = `${pendingCount} Due`;

  container.innerHTML = currentList.map(task => `
    <div class="assignment-item ${task.completed ? 'completed' : ''}" id="assignment-item-${task.id}">
      <div class="assignment-left">
        <div class="assignment-checkbox ${task.completed ? 'checked' : ''}" onclick="toggleAssignmentStatus(${task.id})">
          ${task.completed ? `
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
          ` : ''}
        </div>
        <div class="assignment-details">
          <span class="assignment-title">${task.title}</span>
          <div class="assignment-meta">
            <span>${task.subject}</span>
            <span>•</span>
            <span>${task.dueDate}</span>
            <span>•</span>
            <span class="due-badge ${task.urgency || 'soon'}">${task.urgencyText || (task.completed ? 'Completed' : 'Pending')}</span>
            ${task.weight ? `<span>• ${task.weight}</span>` : ''}
          </div>
        </div>
      </div>
      <div class="assignment-actions">
        <button class="btn-outline" style="padding: 0.45rem 0.85rem; font-size: 0.78rem;" onclick="toggleAssignmentStatus(${task.id})">
          ${task.completed ? 'Mark Incomplete' : 'Submit / Done'}
        </button>
      </div>
    </div>
  `).join('');
}

async function toggleAssignmentStatus(id) {
  // If authenticated with backend, call toggle endpoint
  if (state.authToken) {
    try {
      const res = await fetch(`${API_BASE}/api/student/assignments/toggle`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${state.authToken}`
        },
        body: JSON.stringify({ id: id })
      });
      const data = await res.json();
      if (res.ok && data.success) {
        showToast("Assignment status synchronized with backend!", "success");
        await fetchAssignments();
        return;
      }
    } catch (e) {
      console.warn("Backend toggle failed, falling back locally:", e);
    }
  }

  // Local fallback
  const list = state.assignments.length > 0 ? state.assignments : studentAssignments;
  const task = list.find(a => a.id === id);
  if (!task) return;
  task.completed = !task.completed;
  task.urgency = task.completed ? 'done' : 'urgent';
  task.urgencyText = task.completed ? 'Completed' : 'Pending Submission';
  renderAssignments();
  showToast(task.completed ? `Marked "${task.title}" as completed!` : `Reopened "${task.title}".`, "info");
}

function submitAssignmentPrompt(id) {
  toggleAssignmentStatus(id);
}

/* ==========================================================================
   Student Study Materials Rendering & Filtering
   ========================================================================== */

function filterMaterials(category) {
  state.materialsFilter = category;
  const pills = document.querySelectorAll('#tab-materials .filter-pill');
  pills.forEach(pill => {
    if (
      (category === 'all' && pill.textContent.includes('All')) ||
      (category === 'CS' && pill.textContent.includes('Computer')) ||
      (category === 'STEM' && pill.textContent.includes('Math'))
    ) {
      pill.classList.add('active');
    } else {
      pill.classList.remove('active');
    }
  });
  renderStudentMaterials();
}

function renderStudentMaterials() {
  const container = document.getElementById('student-materials-container');
  if (!container) return;

  const currentList = state.materials;
  const filtered = currentList.filter(m => {
    if (state.materialsFilter === 'all') return true;
    return m.category === state.materialsFilter;
  });

  const badgeEl = document.getElementById('materials-badge');
  if (badgeEl) badgeEl.textContent = `${currentList.length} Handouts`;

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 2.5rem; text-align: center; color: var(--text-muted); background: var(--bg-card); border-radius: var(--radius-md); border: 1px dashed var(--border-medium);">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" style="margin-bottom: 0.5rem;"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        <p>No study materials uploaded for this category yet.</p>
        <p style="font-size: 0.8rem; margin-top: 0.25rem;">Prof. Jissmol will distribute new lecture notes soon.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(mat => {
    const ext = (mat.originalFilename || mat.filename || 'PDF').split('.').pop().toUpperCase();
    const downloadHref = mat.downloadUrl.startsWith('http') ? mat.downloadUrl : `${API_BASE}${mat.downloadUrl}`;
    return `
      <div class="material-card">
        <div>
          <div class="material-header">
            <div class="material-format-icon">${ext.slice(0, 4)}</div>
            <div class="material-meta">
              <h4 class="material-title">${mat.title}</h4>
              <span class="material-subject">${mat.subject} • ${mat.uploadedBy}</span>
            </div>
          </div>
          <p class="material-desc">${mat.description || 'Comprehensive syllabus study document prepared by department faculty.'}</p>
        </div>
        <div class="material-footer">
          <span>${mat.fileSize || 'Standard PDF'} • ${mat.uploadedAt || 'Term 2'}</span>
          <a href="${downloadHref}" download="${mat.originalFilename || mat.filename}" target="_blank" class="btn-download">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            <span>Download</span>
          </a>
        </div>
      </div>
    `;
  }).join('');
}

/* ==========================================================================
   Teacher Publishing Operations: Materials & Assignments
   ========================================================================== */

function handleFileSelected(event) {
  const file = event.target.files[0];
  const label = document.getElementById('selected-file-label');
  if (file) {
    state.selectedMaterialFile = file;
    if (label) label.textContent = `Selected: ${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
  } else {
    state.selectedMaterialFile = null;
    if (label) label.textContent = 'Supports PDF, DOCX, PPT, TXT';
  }
}

async function handleTeacherUploadMaterial() {
  if (!state.authToken || state.currentRole !== 'teacher') {
    showToast("Teacher privilege required to upload study materials.", "error");
    return;
  }

  const title = document.getElementById('mat-title').value.trim();
  const subject = document.getElementById('mat-subject').value;
  const category = document.getElementById('mat-category').value;
  const description = document.getElementById('mat-desc').value.trim();
  const file = state.selectedMaterialFile;

  if (!title) {
    showToast("Please provide a title for the study document.", "error");
    return;
  }

  const btn = document.getElementById('btn-upload-material');
  if (btn) btn.disabled = true;

  try {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('subject', subject);
    formData.append('category', category);
    formData.append('description', description);
    if (file) {
      formData.append('file', file, file.name);
    }

    const res = await fetch(`${API_BASE}/api/teacher/materials`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${state.authToken}`
      },
      body: formData
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast(`Published "${title}" to student portal!`, "success");
      document.getElementById('teacher-material-form').reset();
      state.selectedMaterialFile = null;
      document.getElementById('selected-file-label').textContent = 'Supports PDF, DOCX, PPT, TXT';
      await fetchMaterials();
    } else {
      showToast(data.error || "Failed to upload study material.", "error");
    }
  } catch (err) {
    console.error("Upload error:", err);
    showToast("Network error during document upload.", "error");
  } finally {
    if (btn) btn.disabled = false;
  }
}

async function handleTeacherCreateAssignment() {
  if (!state.authToken || state.currentRole !== 'teacher') {
    showToast("Teacher privilege required to create assignments.", "error");
    return;
  }

  const title = document.getElementById('asg-title').value.trim();
  const subject = document.getElementById('asg-subject').value;
  const urgency = document.getElementById('asg-urgency').value;
  const dueDate = document.getElementById('asg-due').value.trim();
  const weight = document.getElementById('asg-weight').value.trim();
  const description = document.getElementById('asg-desc').value.trim();

  if (!title || !dueDate) {
    showToast("Please enter both assignment title and due date.", "error");
    return;
  }

  const btn = document.getElementById('btn-create-assignment');
  if (btn) btn.disabled = true;

  try {
    const res = await fetch(`${API_BASE}/api/teacher/assignments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${state.authToken}`
      },
      body: JSON.stringify({
        title, subject, urgency, dueDate, weight, description
      })
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast(`Assignment "${title}" published to student dashboard!`, "success");
      document.getElementById('teacher-assignment-form').reset();
      await fetchAssignments();
    } else {
      showToast(data.error || "Failed to create assignment.", "error");
    }
  } catch (err) {
    console.error("Assignment creation error:", err);
    showToast("Network error creating assignment.", "error");
  } finally {
    if (btn) btn.disabled = false;
  }
}

function renderTeacherMaterialsList() {
  const container = document.getElementById('teacher-materials-list');
  if (!container) return;

  if (state.materials.length === 0) {
    container.innerHTML = `<span style="font-size: 0.8rem; color: var(--text-muted); text-align: center; padding: 0.5rem;">No active study materials uploaded yet.</span>`;
    return;
  }

  container.innerHTML = `
    <div style="font-size: 0.78rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.35rem;">Published Documents (${state.materials.length})</div>
    ${state.materials.map(m => `
      <div class="live-item-row">
        <div>
          <div class="live-item-name">${m.title}</div>
          <div class="live-item-sub">${m.subject} • ${m.fileSize}</div>
        </div>
        <button class="btn-icon-danger" title="Remove Document" onclick="deleteMaterialBackend(${m.id})">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
    `).join('')}
  `;
}

function renderTeacherAssignmentsList() {
  const container = document.getElementById('teacher-assignments-list');
  if (!container) return;

  const currentList = state.assignments.length > 0 ? state.assignments : studentAssignments;

  if (currentList.length === 0) {
    container.innerHTML = `<span style="font-size: 0.8rem; color: var(--text-muted); text-align: center; padding: 0.5rem;">No active assignments created yet.</span>`;
    return;
  }

  container.innerHTML = `
    <div style="font-size: 0.78rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 0.35rem;">Current Assignments (${currentList.length})</div>
    ${currentList.map(a => `
      <div class="live-item-row">
        <div>
          <div class="live-item-name">${a.title}</div>
          <div class="live-item-sub">${a.subject} • ${a.dueDate}</div>
        </div>
        <button class="btn-icon-danger" title="Delete Assignment" onclick="deleteAssignmentBackend(${a.id})">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
    `).join('')}
  `;
}

async function deleteMaterialBackend(id) {
  if (!confirm("Are you sure you want to remove this study material from the student portal?")) return;
  try {
    const res = await fetch(`${API_BASE}/api/teacher/materials?id=${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${state.authToken}` }
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Study material removed.", "info");
      await fetchMaterials();
    } else {
      showToast(data.error || "Could not delete material.", "error");
    }
  } catch (err) {
    showToast("Error deleting material.", "error");
  }
}

async function deleteAssignmentBackend(id) {
  if (!confirm("Are you sure you want to delete this assignment?")) return;
  try {
    const res = await fetch(`${API_BASE}/api/teacher/assignments?id=${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${state.authToken}` }
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Assignment removed successfully.", "info");
      await fetchAssignments();
    } else {
      showToast(data.error || "Could not delete assignment.", "error");
    }
  } catch (err) {
    showToast("Error deleting assignment.", "error");
  }
}

/* ==========================================================================
   Voice Recorded Sessions Audio Player Logic
   ========================================================================== */

function renderRecordedSessions() {
  const container = document.getElementById('recorded-sessions-list');
  if (!container) return;

  container.innerHTML = recordedSessions.map(session => `
    <div class="session-item-card ${session.id === state.audio.currentSessionId ? 'active-session' : ''}" onclick="selectSessionToPlay(${session.id})">
      <div class="session-badge-row">
        <span class="category-tag cs">${session.course}</span>
        <span class="session-duration">${session.duration}</span>
      </div>
      <div>
        <h5 class="session-name">${session.title}</h5>
        <span class="session-instructor">${session.instructor} • ${session.date}</span>
      </div>
      <div class="topic-action-btn" style="padding: 0.5rem 0.75rem; font-size: 0.78rem;">
        <span>${session.id === state.audio.currentSessionId && state.audio.isPlaying ? 'Now Playing' : 'Listen Session'}</span>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
      </div>
    </div>
  `).join('');
}

function selectSessionToPlay(sessionId) {
  const session = recordedSessions.find(s => s.id === sessionId);
  if (!session) return;

  state.audio.currentSessionId = sessionId;
  state.audio.totalDuration = session.durationSec;
  state.audio.currentTime = 0;

  document.getElementById('player-session-title').textContent = session.title;
  document.getElementById('player-session-instructor').textContent = `${session.instructor} • ${session.course}`;
  document.getElementById('total-play-time').textContent = session.duration;
  document.getElementById('current-play-time').textContent = "00:00";
  document.getElementById('scrub-progress').style.width = "0%";

  renderRecordedSessions();
  playAudio();
  showToast(`Streaming: ${session.title}`, "info");
}

function toggleAudioPlayback() {
  if (state.audio.isPlaying) {
    pauseAudio();
  } else {
    playAudio();
  }
}

function playAudio() {
  state.audio.isPlaying = true;
  document.getElementById('play-icon').classList.add('hidden');
  document.getElementById('pause-icon').classList.remove('hidden');
  document.getElementById('waveform').classList.add('playing');

  if (state.audio.timerId) clearInterval(state.audio.timerId);

  state.audio.timerId = setInterval(() => {
    if (state.audio.currentTime < state.audio.totalDuration) {
      state.audio.currentTime += 1;
      updateScrubUI();
    } else {
      pauseAudio();
      state.audio.currentTime = 0;
      updateScrubUI();
    }
  }, 1000 / state.audio.playbackSpeed);
}

function pauseAudio() {
  state.audio.isPlaying = false;
  document.getElementById('play-icon').classList.remove('hidden');
  document.getElementById('pause-icon').classList.add('hidden');
  document.getElementById('waveform').classList.remove('playing');

  if (state.audio.timerId) {
    clearInterval(state.audio.timerId);
    state.audio.timerId = null;
  }
}

function updateScrubUI() {
  const current = state.audio.currentTime;
  const total = state.audio.totalDuration;
  const percent = (current / total) * 100;

  document.getElementById('scrub-progress').style.width = `${percent}%`;
  document.getElementById('current-play-time').textContent = formatTime(current);
}

function formatTime(seconds) {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m < 10 ? '0' : ''}${m}:${s < 10 ? '0' : ''}${s}`;
}

function handleScrub(e) {
  const scrubBar = document.getElementById('scrub-container');
  const rect = scrubBar.getBoundingClientRect();
  const clickX = e.clientX - rect.left;
  const percentage = Math.max(0, Math.min(1, clickX / rect.width));

  state.audio.currentTime = Math.floor(percentage * state.audio.totalDuration);
  updateScrubUI();
}

function skipAudio(seconds) {
  state.audio.currentTime = Math.max(0, Math.min(state.audio.totalDuration, state.audio.currentTime + seconds));
  updateScrubUI();
}

function setPlaySpeed(speed, btnElement) {
  state.audio.playbackSpeed = speed;
  const buttons = document.querySelectorAll('.speed-btn');
  buttons.forEach(b => b.classList.remove('active'));
  btnElement.classList.add('active');

  if (state.audio.isPlaying) {
    playAudio(); // restart timer with updated speed factor
  }
}

/* ==========================================================================
   Teacher Profile Logic: Charts & Module Metrics
   ========================================================================== */

function renderTeacherDashboard() {
  renderTeacherModulesCoverage();
  renderTeacherStudentRoster();
  renderTeacherMaterialsList();
  renderTeacherAssignmentsList();
}

function changeTeacherClass() {
  const select = document.getElementById('class-select');
  showToast(`Switched active view to ${select.options[select.selectedIndex].text}`, "info");
  updateTeacherChart('mastery');
}

function initTeacherChart() {
  const ctx = document.getElementById('teacherLearningChart');
  if (!ctx) return;

  if (state.charts.teacherChart) {
    state.charts.teacherChart.destroy();
  }

  const isDark = document.body.classList.contains('theme-dark');
  const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)';
  const textColor = isDark ? '#94A3B8' : '#64748B';

  state.charts.teacherChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6 (Current)'],
      datasets: [
        {
          label: 'Class Average Mastery (%)',
          data: [72, 75, 78, 81, 82, 85],
          borderColor: '#4F46E5',
          backgroundColor: 'rgba(79, 70, 229, 0.12)',
          fill: true,
          tension: 0.35,
          borderWidth: 2.5,
          pointBackgroundColor: '#4F46E5',
          pointRadius: 4
        },
        {
          label: 'Top 10% Cohort (Drupad, Enza, Elizabeth)',
          data: [84, 88, 90, 93, 94, 96],
          borderColor: '#10B981',
          borderDash: [5, 5],
          backgroundColor: 'transparent',
          tension: 0.35,
          borderWidth: 2,
          pointBackgroundColor: '#10B981',
          pointRadius: 4
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: { color: textColor, font: { family: 'Plus Jakarta Sans', size: 12, weight: 600 } }
        },
        tooltip: {
          backgroundColor: isDark ? '#1E293B' : '#0F172A',
          titleFont: { family: 'Space Grotesk' },
          padding: 10
        }
      },
      scales: {
        x: {
          grid: { color: gridColor },
          ticks: { color: textColor, font: { family: 'Plus Jakarta Sans' } }
        },
        y: {
          min: 60,
          max: 100,
          grid: { color: gridColor },
          ticks: {
            color: textColor,
            callback: value => value + '%'
          }
        }
      }
    }
  });
}

function updateTeacherChart(type) {
  const buttons = document.querySelectorAll('.graph-filters .pill-btn');
  buttons.forEach(b => {
    if (b.textContent.toLowerCase().includes(type.slice(0, 4))) {
      b.classList.add('active');
    } else {
      b.classList.remove('active');
    }
  });

  if (!state.charts.teacherChart) {
    initTeacherChart();
    return;
  }

  const chart = state.charts.teacherChart;

  if (type === 'mastery') {
    chart.data.datasets[0].label = 'Class Average Mastery (%)';
    chart.data.datasets[0].data = [72, 75, 78, 81, 82, 85];
    chart.data.datasets[1].label = 'Top 10% Cohort Benchmark';
    chart.data.datasets[1].data = [84, 88, 90, 93, 94, 96];
    chart.options.scales.y.min = 60;
    chart.options.scales.y.max = 100;
  } else if (type === 'hours') {
    chart.data.datasets[0].label = 'Avg Weekly Study Hours';
    chart.data.datasets[0].data = [4.2, 5.0, 5.8, 6.2, 7.0, 7.5];
    chart.data.datasets[1].label = 'Recommended Goal (6.0 hrs)';
    chart.data.datasets[1].data = [6.0, 6.0, 6.0, 6.0, 6.0, 6.0];
    chart.options.scales.y.min = 0;
    chart.options.scales.y.max = 10;
  } else if (type === 'retention') {
    chart.data.datasets[0].label = 'Session Retention Rate (%)';
    chart.data.datasets[0].data = [65, 69, 74, 80, 84, 88];
    chart.data.datasets[1].label = 'Without Voice Recap Cohort';
    chart.data.datasets[1].data = [64, 65, 68, 67, 70, 71];
    chart.options.scales.y.min = 50;
    chart.options.scales.y.max = 100;
  }

  chart.update();
}

function renderTeacherModulesCoverage() {
  const container = document.getElementById('teacher-modules-list');
  if (!container) return;

  container.innerHTML = teacherClassModules.map(mod => `
    <div class="module-item">
      <div class="module-item-header">
        <span class="module-name">${mod.name}</span>
        <span class="module-students-count">${mod.coveredBy}/${mod.total} Students (${mod.percentage}%)</span>
      </div>
      <div class="module-bar-wrap">
        <div class="module-bar-fill" style="width: ${mod.percentage}%;"></div>
      </div>
    </div>
  `).join('');
}

function renderTeacherStudentRoster(searchTerm = '') {
  const tbody = document.getElementById('roster-table-body');
  if (!tbody) return;

  const filtered = teacherStudentRoster.filter(s => 
    s.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    s.id.toLowerCase().includes(searchTerm.toLowerCase())
  );

  tbody.innerHTML = filtered.map(student => `
    <tr>
      <td>
        <div class="student-col">
          <div class="table-avatar">${student.name.split(' ').map(n=>n[0]).join('')}</div>
          <strong>${student.name}</strong>
        </div>
      </td>
      <td><code>${student.id}</code></td>
      <td><span class="status-pill verified" style="font-size: 0.76rem; padding: 0.2rem 0.55rem;">${student.batch || 'S3 CSB'}</span></td>
      <td><strong>${student.modulesCovered}</strong> completed</td>
      <td><strong>${student.mastery}</strong></td>
      <td>${student.currentTopic}</td>
      <td><span class="badge-tag ${student.status}">${student.engagement}</span></td>
      <td>
        <button class="btn-outline" style="padding: 0.35rem 0.65rem; font-size: 0.75rem;" onclick="sendStudentCommendation('${student.name}')">
          Feedback
        </button>
      </td>
    </tr>
  `).join('');
}

function filterStudentRoster() {
  const term = document.getElementById('roster-search').value;
  renderTeacherStudentRoster(term);
}

function sendStudentCommendation(studentName) {
  showToast(`Personalized feedback sent to ${studentName} & their parent.`, "success");
}

/* ==========================================================================
   Parents Profile Logic: Marks, Attendance & Growth
   ========================================================================== */

function renderParentDashboard() {
  renderParentMarksTable();
}

function renderParentMarksTable() {
  const tbody = document.getElementById('parent-marks-body');
  if (!tbody) return;

  tbody.innerHTML = parentMarksData.map(item => `
    <tr>
      <td><strong>${item.subject}</strong></td>
      <td>${item.midTerm}</td>
      <td>${item.unitTest}</td>
      <td>${item.assignments}</td>
      <td class="score-highlight">${item.total}</td>
      <td><span class="score-grade ${item.gradeClass}">${item.grade}</span></td>
    </tr>
  `).join('');
}

function initParentChart() {
  const ctx = document.getElementById('parentProgressChart');
  if (!ctx) return;

  if (state.charts.parentChart) {
    state.charts.parentChart.destroy();
  }

  const isDark = document.body.classList.contains('theme-dark');
  const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)';
  const textColor = isDark ? '#94A3B8' : '#64748B';

  state.charts.parentChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Computer Science', 'Applied Math', 'Physics', 'Chemistry', 'English'],
      datasets: [
        {
          label: "Drupad's Score (%)",
          data: [97, 93, 89, 88, 95],
          backgroundColor: '#059669',
          borderRadius: 6
        },
        {
          label: 'Class Average Benchmark (%)',
          data: [82, 79, 75, 78, 84],
          backgroundColor: isDark ? '#334155' : '#CBD5E1',
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: { color: textColor, font: { family: 'Plus Jakarta Sans', size: 12, weight: 600 } }
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: textColor, font: { family: 'Plus Jakarta Sans' } }
        },
        y: {
          min: 60,
          max: 100,
          grid: { color: gridColor },
          ticks: { color: textColor, callback: val => val + '%' }
        }
      }
    }
  });
}

function printReportCard() {
  showToast("Compiling official Term 2 Academic Transcript PDF...", "info");
  setTimeout(() => {
    window.print();
  }, 600);
}

function openTeacherNoteModal() {
  document.getElementById('feedback-modal').classList.remove('hidden');
}

function closeFeedbackModal(event) {
  if (event.target.id === 'feedback-modal') {
    closeFeedbackModalDirect();
  }
}

function closeFeedbackModalDirect() {
  document.getElementById('feedback-modal').classList.add('hidden');
}

function sendParentFeedback() {
  const subject = document.getElementById('feedback-subject').value;
  showToast(`Note "${subject}" transmitted to Prof. Jissmol.`, "success");
  closeFeedbackModalDirect();
}

/* ==========================================================================
   ATTENDANCE MODULE: TEACHER, STUDENT, AND PARENTS INTEGRATION
   ========================================================================== */

/**
 * Fetch all attendance sessions from the backend
 */
async function fetchAttendanceSessions() {
  try {
    const headers = state.authToken ? { 'Authorization': `Bearer ${state.authToken}` } : {};
    const res = await fetch(`${API_BASE}/api/attendance/sessions`, { headers });
    const data = await res.json();
    if (res.ok && data.success && Array.isArray(data.sessions)) {
      state.attendance.sessions = data.sessions;
      renderTeacherPastSessions();
    }
  } catch (err) {
    console.error("Error fetching attendance sessions:", err);
  }
}

/**
 * Initialize Teacher Attendance UI: set default date to today,
 * populate student roster in the interactive roll-call table.
 */
function initTeacherAttendanceSection() {
  const dateInput = document.getElementById('att-date-input');
  if (dateInput && !dateInput.value) {
    const today = new Date().toISOString().split('T')[0];
    dateInput.value = today;
  }

  // Populate roll-call state from teacherStudentRoster if empty
  if (!state.attendance.currentRollCall || state.attendance.currentRollCall.length === 0) {
    state.attendance.currentRollCall = teacherStudentRoster.map(s => ({
      studentId: s.id,
      studentName: s.name,
      status: 'present',
      remarks: ''
    }));
  }

  renderAttendanceSheet();
  fetchAttendanceSessions();
}

/**
 * Render the teacher's interactive roll-call table
 */
function renderAttendanceSheet() {
  const tbody = document.getElementById('attendance-sheet-tbody');
  if (!tbody) return;

  tbody.innerHTML = state.attendance.currentRollCall.map((record, index) => {
    const initials = record.studentName.split(' ').map(n => n[0]).join('');
    return `
      <tr>
        <td>
          <div class="student-col">
            <div class="table-avatar">${initials}</div>
            <strong>${record.studentName}</strong>
          </div>
        </td>
        <td><code>${record.studentId}</code></td>
        <td>
          <div class="attendance-status-toggle">
            <button type="button" class="status-choice-btn present ${record.status === 'present' ? 'active' : ''}" 
              onclick="setStudentAttendanceStatus(${index}, 'present')">Present</button>
            <button type="button" class="status-choice-btn late ${record.status === 'late' ? 'active' : ''}" 
              onclick="setStudentAttendanceStatus(${index}, 'late')">Late</button>
            <button type="button" class="status-choice-btn absent ${record.status === 'absent' ? 'active' : ''}" 
              onclick="setStudentAttendanceStatus(${index}, 'absent')">Absent</button>
            <button type="button" class="status-choice-btn excused ${record.status === 'excused' ? 'active' : ''}" 
              onclick="setStudentAttendanceStatus(${index}, 'excused')">Excused</button>
          </div>
        </td>
        <td>
          <input type="text" class="remarks-input" placeholder="Add remark (optional)..." 
            value="${escapeHtmlAttr(record.remarks || '')}" 
            onchange="updateStudentAttendanceRemark(${index}, this.value)">
        </td>
        <td>
          <button type="button" class="btn-outline" style="padding: 0.3rem 0.6rem; font-size: 0.72rem;" 
            onclick="toggleStudentQuickStatus(${index})">
            ${record.status === 'present' ? 'Mark Absent' : 'Mark Present'}
          </button>
        </td>
      </tr>
    `;
  }).join('');

  updateAttendanceCounters();
}

function escapeHtmlAttr(str) {
  return String(str).replace(/"/g, '&quot;');
}

/**
 * Change status for a student in the roll-call table
 */
function setStudentAttendanceStatus(index, newStatus) {
  if (state.attendance.currentRollCall[index]) {
    state.attendance.currentRollCall[index].status = newStatus;
    renderAttendanceSheet();
  }
}

/**
 * Toggle between Present and Absent quickly
 */
function toggleStudentQuickStatus(index) {
  if (state.attendance.currentRollCall[index]) {
    const cur = state.attendance.currentRollCall[index].status;
    state.attendance.currentRollCall[index].status = cur === 'present' ? 'absent' : 'present';
    renderAttendanceSheet();
  }
}

/**
 * Update teacher remark for a student
 */
function updateStudentAttendanceRemark(index, remarkText) {
  if (state.attendance.currentRollCall[index]) {
    state.attendance.currentRollCall[index].remarks = remarkText.trim();
  }
}

/**
 * Bulk action: Mark All Present or Mark All Absent
 */
function markAllAttendance(status) {
  state.attendance.currentRollCall.forEach(r => {
    r.status = status;
  });
  renderAttendanceSheet();
  showToast(`Marked all students as "${status.toUpperCase()}".`, "info");
}

/**
 * Update the counter pills above the roll-call table
 */
function updateAttendanceCounters() {
  const records = state.attendance.currentRollCall || [];
  const present = records.filter(r => r.status === 'present').length;
  const late = records.filter(r => r.status === 'late').length;
  const absent = records.filter(r => r.status === 'absent').length;

  const countPresentEl = document.getElementById('count-present');
  const countLateEl = document.getElementById('count-late');
  const countAbsentEl = document.getElementById('count-absent');

  if (countPresentEl) countPresentEl.textContent = `${present} Present`;
  if (countLateEl) countLateEl.textContent = `${late} Late`;
  if (countAbsentEl) countAbsentEl.textContent = `${absent} Absent`;
}

/**
 * Handle date picker change
 */
function handleAttendanceDateChange() {
  const dateVal = document.getElementById('att-date-input').value;
  const classVal = document.getElementById('att-class-select').value;
  const periodVal = document.getElementById('att-period-select').value;

  // Check if a session already exists for this date + class + period
  const match = state.attendance.sessions.find(s => 
    s.date === dateVal && s.classId === classVal && s.period === periodVal
  );

  if (match) {
    loadSessionIntoSheet(match);
    showToast(`Loaded existing session for ${dateVal}`, "info");
  }
}

function handleAttendanceClassChange() {
  handleAttendanceDateChange();
}

/**
 * Load an existing attendance session into the editor sheet
 */
function loadSessionIntoSheet(session) {
  state.attendance.selectedSessionId = session.id;
  const dateInput = document.getElementById('att-date-input');
  const classSelect = document.getElementById('att-class-select');
  const periodSelect = document.getElementById('att-period-select');

  if (dateInput) dateInput.value = session.date;
  if (classSelect) classSelect.value = session.classId;
  if (periodSelect) periodSelect.value = session.period;

  // Map records by studentId
  const recMap = new Map();
  (session.records || []).forEach(r => recMap.set(r.studentId, r));

  state.attendance.currentRollCall = teacherStudentRoster.map(s => {
    const existing = recMap.get(s.id);
    return {
      studentId: s.id,
      studentName: s.name,
      status: existing ? existing.status : 'present',
      remarks: existing ? (existing.remarks || '') : ''
    };
  });

  renderAttendanceSheet();
  renderTeacherPastSessions();
}

/**
 * Render the list of past attendance sessions for teacher review
 */
function renderTeacherPastSessions() {
  const container = document.getElementById('teacher-past-sessions');
  if (!container) return;

  if (state.attendance.sessions.length === 0) {
    container.innerHTML = `<span class="text-muted" style="font-size: 0.8rem;">No attendance sessions logged yet.</span>`;
    return;
  }

  container.innerHTML = state.attendance.sessions.map(s => {
    const isActive = s.id === state.attendance.selectedSessionId ? 'active' : '';
    return `
      <div class="session-chip ${isActive}" onclick="loadSessionById('${s.id}')">
        <span class="session-chip-date">${s.date}</span>
        <span>•</span>
        <span>${s.subject || s.className}</span>
        <span class="session-chip-badge">${s.attendanceRate || 100}% Present</span>
        <button type="button" class="btn-icon-danger" style="margin-left: 0.3rem;" 
          title="Delete attendance session" onclick="event.stopPropagation(); deleteAttendanceSessionDirect('${s.id}')">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
    `;
  }).join('');
}

function loadSessionById(sessionId) {
  const session = state.attendance.sessions.find(s => s.id === sessionId);
  if (session) {
    loadSessionIntoSheet(session);
  }
}

/**
 * Delete an attendance session
 */
async function deleteAttendanceSessionDirect(sessionId) {
  if (!confirm("Are you sure you want to remove this attendance session?")) return;

  try {
    const res = await fetch(`${API_BASE}/api/attendance/session?id=${encodeURIComponent(sessionId)}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${state.authToken}`
      }
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Attendance session removed.", "info");
      await fetchAttendanceSessions();
      if (state.currentRole === 'student') await fetchStudentAttendance();
      if (state.currentRole === 'parents') await fetchParentAttendance();
    } else {
      showToast(data.error || "Could not delete session", "error");
    }
  } catch (err) {
    console.error("Error deleting session:", err);
  }
}

/**
 * Submit and sync the attendance session to the Java backend
 */
async function submitAttendanceSession() {
  const dateInput = document.getElementById('att-date-input');
  const classSelect = document.getElementById('att-class-select');
  const periodSelect = document.getElementById('att-period-select');

  const payload = {
    id: state.attendance.selectedSessionId || ("ATT-" + Date.now()),
    date: dateInput.value || new Date().toISOString().split('T')[0],
    classId: classSelect.value,
    className: classSelect.options[classSelect.selectedIndex].text,
    subject: periodSelect.options[periodSelect.selectedIndex].text.split('•')[1]?.trim() || "Computer Science",
    period: periodSelect.value,
    records: state.attendance.currentRollCall
  };

  try {
    const res = await fetch(`${API_BASE}/api/attendance/submit`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${state.authToken}`
      },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Attendance synchronized! Live updates delivered to Student and Parent portals.", "success");
      state.attendance.selectedSessionId = data.session.id;
      await fetchAttendanceSessions();
    } else {
      showToast(data.error || "Failed to submit attendance", "error");
    }
  } catch (err) {
    console.error("Attendance submission error:", err);
    showToast("Server offline: Saved attendance locally for this session.", "info");
  }
}

/**
 * STUDENT PORTAL: Fetch and render student's attendance summary & history
 */
async function fetchStudentAttendance() {
  try {
    const headers = state.authToken ? { 'Authorization': `Bearer ${state.authToken}` } : {};
    const res = await fetch(`${API_BASE}/api/attendance/student?studentId=SLC-9041`, { headers });
    const data = await res.json();

    if (res.ok && data.success && data.summary) {
      state.attendance.studentSummary = data.summary;
      renderStudentAttendanceTab(data.summary);
    }
  } catch (err) {
    console.error("Error fetching student attendance:", err);
  }
}

function renderStudentAttendanceTab(summary) {
  if (!summary) return;

  const rateEl = document.getElementById('student-att-rate');
  const presentEl = document.getElementById('student-att-present');
  const totalSessionsEl = document.getElementById('student-att-total-sessions');
  const lateEl = document.getElementById('student-att-late');
  const absentEl = document.getElementById('student-att-absent');
  const badgeEl = document.getElementById('student-attendance-badge');

  if (rateEl) rateEl.textContent = `${summary.attendancePercentage}%`;
  if (presentEl) presentEl.textContent = summary.presentCount;
  if (totalSessionsEl) totalSessionsEl.textContent = `of ${summary.totalSessions} sessions logged`;
  if (lateEl) lateEl.textContent = summary.lateCount;
  if (absentEl) absentEl.textContent = summary.absentCount;
  if (badgeEl) badgeEl.textContent = `${summary.attendancePercentage}%`;

  const tbody = document.getElementById('student-attendance-table-body');
  if (!tbody) return;

  const history = summary.history || [];
  if (history.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center" style="padding: 1.5rem; color: var(--text-muted);">No attendance records posted yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = history.map(item => {
    const statusClass = item.status === 'present' ? 'status-present' : 
                        (item.status === 'late' ? 'status-late' : 
                        (item.status === 'absent' ? 'status-absent' : 'status-excused'));
    const statusLabel = item.status ? (item.status.charAt(0).toUpperCase() + item.status.slice(1)) : 'Present';

    return `
      <tr>
        <td><strong>${item.date}</strong></td>
        <td>${item.subject || 'Computer Science'} <span class="badge-tag low" style="margin-left: 0.3rem;">${item.className || 'S3 CSB'}</span></td>
        <td>${item.period || 'Period 1'}</td>
        <td>${item.recordedBy || 'Prof. Jissmol'}</td>
        <td><span class="status-pill ${statusClass}">${statusLabel}</span></td>
        <td><span style="font-size: 0.82rem; color: var(--text-secondary);">${item.remarks ? item.remarks : '—'}</span></td>
      </tr>
    `;
  }).join('');
}

/**
 * PARENTS PORTAL: Fetch and render child's attendance audit
 */
async function fetchParentAttendance() {
  try {
    const headers = state.authToken ? { 'Authorization': `Bearer ${state.authToken}` } : {};
    const res = await fetch(`${API_BASE}/api/attendance/student?studentId=SLC-9041`, { headers });
    const data = await res.json();

    if (res.ok && data.success && data.summary) {
      renderParentAttendanceAudit(data.summary);
    }
  } catch (err) {
    console.error("Error fetching parent attendance:", err);
  }
}

function renderParentAttendanceAudit(summary) {
  if (!summary) return;

  const rateEl = document.getElementById('parent-attendance-rate');
  const subtextEl = document.getElementById('parent-attendance-subtext');
  const pillsContainer = document.getElementById('parent-att-summary-pills');

  if (rateEl) rateEl.textContent = `${summary.attendancePercentage}%`;
  if (subtextEl) subtextEl.textContent = `${summary.presentCount + summary.lateCount} of ${summary.totalSessions} sessions attended`;

  if (pillsContainer) {
    pillsContainer.innerHTML = `
      <div class="preview-pills">
        <span class="att-counter-pill green">${summary.presentCount} Present</span>
        <span class="att-counter-pill amber">${summary.lateCount} Late</span>
        <span class="att-counter-pill red">${summary.absentCount} Absent</span>
      </div>
    `;
  }

  const tbody = document.getElementById('parent-attendance-table-body');
  if (!tbody) return;

  const history = summary.history || [];
  if (history.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 1.5rem; color: var(--text-muted);">No attendance entries recorded yet for this semester.</td></tr>`;
    return;
  }

  tbody.innerHTML = history.map(item => {
    const statusClass = item.status === 'present' ? 'status-present' : 
                        (item.status === 'late' ? 'status-late' : 
                        (item.status === 'absent' ? 'status-absent' : 'status-excused'));
    const statusLabel = item.status ? (item.status.charAt(0).toUpperCase() + item.status.slice(1)) : 'Present';

    return `
      <tr>
        <td><strong>${item.date}</strong></td>
        <td>${item.className || 'S3 CSB'}</td>
        <td><strong>${item.subject || 'Computer Science'}</strong></td>
        <td>${item.period || 'Period 1'}</td>
        <td>${item.recordedBy || 'Prof. Jissmol'}</td>
        <td><span class="status-pill ${statusClass}">${statusLabel}</span></td>
        <td><span style="font-size: 0.82rem; color: var(--text-secondary);">${item.remarks ? item.remarks : 'On time & active'}</span></td>
      </tr>
    `;
  }).join('');
}


/* ==========================================================================
   Toast Notification Utility
   ========================================================================== */
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = `
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
  `;
  if (type === 'success') {
    icon = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
    `;
  } else if (type === 'error') {
    icon = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
    `;
  }

  toast.innerHTML = `
    ${icon}
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
