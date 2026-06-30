
**complete architectural analysis** of ProcureGov.

---

## 1. HIGH-LEVEL ARCHITECTURE: STRICT MVC LAYERED PATTERN

```
┌─────────────────────────────────────────────────────────┐
│                    BROWSER (Client)                     │
├─────────────────────────────────────────────────────────┤
│              AuthRedirectFilter (Security Gate)         │
├──────────────────────────┬──────────────────────────────┤
│     Controller Layer     │    View Layer (JSP/JSTL)     │
│  (20 Servlets)           │  (38 JSP files)              │
│                          │  - /WEB-INF/fragments/       │
│  auth/    officer/       │  - pages/{officer,supplier,  │
│  supplier/ evaluator/    │     evaluator,common,error}/ │
│  common/                 │  - includes/                 │
├──────────────────────────┴──────────────────────────────┤
│              Service Layer (Business Logic)             │
│  EvaluationService   |  EmailService                    │
│  NotificationService |  PdfGenerationService            │
├─────────────────────────────────────────────────────────┤
│              DAO Layer (Data Access)                    │
│  9 interfaces (contracts) + 9 impl (JDBC)               │
│  Uses DBConnectionUtil → JNDI DataSource                │
├─────────────────────────────────────────────────────────┤
│              MySQL Database (11 tables, 3NF)            │
└─────────────────────────────────────────────────────────┘
```

---

## 2. PACKAGE STRUCTURE & RESPONSIBILITIES

### **`com.procuregov.model`** — The M in MVC (8 JavaBeans)
All implement `Serializable` for session/transfer.

| Class | DB Table | Purpose | Key Fields |
|-------|----------|---------|------------|
| `User` | `users` | Central auth entity + flattened role profile data | email, passwordHash, role, displayName, companyName, staffId |
| `Tender` | `tenders` | Tender notice with lifecycle state | referenceNumber, status, deadline, estimatedValue |
| `Bid` | `bids` | Supplier's bid on a tender | amount, technicalCompliance, proposedTimelineDays |
| `BidTechnicalCriterion` | `bid_technical_criteria` | Structured compliance per bid | criterionType (Enum), criterionValue, evidenceDocumentPath |
| `Evaluation` | `evaluations` | Individual evaluator score per bid | technicalScore (manual), priceScore+timelineScore+weightedTotal (auto) |
| `Award` | `awards` | Contract award record | awardedValue, justification, winningBidId |
| `UserToken` | `user_tokens` | Reset/confirmation tokens | token (UUID), tokenType, expiresAt, isExpired() |

**OOP Concept**: Encapsulation — all fields private, exposed via getters/setters. Inheritance not used (composition preferred). `UserToken.isExpired()` demonstrates **business logic within the model**.

### **`com.procuregov.dao`** — Data Access Interfaces (9 interfaces)
Pure contracts defining DB operations. Example patterns:
- `UserDAO`: `findByEmail()`, `registerSupplier()`, `incrementFailedAttempts()`, `countSuppliersByRegNumberPrefix()`
- `TenderDAO`: `create()`, `update()`, `getByStatus()`, `autoCloseExpiredTenders()`, `getByAssignedEvaluator()`
- `BidDAO`: `submit()`, `getByTenderId()`, `countByTenderId()`
- `EvaluationDAO`: `score()`, `getByBidId()`, `getPendingByEvaluatorId()`
- `AwardDAO`: `create()`, `getByTenderId()`, `getByTenderRef()`, `getBySupplierId()`

**Design Pattern**: **DAO Pattern** + **Interface Segregation Principle** — each entity gets its own interface.

### **`com.procuregov.dao.impl`** — JDBC Implementations (9 classes)
Key patterns:
- Every method: `getConnection()` → `PreparedStatement` → execute → catch+log → return
- Use of **try-with-resources** for auto-closing Connection, Statement, ResultSet
- `UserDAOImpl.registerSupplier()` demonstrates **manual transaction management**: `setAutoCommit(false)`, commit on success, rollback on failure
- `TenderDAOImpl.autoCloseExpiredTenders()`: pure SQL `UPDATE ... WHERE status='Open' AND deadline < NOW()` — **server-side deadline enforcement**
- `TenderDAOImpl.mapRow()` — private helper mapping ResultSet → JavaBean

**J2EE Concept**: JNDI DataSource lookup via `DBConnectionUtil.getConnection()` instead of `DriverManager`.

### **`com.procuregov.service`** — Business Logic Layer (4 classes)

| Service | Responsibility |
|---------|---------------|
| `EvaluationService` | Scoring algorithm (price=40%, technical=35%, timeline=25%), weighted total, final score averaging, auto-transition to Evaluated |
| `EmailService` | JavaMail SMTP with STARTTLS, HTML email templates for password reset/verification, plain text for bid/award notifications |
| `NotificationService` | Orchestrates evaluation lifecycle emails: started, reminder, complete — uses `OnlineUserTracker` to avoid emailing online users |
| `PdfGenerationService` | iText PDF generation for award confirmation documents |

**Key OOP**: **Separation of Concerns** — servlets don't compute scores, services don't handle HTTP.

### **`com.procuregov.controller`** — The C in MVC (20 Servlets)

| Package | Servlets | URLs |
|---------|----------|------|
| `auth` | Login, Logout, Register, ForgotPassword, ResetPassword, ConfirmAccount, ResendConfirmation, EmailVerification | /login, /logout, /register, /forgot-password, /reset-password, /confirm-account, /resend-confirmation, /email-verify |
| `officer` | OfficerDashboard, CreateTender, EditTender, TenderStatus, ManageTenders, AwardContract, BidReport | /officer-dashboard, /create-tender, /edit-tender, /tender-status, /manage-tenders, /award-contract, /bid-report |
| `supplier` | SupplierDashboard, OpenTenders, TenderDetail, SubmitBid | /supplier-dashboard, /open-tenders, /tender-detail, /submit-bid |
| `evaluator` | EvaluatorDashboard, EvaluationList, EvaluationPanel, SubmitScore | /evaluator-dashboard, /evaluations, /evaluation-panel, /submit-score |
| `common` | Download, AwardNotice, Profile | /download, /award-notice, /profile |

**Key patterns in every Servlet**:
- `doGet()` → display page
- `doPost()` → process form
- Role check at top: `SessionUtil.isLoggedIn() && SessionUtil.hasRole()`
- All URL mappings in `web.xml` (not annotations, except `@MultipartConfig`)

### **`com.procuregov.filter`** — Security

**`AuthRedirectFilter`** implements `javax.servlet.Filter`:
- `doFilter()` checks `SessionUtil.isLoggedIn()` for 4 URL patterns: `/pages/officer/*`, `/pages/supplier/*`, `/pages/evaluator/*`, `/pages/profile/*`
- If not logged in → forward to login.jsp with `"Access Denied"` message
- If wrong role → redirect to correct dashboard
- **Exam requirement**: *"Unauthorised access attempts must redirect to login with 'Access Denied' message — never a raw error page"* — fully satisfied

### **`com.procuregov.listener`** — Lifecycle

**`DBInitListener`** implements `ServletContextListener`:
- `contextInitialized()`: creates upload directory on server startup
- `contextDestroyed()`: logs shutdown

**`OnlineUserTracker`** implements `HttpSessionListener`:
- Tracks active user IDs in a `ConcurrentHashMap` (thread-safe)
- Used by `NotificationService` to decide: should we email this evaluator (offline) or not (online)?

### **`com.procuregov.util`** — Utilities

| Class | Purpose |
|-------|---------|
| `DBConnectionUtil` | JNDI lookup → `DataSource.getConnection()` |
| `PasswordUtil` | SHA-256: `hashPassword()`, `verifyPassword()` |
| `SessionUtil` | Session CRUD, login tracking, role checking, dashboard URL mapping |
| `TokenUtil` | UUID token generation for password resets |
| `MailConfigUtil` | JNDI mail session lookup (unused by EmailService which uses context params) |
| `FileUtil` | Cross-platform upload directory management |
| `OnlineUserTracker` | ConcurrentHashMap-based online user tracking |
| `ConfigUtil` | Property file reader (fallback only — not primary config path) |

---

## 3. DATA FLOW — COMPLETE REQUEST LIFECYCLE

### Example: Supplier Submits a Bid (SubmitBidServlet)

```
1. Browser POST /submit-bid (multipart/form-data)
   ↓
2. AuthRedirectFilter (JSP pages not involved in servlet URL)
   ↓
3. SubmitBidServlet.doPost()
   ├── SessionUtil.isLoggedIn() + role check
   ├── Parse tenderId from request
   ├── TenderDAO.getById(tenderId) → Model → validate Open + deadline
   ├── hasExistingBid() → BidDAO.getBySupplierId() → check uniqueness
   ├── Parse form fields (amount, compliance, timeline)
   ├── Part API → filePart.write(uploadDir + fileName) [@MultipartConfig required]
   ├── Create Bid JavaBean (Model)
   ├── BidDAO.submit(bid) → INSERT INTO bids (PreparedStatement)
   ├── parseCriteria() → BidTechnicalCriterionDAO.insertBatch()
   ├── EmailService.notifyBidReceived() [JavaMail]
   └── POST-Redirect-GET → /tender-detail?id=X&success=true
   ↓
4. Browser GET /tender-detail?id=X
   ↓
5. TenderDetailServlet.doGet()
   ├── TenderDAO.getById()
   ├── BidDAO.getByTenderId() (filtered: only own bid for SUPPLIER)
   └── forward to /pages/supplier/tender-detail.jsp
```

### Data Flow Diagram:
```
Browser ──POST──→ Servlet ──→ DAO ──PreparedStatement──→ MySQL
                           ←── JavaBean ──────────────────
            Servlet ──setAttribute()──→ JSP ──JSTL/EL──→ HTML
```

---

## 4. TENDER LIFECYCLE STATE MACHINE

```
Draft ──publish──→ Open ──autoClose──→ Closed ──startEval──→ Under Evaluation ──auto──→ Evaluated ──award──→ Awarded
  ↑                    ↑                    ↑                      ↑                       ↑             ↑
Officer             Officer              System                Officer                 System       Officer
creates              clicks               checks               clicks                  all scores  clicks
                      Publish              deadline              Start Eval              submitted    Award
```

**Enforcement points**:
1. **`TenderStatusServlet.doPost()`**: switch-case prevents invalid transitions
2. **`TenderDAOImpl.autoCloseExpiredTenders()`**: SQL update on every dashboard load
3. **`EvaluationService.checkAndTransitionToEvaluated()`**: after each score submission
4. **Database ENUM**: `status ENUM('Draft','Open','Closed','Under Evaluation','Evaluated','Awarded')` prevents invalid DB states

---

## 5. EVALUATION SCORING ALGORITHM

All in `EvaluationService.java`:

```
Price Score     = (Lowest Bid / This Bid) × 100          × 0.40
Technical Score = Evaluator enters 0-100                  × 0.35
Timeline Score  = (Shortest Timeline / This Timeline) × 100 × 0.25
─────────────────────────────────────────────────────────────────
Weighted Total  = Price×0.40 + Technical×0.35 + Timeline×0.25
```

**Multi-evaluator averaging**: `getFinalScore(bidId)` sums all `weightedTotal` values and divides by evaluator count.

---

## 6. DESIGN PATTERNS USED

| Pattern | Where | How |
|---------|-------|-----|
| **MVC** | Entire app | Servlet=Controller, JSP=View, JavaBean+DAO=Model |
| **DAO** | `dao` + `dao.impl` | Interface contract + JDBC implementation per entity |
| **Front Controller** | `AuthRedirectFilter` | Single gate for all protected pages |
| **Service Layer** | `service/` | Encapsulates business logic from servlets |
| **Value Object / DTO** | `model/` | Serializable beans carry data across layers |
| **Strategy** | `PasswordUtil` | SHA-256 strategy can be swapped |
| **Singleton** | `DBConnectionUtil` | Static methods, no instance state |
| **Template Method** | DAO pattern | Each DAO follows same getConn→prepare→exec→map pattern |
| **Factory** | `new XxxDAOImpl()` | Instantiated in servlet init() |

---

## 7. SECURITY ARCHITECTURE

| Concern | Implementation |
|---------|---------------|
| **Password Storage** | SHA-256 hashing (`PasswordUtil`) — never plain text |
| **Authentication** | `LoginServlet.doPost()` — compares hash |
| **Login Lockout** | Session-based: `SessionUtil.recordFailedLogin()` → 3 attempts → locked |
| **DB-level Lockout** | `users.account_locked` column + `users.failed_login_attempts` |
| **Authorization** | `AuthRedirectFilter` + `SessionUtil.hasRole()` in every servlet |
| **Session Management** | `HttpSession` with `invalidate()` on logout |
| **Bid Privacy** | `BidDAO.getBySupplierId()` — suppliers only see own bids |
| **File Download** | `DownloadServlet` — path not exposed to browser |
| **SQL Injection** | All queries use `PreparedStatement` — no concatenation |
| **Error Handling** | All DAO methods catch+log; custom error pages in web.xml |

---

## 8. EXAM REQUIREMENTS MAPPING

| Module | Marks | Files |
|--------|-------|-------|
| **M1: Auth (10)** | /10 | `LoginServlet`, `RegisterServlet`, `LogoutServlet`, `PasswordUtil`, `SessionUtil`, `AuthRedirectFilter` |
| **M2: Tender Mgmt (12)** | /12 | `CreateTenderServlet`, `EditTenderServlet`, `TenderStatusServlet`, `AwardContractServlet`, `BidReportServlet`, `ManageTendersServlet`, `OfficerDashboardServlet` |
| **M3: Bid Submission (11)** | /11 | `SubmitBidServlet`, `OpenTendersServlet`, `TenderDetailServlet`, `SupplierDashboardServlet` |
| **M4: Evaluation (13)** | /13 | `EvaluatorDashboardServlet`, `EvaluationPanelServlet`, `SubmitScoreServlet`, `EvaluationService`, `NotificationService` |
| **M5: Persistence (8)** | /8 | All `dao/` + `dao/impl/`, `DBConnectionUtil`, `schema.sql` |
| **M6: Email (4)** | /4 | `EmailService`, `MailConfigUtil` |

---

## 9. POTENTIAL ARCHITECTURAL ISSUES

1. **No Inversion of Control / DI**: Servlets directly instantiate `new XxxDAOImpl()`. Hard to unit test. A simple `ServiceFactory` or even constructor injection via `init()` would help. Since this is a J2EE exam project without Spring, manual DI in `init()` is acceptable.

2. **Service layer bypass**: Some servlets call DAOs directly (e.g., `TenderStatusServlet` calls `evaluatorDAO.assignAllEvaluatorsToTender()`). Ideally all business logic goes through services. However, for an exam project this is pragmatic.

3. **`OnlineUserTracker` session listener timing**: The `sessionCreated` listener fires before `userId` is set. The workaround (`userLoggedIn()` in `SessionUtil`) works but is fragile. This is a known Servlet spec limitation.

4. **`ConfigUtil` is dead code**: It reads `config.properties` but the project explicitly states *"No properties files used"*. All config comes from web.xml context params or context.xml JNDI. This file is not wired to anything.

5. **Hardcoded email password in web.xml**: `smtpPassword` is in plaintext in web.xml. For a real deployment this should be in Tomcat JNDI with `{cipher}` encryption or environment variables.

6. **No pagination**: `getAll()` on tenders/bids returns everything — fine for exam scale but would fail at production scale.

7. **`PasswordUtil` uses `SHA-256` directly** rather than `PBKDF2`/`bcrypt`/`argon2`. SHA-256 alone is fast and vulnerable to brute force without salting. The exam spec requires SHA-256 though, so this is compliant.

8. **JSPs use some inline styles** in `<style>` blocks (officer dashboard). Only JSTL/EL should be used — no scriptlets is correct, but `<style>` isn't technically scriptlets.

---

## 10. DATABASE DESIGN (3NF)

11 tables, all InnoDB with `utf8mb4`:

```
users (1) ──→ suppliers (1)       [1:1 via user_id FK]
users (1) ──→ officers (1)        [1:1 via user_id FK]
users (1) ──→ evaluators (1)      [1:1 via user_id FK]
users (1) ──→ user_tokens (many)  [1:N via user_id FK]
users (1) ──→ tenders (many)      [1:N via created_by FK]
tenders (1) ──→ tender_evaluators (many) ←── evaluators (1) [M:N junction]
tenders (1) ──→ bids (many)       [1:N via tender_id FK]
bids (1) ──→ bid_technical_criteria (many) [1:N via bid_id FK]
bids (1) ──→ evaluations (many)   [1:N via bid_id FK]
evaluators (1) ──→ evaluations (many) [1:N via evaluator_id FK]
tenders (1) ──→ awards (1)        [1:1 via tender_id FK]
bids (1) ──→ awards (1)           [1:1 via winning_bid_id FK]
```

**Key constraints**: `UNIQUE(supplier_id, tender_id)` for one-bid-per-tender, `UNIQUE(bid_id, evaluator_id)` for one-score-per-evaluator-per-bid.

---

## 11. XML FILES USAGE

| File | Location | Purpose |
|------|----------|---------|
| `web.xml` | `/web/WEB-INF/` | Servlet mappings, filters, listeners, context params (SMTP, upload dir), error pages, resource refs |
| `context.xml` | `/web/META-INF/` | JNDI DataSource (MySQL connection pool) — driver, URL, credentials, pool tuning |
| `build.xml` | root | NetBeans Ant build configuration |
| `project.xml` | `/nbproject/` | NetBeans project metadata |
| `sun-resources.xml` | `/setup/` | NetBeans IDE datasource reference for design-time |
| `private.xml` | `/nbproject/private/` | Per-developer IDE settings (not committed) |

---

## 12. SUMMARY: KEY J2EE CONCEPTS DEMONSTRATED

1. **Servlets** as HTTP controllers (doGet/doPost lifecycle)
2. **JSP** as view with JSTL (no scriptlets)
3. **JavaBeans** as data carriers (Serializable, getters/setters)
4. **DAO Pattern** with interface/impl separation
5. **JNDI DataSource** for connection pooling (no DriverManager)
6. **JDBC PreparedStatement** for parameterized queries
7. **Filters** for cross-cutting security concerns
8. **Listeners** for app lifecycle + session tracking
9. **File Upload** via Servlet 3.1 `Part` API with @MultipartConfig
10. **JavaMail** with SMTP STARTTLS for email
11. **SHA-256** password hashing
12. **Session management** with login lockout
13. **Custom error pages** (400/401/403/404/408/500)
14. **PDF generation** via iText
15. **POST-Redirect-GET** pattern for form submission
16. **3NF database design** with foreign keys and indexes

---

