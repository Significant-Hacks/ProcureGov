# ProcureGov — Ministry of Public Works
## Tender Management System: Project Documentation

---

## 1. Cover Page

| Field | Value |
|---|---|
| **Student Name** | _[Enter your full name]_ |
| **Student Number** | _[Enter your student number]_ |
| **Project Title** | ProcureGov — Ministry of Public Works Tender Management System |
| **Submission Date** | _[Enter submission date]_ |
| **Module** | _[Enter module name/code]_ |
| **Institution** | _[Enter institution name]_ |

---

## 2. System Overview

ProcureGov is a web-based e-Procurement system developed for the **Kingdom of Lesotho Ministry of Public Works** to digitise and enforce the public tender management lifecycle. It replaces manual, paper-based procurement workflows with a transparent, auditable, and role-controlled digital platform.

### Problem Solved

Prior to ProcureGov, the Ministry managed tenders through physical documents and informal communication, leading to: lack of transparency in evaluation and awarding; missed deadlines with no server-side enforcement; inconsistent scoring across evaluators; no audit trail for status transitions; and delayed notifications. ProcureGov solves these by enforcing a strict lifecycle (Draft → Open → Closed → Under Evaluation → Evaluated → Awarded), implementing a weighted multi-criteria scoring algorithm, automating deadline enforcement and status transitions, and providing email notifications at each critical stage.

### User Roles

- **Supplier**: Registers on the platform, browses open tenders, submits bids with technical compliance data and documents, tracks bid status, and receives award notifications. Cannot view other suppliers' bids or scores.

- **Procurement Officer**: Creates tender notices in Draft, publishes them (Draft → Open), initiates evaluation (Closed → Under Evaluation), and awards contracts (Evaluated → Awarded). Has full visibility into all bids and evaluation results. Officers are auto-assigned as evaluators when evaluation starts.

- **Evaluation Committee Member**: Assigned to tenders entering evaluation. Scores bids by submitting a technical score (0–100); the system auto-calculates price and timeline scores. Can view other evaluators' scores only after submitting their own. The system auto-transitions a tender to "Evaluated" once all evaluators score all bids.

### Technology Stack

| Layer | Technology |
|---|---|
| Server | Apache Tomcat 9.x (Java EE 7 / Servlet 3.1) |
| Language | Java 8+ (JSP 2.3, Servlet 3.1) |
| Database | MySQL 8.x (InnoDB, utf8mb4) |
| Connection Pool | JNDI DataSource (`jdbc/ProcureGovDB`) |
| Frontend | HTML5, CSS3, JSTL 1.2, vanilla JavaScript |
| Security | SHA-256 hashing, session-based auth, role-based filter |
| Email | JavaMail API via SMTP TLS (port 587) |
| File Upload | Servlet 3.1 `Part` API |
| Build | Apache NetBeans with Ant |

[INSERT SCREENSHOT HERE: Screenshot of the ProcureGov login page showing the Coat of Arms branding and login form]

---

## 3. Architecture Diagram & Explanation

[INSERT DIAGRAM HERE: Draw a labelled MVC architecture diagram showing: (1) Browser/Client, (2) JSP Views, (3) Servlet Controllers, (4) Service Layer (EvaluationService, EmailService, NotificationService), (5) DAO Layer, (6) MySQL Database. Draw arrows: Browser → Servlet → DAO → DB and DB → DAO → Servlet → JSP → Browser. Show AuthRedirectFilter between Browser and Servlets.]

### 3.1 MVC Pattern

ProcureGov implements **Model-View-Controller (MVC)** using Java EE. Servlets are Controllers, JSPs/JSTL are Views, and DAOs/JavaBeans are the Model.

### 3.2 Controller Layer — Servlets

The system contains **20 servlets** in `com.procuregov.controller`, mapped via `web.xml`:

**Auth** (`controller.auth`): `LoginServlet` (`/login`), `LogoutServlet` (`/logout`), `RegisterServlet` (`/register`), `ForgotPasswordServlet` (`/forgot-password`), `ResetPasswordServlet` (`/reset-password`), `ConfirmAccountServlet` (`/confirm-account`), `ResendConfirmationServlet` (`/resend-confirmation`), `EmailVerificationServlet` (`/email-verify`)

**Officer** (`controller.officer`): `OfficerDashboardServlet` (`/officer-dashboard`), `CreateTenderServlet` (`/create-tender`), `EditTenderServlet` (`/edit-tender`), `TenderStatusServlet` (`/tender-status`), `ManageTendersServlet` (`/manage-tenders`), `AwardContractServlet` (`/award-contract`), `BidReportServlet` (`/bid-report`)

**Supplier** (`controller.supplier`): `SupplierDashboardServlet` (`/supplier-dashboard`), `OpenTendersServlet` (`/open-tenders`), `TenderDetailServlet` (`/tender-detail`), `SubmitBidServlet` (`/submit-bid`)

**Evaluator** (`controller.evaluator`): `EvaluatorDashboardServlet` (`/evaluator-dashboard`), `EvaluationListServlet` (`/evaluations`), `EvaluationPanelServlet` (`/evaluation-panel`), `SubmitScoreServlet` (`/submit-score`)

**Common** (`controller.common`): `DownloadServlet` (`/download`), `AwardNoticeServlet` (`/award-notice`), `ProfileServlet` (`/profile`)

### 3.3 View Layer — JSPs with JSTL

JSPs under `/web/pages/` by role: `common/` (login, register, forgot-password, reset-password), `officer/` (dashboard, create-tender, manage-tenders, award-contract, bid-report), `supplier/` (dashboard, open-tenders, tender-detail), `evaluator/` (dashboard, evaluation-list, evaluation-panel). Reusable fragments in `/WEB-INF/fragments/`: `header.jsp`, `sidebar.jsp`, `footer.jsp`. All iteration and conditionals use JSTL — no scriptlets.

### 3.4 Model Layer — DAOs and JavaBeans

**Entity classes** (`com.procuregov.model`): `User`, `Supplier`, `Tender`, `Bid`, `BidTechnicalCriterion`, `Evaluation`, `Award`, `UserToken`

**DAO interfaces & implementations** (`com.procuregov.dao` / `dao.impl`): `UserDAO`/`UserDAOImpl`, `SupplierDAO`/`SupplierDAOImpl`, `TenderDAO`/`TenderDAOImpl`, `BidDAO`/`BidDAOImpl`, `EvaluationDAO`/`EvaluationDAOImpl`, `EvaluatorDAO`/`EvaluatorDAOImpl`, `AwardDAO`/`AwardDAOImpl`, `BidTechnicalCriterionDAO`/`BidTechnicalCriterionDAOImpl`, `UserTokenDAO`/`UserTokenDAOImpl`

All DAOs use JDBC `PreparedStatement` with parameterised queries. Connections via `DBConnectionUtil.getConnection()` which performs JNDI lookup on `java:comp/env/jdbc/ProcureGovDB`.

### 3.5 Service Layer

| Service | Purpose |
|---|---|
| `EvaluationService` | Computes price/timeline/weighted scores; checks evaluation completion for auto-transition |
| `EmailService` | Sends HTML emails via JavaMail TLS SMTP |
| `NotificationService` | Orchestrates notifications to suppliers, evaluators, officers |
| `PdfGenerationService` | Generates award notice PDFs via iText |

### 3.6 Security Layer

- **`AuthRedirectFilter`**: `javax.servlet.Filter` mapped to `/pages/officer/*`, `/pages/supplier/*`, `/pages/evaluator/*`, `/pages/profile/*`. Verifies session and enforces role-based access.
- **`SessionUtil`**: Manages session attributes (userId, userRole, userName, failedLoginAttempts).
- **`PasswordUtil`**: SHA-256 password hashing and verification.
- **Login Lockout**: `LoginServlet` enforces 3-attempt session-based lockout.

---

## 4. Database Design — ERD & Justification

[INSERT DIAGRAM HERE: Draw an Entity-Relationship Diagram (ERD) based on schema.sql. Show all 11 tables with columns, primary keys (underlined), foreign keys (dashed arrows to parent tables), and relationship cardinality labels (1:1, 1:N, M:N).]

### 4.1 Schema Overview

The ProcureGov database (`DavidMohale2333908`) consists of **11 tables** in MySQL 8.x using InnoDB with `utf8mb4` character encoding.

### 4.2 Table Definitions

#### `users` — Central Authentication Table

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `email` | `VARCHAR(255)` | `UNIQUE NOT NULL` |
| `password_hash` | `VARCHAR(64)` | `NOT NULL` — SHA-256 hex digest |
| `role` | `ENUM('SUPPLIER','OFFICER','EVALUATOR')` | `NOT NULL` |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` |
| `failed_login_attempts` | `INT` | `NOT NULL DEFAULT 0` |
| `account_locked` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` |
| `created_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |
| `updated_at` | `DATETIME` `ON UPDATE CURRENT_TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

#### `suppliers` — Supplier Profile (1:1 with `users`)

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `user_id` | `INT` | `NOT NULL UNIQUE`, **FK → users(id) ON DELETE CASCADE** |
| `company_name` | `VARCHAR(255)` | `NOT NULL` |
| `registration_number` | `VARCHAR(50)` | `NOT NULL UNIQUE` — format `SUP-YYYY-NNNN` |
| `physical_address` | `VARCHAR(500)` | `NOT NULL` |
| `contact_number` | `VARCHAR(20)` | `NOT NULL` |
| `is_verified` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` |

#### `officers` — Procurement Officer Profile (1:1 with `users`)

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `user_id` | `INT` | `NOT NULL UNIQUE`, **FK → users(id) ON DELETE CASCADE** |
| `full_name` | `VARCHAR(255)` | `NOT NULL` |
| `department` | `VARCHAR(255)` | `NOT NULL` |
| `staff_id` | `VARCHAR(50)` | `NOT NULL UNIQUE` |

#### `evaluators` — Evaluation Committee Member Profile (1:1 with `users`)

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `user_id` | `INT` | `NOT NULL UNIQUE`, **FK → users(id) ON DELETE CASCADE** |
| `full_name` | `VARCHAR(255)` | `NOT NULL` |
| `department` | `VARCHAR(255)` | `NOT NULL` |
| `staff_id` | `VARCHAR(50)` | `NOT NULL UNIQUE` |

#### `user_tokens` — Password Reset & Account Confirmation

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `user_id` | `INT` | `NOT NULL`, **FK → users(id) ON DELETE CASCADE** |
| `token` | `VARCHAR(64)` | `NOT NULL UNIQUE` |
| `token_type` | `ENUM('PASSWORD_RESET','ACCOUNT_CONFIRMATION')` | `NOT NULL` |
| `expires_at` | `DATETIME` | `NOT NULL` |
| `used` | `BOOLEAN` | `NOT NULL DEFAULT FALSE` |
| `created_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

**Indexes**: `idx_token(token)`, `idx_user_token_type(user_id, token_type)`

#### `tenders` — Tender Notices

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `reference_number` | `VARCHAR(20)` | `NOT NULL UNIQUE` — format `MPW-YYYY-NNNN` |
| `title` | `VARCHAR(255)` | `NOT NULL` |
| `category` | `ENUM('Construction','Roads','Electrical','Plumbing','General Services')` | `NOT NULL` |
| `description` | `TEXT` | `NOT NULL` |
| `estimated_value` | `DECIMAL(15,2)` | `NOT NULL` |
| `submission_deadline` | `DATETIME` | `NOT NULL` |
| `notice_document_path` | `VARCHAR(500)` | Nullable |
| `show_estimated_value` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` |
| `status` | `ENUM('Draft','Open','Closed','Under Evaluation','Evaluated','Awarded')` | `NOT NULL DEFAULT 'Draft'` |
| `created_by` | `INT` | `NOT NULL`, **FK → users(id)** |
| `created_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |
| `updated_at` | `DATETIME` `ON UPDATE CURRENT_TIMESTAMP` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

**Indexes**: `idx_status(status)`, `idx_deadline(submission_deadline)`, `idx_reference(reference_number)`

#### `tender_evaluators` — Evaluator-Tender Assignment (M:N Junction)

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `tender_id` | `INT` | `NOT NULL`, **FK → tenders(id) ON DELETE CASCADE** |
| `evaluator_id` | `INT` | `NOT NULL`, **FK → evaluators(id) ON DELETE CASCADE** |
| `assigned_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

**Unique Key**: `uk_tender_evaluator(tender_id, evaluator_id)`

#### `bids` — Supplier Bid Submissions

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `tender_id` | `INT` | `NOT NULL`, **FK → tenders(id) ON DELETE CASCADE** |
| `supplier_id` | `INT` | `NOT NULL`, **FK → suppliers(id) ON DELETE CASCADE** |
| `bid_amount` | `DECIMAL(15,2)` | `NOT NULL` |
| `technical_compliance` | `TEXT` | `NOT NULL` |
| `proposed_timeline_days` | `INT` | `NOT NULL` |
| `document_path` | `VARCHAR(500)` | Nullable |
| `submitted_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

**Unique Key**: `uk_supplier_tender(supplier_id, tender_id)` — one bid per supplier per tender.
**Index**: `idx_tender(tender_id)`

#### `bid_technical_criteria` — Structured Technical Compliance

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `bid_id` | `INT` | `NOT NULL`, **FK → bids(id) ON DELETE CASCADE** |
| `criterion_name` | `VARCHAR(255)` | `NOT NULL` |
| `criterion_type` | `ENUM('Equipment','Certifications','Experience','QualityStandards','Methodology','Personnel','Other')` | `NOT NULL` |
| `criterion_value` | `VARCHAR(500)` | `NOT NULL` |
| `evidence_document_path` | `VARCHAR(500)` | Nullable |

**Index**: `idx_bid(bid_id)`

#### `evaluations` — Individual Evaluator Scores

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `bid_id` | `INT` | `NOT NULL`, **FK → bids(id) ON DELETE CASCADE** |
| `tender_id` | `INT` | `NOT NULL`, **FK → tenders(id) ON DELETE CASCADE** |
| `evaluator_id` | `INT` | `NOT NULL`, **FK → evaluators(id) ON DELETE CASCADE** |
| `technical_score` | `DECIMAL(5,2)` | `NOT NULL` — evaluator-entered (0–100) |
| `price_score` | `DECIMAL(5,2)` | `NOT NULL` — auto-calculated |
| `timeline_score` | `DECIMAL(5,2)` | `NOT NULL` — auto-calculated |
| `weighted_total` | `DECIMAL(7,2)` | `NOT NULL` — auto-calculated |
| `submitted_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

**Unique Key**: `uk_bid_evaluator(bid_id, evaluator_id)` — one score per evaluator per bid.
**Index**: `idx_bid(bid_id)`

#### `awards` — Contract Award Records

| Column | Data Type | Constraints |
|---|---|---|
| `id` | `INT AUTO_INCREMENT` | **PRIMARY KEY** |
| `tender_id` | `INT` | `NOT NULL UNIQUE`, **FK → tenders(id)** |
| `winning_bid_id` | `INT` | `NOT NULL`, **FK → bids(id)** |
| `awarded_value` | `DECIMAL(15,2)` | `NOT NULL` |
| `justification` | `TEXT` | `NOT NULL` |
| `awarded_by` | `INT` | `NOT NULL`, **FK → users(id)** |
| `awarded_at` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |
| `confirmation_document_path` | `VARCHAR(500)` | Nullable |

### 4.3 Third Normal Form (3NF) Justification

**1NF**: Every table has a defined primary key, and all columns contain atomic values. `ENUM` types for `role`, `status`, `category`, `token_type`, and `criterion_type` ensure domain integrity at the database level.

**2NF**: All non-key attributes are fully dependent on the entire primary key. Every table uses a single-column surrogate primary key (`id AUTO_INCREMENT`), eliminating partial dependencies. The `tender_evaluators` junction table's non-key column (`assigned_at`) depends on the full combination.

**3NF**: No transitive dependencies exist. Role-specific profile data is separated into `suppliers`, `officers`, and `evaluators` rather than stored in `users`. Token data is in `user_tokens` rather than adding token columns to `users`. Evaluation scores are in `evaluations` rather than derived columns on `bids`. The `weighted_total` in `evaluations` is a computed value stored to preserve the exact score at submission time, preventing retroactive changes if other bids are added later.

### 4.4 Indexing Decisions

- **`idx_status`** on `tenders(status)`: Supports `countByStatus()` and `getByStatus()` — the most frequent dashboard queries.
- **`idx_deadline`** on `tenders(submission_deadline)`: Supports `autoCloseExpiredTenders()` (`WHERE submission_deadline < NOW()`), running on every officer dashboard load.
- **`idx_reference`** on `tenders(reference_number)`: Supports lookups by unique reference number.
- **`idx_token`** on `user_tokens(token)`: O(1) token lookups during password reset and account confirmation.
- **`idx_user_token_type`** composite on `user_tokens(user_id, token_type)`: Supports checking for active tokens of a given type per user.
- **`idx_tender`** on `bids(tender_id)`: Supports fetching all bids for a tender during evaluation.
- **`idx_bid`** on `evaluations(bid_id)` and `bid_technical_criteria(bid_id)`: Supports fetching scores/criteria per bid.

---

## 5. Tender Lifecycle Implementation

### 5.1 Status State Machine

The tender lifecycle is a strict forward-only state machine with six states:

```
Draft ──publish──▶ Open ──auto-close──▶ Closed ──startEvaluation──▶ Under Evaluation ──auto──▶ Evaluated ──award──▶ Awarded
```

The `tenders.status` column uses a MySQL `ENUM('Draft','Open','Closed','Under Evaluation','Evaluated','Awarded')` to enforce valid values at the database level. No backward transitions are permitted.

### 5.2 Servlet-Handled Transitions

All manual transitions are handled by `TenderStatusServlet` (mapped to `/tender-status`). Its `doPost()` method validates the current status before applying the requested action:

```java
switch (action) {
    case "publish":
        if ("Draft".equals(currentStatus)) newStatus = "Open";
        break;
    case "startEvaluation":
        if ("Closed".equals(currentStatus)) newStatus = "Under Evaluation";
        break;
    case "award":
        if ("Evaluated".equals(currentStatus)) newStatus = "Awarded";
        break;
    default:
        break;
}
```

If the action does not match a valid transition, `newStatus` remains `null` and an error is returned: *"Invalid status transition from [current] for action [action]"*. This prevents invalid jumps (e.g., Draft → Awarded) and backward transitions (e.g., Open → Draft).

When transitioning to **"Under Evaluation"**, the servlet auto-assigns all evaluators:

```java
if ("Under Evaluation".equals(newStatus)) {
    int assigned = evaluatorDAO.assignAllEvaluatorsToTender(tenderId);
}
```

When transitioning to **"Open"**, the servlet sends email notifications to all active suppliers:

```java
if ("Open".equals(newStatus) && emailService != null) {
    List<String> supplierEmails = supplierDAO.getAllActiveEmails();
    emailService.notifyTenderPublished(supplierEmails, ...);
}
```

### 5.3 Automatic Closing — Server-Side Deadline Enforcement

The **Open → Closed** transition is fully automated on the server side. `TenderDAOImpl.autoCloseExpiredTenders()` executes:

```sql
UPDATE tenders SET status = 'Closed' WHERE status = 'Open' AND submission_deadline < NOW()
```

This method is called by `OfficerDashboardServlet.doGet()` on every page load of the officer dashboard. Server-side enforcement ensures:

1. **Browser timezone manipulation cannot bypass deadlines** — the comparison uses MySQL server time, not client time.
2. **No scheduled task (cron) is required** — the check is event-driven, running whenever any officer accesses the dashboard.
3. **Immediate effect** — tenders are closed in real-time as soon as the deadline passes, not on a polling interval.

### 5.4 Automatic "Evaluated" Transition

The **Under Evaluation → Evaluated** transition is triggered automatically by `SubmitScoreServlet` (mapped to `/submit-score`) after each score submission. The logic flows as follows:

1. Evaluator submits a technical score via `SubmitScoreServlet.doPost()`.
2. The servlet calls `EvaluationService.calculatePriceScore()` and `EvaluationService.calculateTimelineScore()` to compute the auto-calculated scores.
3. `EvaluationService.calculateWeightedTotal()` computes the final weighted score.
4. The complete `Evaluation` object is saved via `EvaluationDAO.score()`.
5. The servlet then calls `EvaluationService.checkAndTransitionToEvaluated(tenderId)`.

Inside `checkAndTransitionToEvaluated()`, the service:
- Retrieves all evaluator IDs assigned to the tender via `EvaluatorDAO.getAssignedEvaluatorIds()`.
- For each evaluator, checks if they have scored all bids via `EvaluatorDAO.hasEvaluatorScoredBid()`.
- If **every** assigned evaluator has scored **every** bid, the tender status is updated to "Evaluated" via `TenderDAO.update()`.
- Notifications are sent to the procurement officer that evaluation is complete.

This design ensures that the transition happens **exactly when the last evaluator submits their last score**, with no manual officer intervention required.

[INSERT SCREENSHOT HERE: Screenshot of the Officer Dashboard showing tender status cards with counts for each lifecycle stage]

---

## 6. Evaluation Calculation Walkthrough

### 6.1 Scoring Algorithm

The evaluation system uses a **weighted multi-criteria scoring algorithm** implemented in `EvaluationService`. Each bid receives three component scores that are combined into a single weighted total:

| Component | Weight | Source |
|---|---|---|
| **Price Score** | 40% (`PRICE_WEIGHT = 0.40`) | Auto-calculated from bid amounts |
| **Technical Score** | 35% (`TECHNICAL_WEIGHT = 0.35`) | Evaluator-entered (0–100) |
| **Delivery Timeline Score** | 25% (`TIMELINE_WEIGHT = 0.25`) | Auto-calculated from proposed timeline days |

### 6.2 Price Score Calculation

The price score rewards the **lowest bid**. It is calculated relative to the lowest bid amount in the same tender:

```java
// From EvaluationService
public double calculatePriceScore(double bidAmount, double lowestBidAmount) {
    return (lowestBidAmount / bidAmount) * 100;
}
```

- The lowest bid always receives **100.00** (lowestBidAmount / lowestBidAmount = 1.0 × 100).
- Higher bids receive proportionally lower scores (e.g., a bid of 11500000 when the lowest is 10800000 scores 93.91).

### 6.3 Timeline Score Calculation

The timeline score rewards the **shortest delivery period**. It is calculated relative to the shortest proposed timeline:

```java
// From EvaluationService
public double calculateTimelineScore(int proposedTimelineDays, int shortestTimelineDays) {
    return ((double) shortestTimelineDays / proposedTimelineDays) * 100;
}
```

- The shortest timeline always receives **100.00**.
- Longer timelines receive proportionally lower scores.

### 6.4 Weighted Total Calculation

```java
// From EvaluationService
public double calculateWeightedTotal(double priceScore, double technicalScore, double timelineScore) {
    return (priceScore * PRICE_WEIGHT) + (technicalScore * TECHNICAL_WEIGHT) + (timelineScore * TIMELINE_WEIGHT);
}
```

### 6.5 Worked Example — Tender 3 (Mafeteng-Lesobeng Road Upgrade)

Consider Tender 3 with three bids and four evaluators. The bid data is:

| Bid ID | Supplier | Bid Amount (M) | Timeline (days) |
|---|---|---|---|
| 7 | Basotho Builders | 11,500,000 | 365 |
| 8 | Maloti Electrical | 10,800,000 | 400 |
| 9 | Highland Roads | 11,000,000 | 340 |

**Reference values**: Lowest bid = 10,800,000; Shortest timeline = 340 days.

#### Step 1: Calculate Price Scores

| Bid | Calculation | Price Score |
|---|---|---|
| Bid 7 | (10,800,000 / 11,500,000) × 100 | **93.91** |
| Bid 8 | (10,800,000 / 10,800,000) × 100 | **100.00** |
| Bid 9 | (10,800,000 / 11,000,000) × 100 | **98.18** |

#### Step 2: Calculate Timeline Scores

| Bid | Calculation | Timeline Score |
|---|---|---|
| Bid 7 | (340 / 365) × 100 | **93.15** |
| Bid 8 | (340 / 400) × 100 | **85.00** |
| Bid 9 | (340 / 340) × 100 | **100.00** |

#### Step 3: Evaluator 1 (Palesa Motseare) Submits Technical Scores

| Bid | Technical Score |
|---|---|
| Bid 7 | 75.00 |
| Bid 8 | 70.00 |
| Bid 9 | 85.00 |

#### Step 4: Compute Weighted Totals for Evaluator 1

**Bid 7**: (93.91 × 0.40) + (75.00 × 0.35) + (93.15 × 0.25) = 37.56 + 26.25 + 23.29 = **87.10**

**Bid 8**: (100.00 × 0.40) + (70.00 × 0.35) + (85.00 × 0.25) = 40.00 + 24.50 + 21.25 = **85.75**

**Bid 9**: (98.18 × 0.40) + (85.00 × 0.35) + (100.00 × 0.25) = 39.27 + 29.75 + 25.00 = **94.02**

#### Step 5: Final Ranking (Average Across All 4 Evaluators)

After all four evaluators submit their scores, the system computes the average weighted total per bid:

| Rank | Bid | Supplier | Avg Weighted Total |
|---|---|---|---|
| 1 | Bid 9 | Highland Roads | **94.46** |
| 2 | Bid 7 | Basotho Builders | **87.98** |
| 3 | Bid 8 | Maloti Electrical | **85.60** |

Bid 9 (Highland Roads Construction) ranks first with the highest average weighted total, making it the recommended award recipient.

[INSERT SCREENSHOT HERE: Screenshot of the Evaluation Panel JSP showing ranked bids with computed scores for Tender 3]

---

## 7. Challenges & Solutions

### Challenge: CSS `zoom: 0.8` Causing Viewport Height Miscalculation

#### Problem Description

The ProcureGov UI applies `zoom: 0.8` to the `<body>` element to scale the entire interface to 80% for a compact, professional appearance. However, this CSS property introduced a critical layout bug: the **sidebar did not span the full viewport height** (leaving a gap at the bottom), and the **footer floated above the bottom of the page** on the login screen instead of sticking to the viewport edge.

#### Root Cause Investigation

The root cause was the interaction between `zoom: 0.8` and CSS viewport units (`vh`). When `zoom: 0.8` is applied to the body:

- `100vh` in CSS is computed relative to the **layout viewport**, which remains the full browser height.
- However, the zoom factor scales all CSS pixels by 0.8, meaning `100vh` in CSS pixels only visually fills **80%** of the actual screen.
- Elements using `min-height: 100vh` (the page wrapper) or `height: 100vh` (the sidebar) would only visually reach 80% of the screen, leaving a 20% gap.

The sidebar used `position: fixed; top: 0; height: 100vh;` and the page wrapper used `min-height: 100vh; display: flex; flex-direction: column;`. Both relied on `100vh` which, under `zoom: 0.8`, was insufficient to fill the visual viewport.

#### Technical Solution

A three-pronged approach was implemented:

**1. CSS Variable Compensation** — In `style.css`, a CSS custom property was introduced to compute the inverse zoom factor:

```css
:root {
    --full-vh: 125vh; /* 100vh / 0.8 = 125vh — fills the real visual viewport */
}
```

This variable was applied to `.page-wrapper` (`min-height: var(--full-vh)`) and `.sidebar` (`height: var(--full-vh)`), ensuring they visually span the full screen height.

**2. Inline Style Overrides** — In `header.jsp`, inline styles and a dynamically injected `<style>` block enforced `min-height: 125vh` on the body element:

```css
body { min-height: 125vh !important; }
```

This ensured the body itself was tall enough for the flex layout to push the footer to the bottom.

**3. Dynamic JavaScript Calculation** — In `layout.js`, a JavaScript function dynamically computes the exact CSS pixel height needed to fill the visual viewport:

```javascript
const ZOOM = 0.8;
var cssHeight = Math.ceil(window.innerHeight / ZOOM);
// Apply to sidebar and page-wrapper elements
sidebar.style.height = cssHeight + 'px';
pageWrapper.style.minHeight = cssHeight + 'px';
footer.style.marginTop = 'auto';
footer.style.flexShrink = '0';
```

This uses `window.innerHeight` (which returns the visual viewport height in CSS pixels, already accounting for zoom) divided by the zoom factor to compute the exact CSS pixel value needed. The function runs on `DOMContentLoaded`, `resize`, and `orientationchange` events.

#### Result

The combination of CSS variable compensation, inline style enforcement, and dynamic JavaScript calculation ensures that the sidebar spans from the very top to the very bottom of the viewport, and the footer sticks to the bottom of the page — regardless of the `zoom: 0.8` scaling factor. This approach is robust across different screen sizes and zoom levels.

[INSERT SCREENSHOT HERE: Screenshot of the ProcureGov application showing the sidebar spanning full height and footer fixed at the bottom of the login page]

---

### Challenge: Servlet 3.1 `@MultipartConfig` and `Part` API File Upload Path Resolution

#### Problem Description

ProcureGov allows suppliers to upload supporting documents (PDF/DOCX) when submitting bids via `SubmitBidServlet`, and officers to upload tender notice documents via `CreateTenderServlet`. Both servlets use the Servlet 3.1 `javax.servlet.http.Part` API for file upload handling. During development, file uploads silently failed — uploaded files were not appearing in the configured upload directory (`${user.home}/ProcureGov/uploads`), and `document_path` values stored in the database pointed to files that did not exist on disk.

#### Root Cause Investigation

Two distinct issues were identified:

**1. Missing `@MultipartConfig` Annotation**: The Servlet 3.1 specification requires that any servlet handling `multipart/form-data` requests must be annotated with `@MultipartConfig` (or configured equivalently in `web.xml`). Without this annotation, `request.getPart("document")` returns `null` — it does not throw an exception, making the failure silent and difficult to diagnose. The servlet code had a null-check (`if (filePart != null && filePart.getSize() > 0)`), so the upload was simply skipped without any error message.

**2. `Part.write()` Relative Path Resolution**: The `Part.write(String fileName)` method behaves differently depending on whether the argument is a relative or absolute path. When called with a relative path (e.g., `"bid_1_tender_3_1234.pdf"`), the container writes the file to the temporary directory defined by `java.io.tmpdir` (e.g., `C:\Users\...\AppData\Local\Temp\`), **not** to the intended upload directory. The `@MultipartConfig` annotation's `location` attribute sets the default write location, but it was not being used. The code was constructing the path as:

```java
filePart.write(uploadDir + File.separator + fileName);
```

While this passes an absolute path (which does work), the initial implementation used only `filePart.write(fileName)` — a relative path — causing files to vanish into the system temp directory.

#### Technical Solution

**1. Added `@MultipartConfig` Annotation**: Each servlet that handles file uploads now declares the annotation with an explicit maximum file size:

```java
@MultipartConfig(maxFileSize = 10485760) // 10MB max
public class SubmitBidServlet extends HttpServlet { ... }
```

This was applied to `SubmitBidServlet`, `CreateTenderServlet`, `EditTenderServlet`, and `AwardContractServlet` — all servlets that process file uploads.

**2. Used Absolute Path with `Part.write()`**: The upload directory is resolved from the `web.xml` context parameter `uploadDirectory` (defaulting to `${user.home}/ProcureGov/uploads`), and the directory is created if it does not exist:

```java
private String getUploadDir() {
    String uploadDir = getServletContext().getInitParameter("uploadDirectory");
    if (uploadDir == null || uploadDir.isEmpty()) {
        uploadDir = System.getProperty("user.home") + File.separator
                    + "ProcureGov" + File.separator + "uploads";
    }
    return uploadDir;
}
```

Files are written using the full absolute path to guarantee they land in the correct directory:

```java
String uploadDir = getUploadDir();
File dir = new File(uploadDir);
if (!dir.exists()) dir.mkdirs();

String fileName = "bid_" + supplierId + "_tender_" + tenderId + "_" + System.currentTimeMillis();
// ... determine extension from getSubmittedFileName() ...
filePart.write(uploadDir + File.separator + fileName);
documentPath = fileName;  // store only the filename in DB, resolve at download time
```

**3. File Type and Size Validation**: Before writing, the servlet validates the MIME type (`application/pdf` or `application/vnd.openxmlformats-officedocument.wordprocessingml.document`) and enforces the 10MB limit programmatically, providing clear error messages to the user rather than relying on container-level rejection.

#### Result

With `@MultipartConfig` declared and absolute paths used in `Part.write()`, file uploads now reliably persist to the configured upload directory. The `document_path` and `evidence_document_path` columns in the database store only the filename, and `DownloadServlet` resolves the full path at download time using the same `uploadDirectory` context parameter. This approach is portable across Tomcat, WildFly, and other Servlet 3.1 containers.

[INSERT SCREENSHOT HERE: Screenshot of the bid submission form showing the file upload field and a successful bid submission confirmation message]

---

*End of Project Documentation*
