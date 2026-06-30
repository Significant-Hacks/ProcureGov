

# C7-ADJ-11 End Assessment QP-ASR-001 Exam Question Paper

## General Instructions

1. Ensure to submission of both the artefact and documentation on or before 8th May 2026 23:59
2. Should there be a submission of the artefacts and no submission of the project documentation the entire exam will be considered a non-submission and will be treated as such.
3. The database name should be the student's name and student number e.g JohnDoe12345678.
4. The Project Name should follow the same naming structure.
5. Include a readme file in your project to explain how to get started and running your project.
6. The report should have a cover page with your particulars and contents should be in an orderly manner.
7. Similarity Index should not exceed \(20\%\) .
8. Your final project submission consists of five required items.
    a. Four items must be submitted on Blackboard as a single ZIP archive (Source Code ZIP, Compiled WAR File, MySQL database export e.g schema.sql, Read.me file).
    b. The Project Documentation must be submitted separately on Turnitin.
9. A date and time for Project Demo Presentation will be communicated at a later stage (consult your instructor).
10. Ensure academic honesty, any kind of misconduct will result in disciplinary action as per the university's academic integrity policy, which may include a reduction in marks, failure of the project, or further penalties as deemed appropriate
11. Usage of AI tools in this assessment is prohibited.
    a. You may not submit AI-generated Servlets, JSPs, DAOs, or any other component as your own original work.
    b. You may not use AI to write your project documentation. The report must be your own writing.
    c. You may not submit any work — code or written — that you cannot fully explain and defend during the demo and viva.

## 1. Scenario & Background

The Ministry of Public Works of the Kingdom of Lesotho currently manages its government tender processes through a combination of paper- based submissions, physical notice boards, and email correspondence. This approach results in delayed publication of tender notices, misplaced bid documents, lack of transparency in the evaluation process, and difficulty tracking the lifecycle of each tender from publication through to award.

You have been engaged as a junior Java web developer by the Ministry's Directorate of ICT to design and build ProcureGov — a web- based Tender Management System that digitises the full tender lifecycle. The system must allow the Ministry to publish tenders, receive sealed electronic bids from registered suppliers, conduct a structured evaluation using a weighted scoring model, and formally award contracts — all through a secure, role- controlled web portal built using J2EE technologies.

The scenario is intentionally specific to a real government context. Your system must reflect that context in its language, workflows, and business rules. No two students should produce a system that looks or behaves identically — your design decisions, database structure, UI layout, naming conventions, and business logic are entirely your own responsibility.

### System Roles

ProcureGov must support three distinct user roles, each with its own access rights and responsibilities:

| Role | Who They Are | What They Can Do |
| :--- | :--- | :--- |
| **Supplier** | A registered company or individual submitting bids for published tenders. | Register an account, browse open tenders, download tender notices, submit a single bid per tender (with one supporting document), track their own bid status. |
| **Procurement Officer** | A Ministry official who manages the tender process from creation to award. Also participates in bid evaluation as part of the committee. | Create and publish tenders, set closing dates, manage tender lifecycle (Draft → Open → Closed → Evaluated → Awarded), score bids, award contracts, generate a tender result notice. |
| **Evaluation Committee Member** | A Ministry official appointed to score bids. Cannot create tenders or view bids before the tender is formally closed. | Log in, view tenders in Closed status, score each eligible bid using the three weighted criteria, view consolidated scores after all members have scored. |

## 2. FUNCTIONAL REQUIREMENTS

The system is divided into five mandatory modules and one bonus module. Each module specifies the J2EE technology that must be demonstrably applied. **Use of JSP scriptlets ( `<% %>` ) anywhere in the application will attract a deduction for the relevant module.**

### Tender Lifecycle
Every tender in ProcureGov must pass through the following statuses in order. Status transitions must be enforced at the Servlet layer - a tender cannot skip a stage or move backwards.

| Status | Who Triggers It | What It Means |
| :--- | :--- | :--- |
| **Draft** | Procurement Officer | Tender created but not yet visible to suppliers. Officer can still edit all fields. |
| **Open** | Procurement Officer | Tender published and visible to all suppliers. Bid submission is active. Closing date/time is enforced. |
| **Closed** | System (automatic) | Closing date/time has passed. Bid submission link is deactivated server-side. Evaluation Committee Members can now access bids. |
| **Under Evaluation** | Procurement Officer | Formally signals that scoring has begun. Procurement Officer and Committee Members are scoring bids. |
| **Evaluated** | System | All appointed evaluators have submitted scores. System calculates weighted totals and ranks bids. |
| **Awarded** | Procurement Officer | Officer selects the winning supplier based on evaluation scores. Award notice is generated and visible to all bidding suppliers. |

### Module 1: User Registration & Authentication [10 marks]

**Requirements**
- A Supplier Registration page collecting: company/individual name, registration number (Auto generated), email address, physical address, contact number, and password. All fields are mandatory.
- Ministry staff accounts (Procurement Officer and Evaluation Committee Member) are created directly in the database via the seed script - there is no self- registration for staff.
- A single Login page for all three roles. On successful login the system must detect the role from the database and redirect each role to their respective dashboard.
- Failed login attempts must be counted in the session. After three consecutive failures the account must be temporarily locked and a clear message displayed. The lock persists for the session duration.
- Passwords must not be stored in plain text. Apply SHA- 256 hashing before persisting to the database.
- A logout function must explicitly invalidate the HttpSession and redirect to the login page.
- All protected pages must verify session validity and role. Unauthorised access attempts must redirect to the login page with an 'Access Denied' message — never a raw error page.

**Technology Constraints**
- All authentication logic must reside in a Servlet. No Java code in JSPs.
- Role and user identity must be stored in a session.
- Login form must use POST. The session check on protected pages must be in a reusable method or utility class called from each Servlet.

### Module 2: Tender Management (Procurement Officer) [12 marks]

**Requirements**
- A Create Tender form with fields: tender title, reference number (system-generated in format MPW-YYYY-NNNN), category (Construction, Roads, Electrical, Plumbing, General Services), description, estimated value (Maloti), submission deadline date and time, and a tender notice document (PDF upload, max 5MB).
- The system must generate the reference number automatically — the officer does not type it. Generation must be server-side and guarantee uniqueness.
- Officers can edit tenders only while in Draft status. Once Published (Open), the tender is locked for editing.
- A Tender List page showing all tenders with their current status, filterable by status and category using JSTL.
- Manual status transitions for all the state changes of the tender except the 'closed' status as this should be taken care of by the application automatically.
- When awarding a contract, the officer must select the winning supplier from a ranked list generated by the evaluation scores and enter a brief award justification note.
- An Award Notice page must be generated showing: tender reference, title, winning supplier name, awarded value (as entered by officer), award date, and justification — visible to all suppliers who bid on that tender.

**Technology Constraints**
- Tender data must be encapsulated in a JavaBean before being passed to the DAO.
- File upload must be handled using the Part API in the Servlet — not a third-party library.
- The tender notice PDF must be stored on the server filesystem and served back through a dedicated download Servlet — not by exposing the file path directly.
- All JSPs in this module must use JSTL core tags only — no scriptlets.

### Module 3: Supplier Bid Submission [11 marks]

**Requirements**
- A Supplier Dashboard showing: all currently Open tenders (visible to all logged-in suppliers), and a separate section showing the supplier's own submitted bids with current status.
- A Tender Detail page showing full tender information and, if the tender is Open and the supplier has not yet bid, a Submit Bid button.
- The bid submission form must collect: bid amount (Maloti), technical compliance statement (text, max 600 characters), proposed delivery timeline in days (integer), and one supporting document (PDF or DOCX, max 10MB).
- The system must enforce the closing date/time server- side. If the current server time is past the tender closing date/time, the submission endpoint must reject the request and return an appropriate message - regardless of what the browser shows.
- A supplier may submit only one bid per tender. If they attempt a second submission the system must detect this and block it with a clear message.
- After a tender is Awarded, suppliers who submitted bids on that tender must be able to view the Award Notice from their dashboard.
- Suppliers must never be able to view bids submitted by other suppliers under any circumstances.

**Technology Constraints**
- Closing date enforcement must use Java's LocalDateTime or java.util.Date comparison in the Servlet — the JSP must never make this determination.
- Bid data must be encapsulated in a JavaBean.
- The supplier dashboard must use JSTL for conditional display logic. Use formatting tags for all dates and time display throughout this module.

### Module 4: Bid Evaluation [13 marks]

**Requirements**
- Once a tender moves to Under Evaluation status, Procurement Officers and Evaluation Committee Members can access the evaluation panel for that tender.
- The evaluation panel lists all bids submitted for the tender. For each bid an evaluator must score three criteria:
    - **Price Score** - calculated automatically by the system as: (Lowest Bid Amount / This Bid Amount) × 100, then weighted at **40%** . The evaluator does not enter this - the system computes it.
    - **Technical Compliance Score** - the evaluator enters a score from 0 to 100. This is weighted at **35%** .
    - **Delivery Timeline Score** - calculated automatically as: (Shortest Proposed Timeline / This Bid's Timeline) × 100, weighted at **25%** . The evaluator does not enter this - the system computes it.
- The **Weighted Total Score** for a bid = (Price Score × 0.40) + (Technical Compliance Score × 0.35) + (Delivery Timeline Score × 0.25). This must be computed and stored when an evaluator submits their scores.
- If multiple evaluators score the same bid, the system must average the Weighted Total Scores across all evaluators to produce a **Final Score** for that bid.
- An evaluator cannot see another evaluator's individual scores until they have submitted their own scores for that bid.
- Once all evaluators have scored all bids, the tender status must automatically transition to **Evaluated** and a ranked leaderboard of bids (sorted by Final Score descending) must be displayed to the Procurement Officer.
- The Procurement Officer uses this ranked list to select the winning bid and proceed to award.

**Technology Constraints**
- All score calculations must be performed in a dedicated service class - not in the Servlet or JSP.
- Score data must be stored per evaluator per bid in the database. No in- memory- only calculation.
- The evaluation results page must display scores formatted to two decimal places using either EL formatting or JSTL fmt: tags.
- The automatic Evaluated status transition must be triggered server- side - a database check after each score submission, not a scheduled job.

### Module 5: Data Persistence Layer [8 marks]

**Requirements**
- All data must be persisted to a relational database (MySQL).
- You must design a database of related tables in 3rd Normal Form.
- All database operations must go through dedicated DAO classes. No JDBC code in Servlets or JSPs.
- A Tomcat JNDI DataSource or Apache DBCP connection pool must be used. No DriverManager.getConnection() calls in application logic.
- Your submission must include a complete schema.sql file that: drops existing tables if they exist, creates all tables with correct data types and constraints, and inserts seed data comprising at least: 2 Procurement Officer accounts, 2 Evaluation Committee Member accounts, 3 Supplier accounts, 2 published tenders with at least 3 bids each.
- SQLExceptions must be caught and logged in every DAO method. Stack traces must never be displayed to the user. A generic error page must be shown instead, configured in web.xml.

**Technology Constraints**
- Each DAO must implement a corresponding interface (e.g., TenderDAO, BidDAO). This enforces separation of concerns and will be verified during the via.
- Uploaded document file paths (not the binary content) are stored in the database. Binary files are stored on the server filesystem.

### Module 6: Supplier Email Notification [4 marks]

When a tender is awarded, the system must send an email notification to all suppliers who bid on that tender.
- Implementation must use JavaMail API to send the email.
- The notification must include the tender reference, the outcome (Won/Not Won), and a link to the award notice page.

## 3. NON-FUNCTIONAL & DESIGN REQUIREMENTS

- The application must be deployable as a single WAR file on Apache Tomcat 9.x or later.
- The MVC pattern must be applied throughout: Servlets act as Controllers, JSPs act as Views, JavaBeans and DAO classes form the Model. Business logic in JSPs will attract deductions.
- All Servlet URL mappings must be declared in web.xml. Annotation-based mapping (@WebServlet) may additionally be used but cannot replace web.xml declarations.
- Custom error pages for HTTP 4XX and HTTP 5XX must be configured in web.xml and must display a Ministry-branded page — not the default Tomcat error page.
- All JSPs must use the JSTL taglib directives at the top of the file.
- All JAR files must be included in WEB-INF/lib.
- The UI must be consistent across all pages. A shared CSS stylesheet must be linked from all pages.
- All Servlet classes and DAO classes must include Javadoc comments at the class level and on every public method.
- Uploaded files must be saved to a configurable directory outside the WAR (e.g., a path set in context.xml or web.xml as an init parameter). Hard-coded absolute paths will attract deductions.
- The application must include a navigation bar visible on all authenticated pages, showing the logged-in user's name, role, and a Logout link.

## 4. PROJECT DOCUMENTATION REQUIREMENTS [20 marks]

The project documentation is a professional technical report written in your own words. It must describe and justify the system you built — not an idealised version of it. Markers will cross-reference the report against your submitted code and your demo. Inconsistencies will be penalised.

| Required Section | Marks | Marking Criteria |
| :--- | :---: | :--- |
| **1. Cover Page** | - | Student name, number, project title, submission date. Not marked but mandatory. |
| **2. System Overview** *(1 page)* | 2 | Describes ProcureGov in the student's own words. What it does, who uses it, what problem it solves within the Ministry of Public Works context. |
| **3. Architecture Diagram & Explanation** *(1–2 pages)* | 4 | A labelled MVC diagram showing the layers of the application. A paragraph beneath the diagram must explain each component's role. Diagram must match submitted code. |
| **4. Database Design — ERD & Justification** *(1 page)* | 4 | Correct entity-relationship diagram reflecting the actual schema.sql. Primary keys, foreign keys, and data types explained. Any indexing decisions justified. |
| **5. Tender Lifecycle Implementation** *(1 page)* | 3 | Explains how the student implemented the status transitions, which Servlet handles each, how the closing date enforcement works, and how the Evaluated auto-transition is triggered. |
| **6. Evaluation Calculation Walkthrough** *(1 page)* | 4 | Step-by-step worked example showing how the weighted score is calculated for a sample bid. Must include the formula, sample numbers, and a trace through the actual code that computes it. |
| **7. Challenges & Solutions** *(1 page)* | 3 | Describes a specific technical problem encountered (not a generic statement). Names the actual error or unexpected behaviour, describes how it was investigated, and explains the solution applied. |

## 5. SUBMISSION DELIVERABLES

All the items below except the project documentation must be submitted as a single ZIP archive on Blackboard by the stated deadline. The Project Documentation should be submitted on Turnitin on or before the stated deadline.

| # | Item | Description |
| :--- | :--- | :--- |
| 1 | **Source Code ZIP** | Complete IDE project folder. Must include src/, WebContent/ or webapp/, WEB-INF/ (including web.xml and lib/), and any configuration files. |
| 2 | **Compiled WAR File** | Deployable on stock Tomcat 9+ with only schema.sql run first. Must start without errors in the Tomcat logs. |
| 3 | **schema.sql** | Complete SQL script: DROP TABLE IF EXISTS, CREATE TABLE, all constraints, INSERT seed data. No partial scripts. |
| 4 | **Readme File** | A complete readme file containing instruction on how to run the application and what are the credentials of the predefined user |
| 5 | **Project Documentation** | Required sections specified in Part 4. |

## 6. MARKING SCHEME (Total: 100 Marks)

### 6.1. Technical Implementation [70 marks]

| Module / Component | What the Marker Will Verify | Mark |
| :--- | :--- | :---: |
| **Module 1** — Authentication & Session Management | Session lifecycle, role detection, lockout logic, SHA-256 hashing, logout invalidation, role guard on protected pages | /10 |
| **Module 2** — Tender Management (Officer) | Reference generation (MPW-YYYY-NNNN), status transition enforcement, file upload via Part API, download Servlet, Draft lock, award notice generation | /12 |
| **Module 3** — Supplier Bid Submission | Closing date server-side check, one-bid-per-tender enforcement, POST-Redirect-GET on submission, JavaBean usage, ownership enforcement | /11 |
| **Module 4** — Bid Evaluation & Scoring | Automated Price and Timeline score calculation, manual Technical score entry, weighted total formula, multi-evaluator averaging, auto Evaluated transition, ranked display | /13 |
| **Module 5** — Data Persistence Layer | DAO interface + implementation pattern, connection pool, 5+ tables, schema.sql completeness, exception handling and logging, no JDBC in Servlets/JSPs | /8 |
| **Module 6** — Supplier Email Notification | Use of JavaMail API to send the email, notification includes the tender reference, the outcome (Won/Not Won), and a link to the award notice page. | /4 |
| **MVC Adherence & web.xml Configuration** | No business logic in JSPs, Servlets as controllers, custom 404/500 pages, JNDI or DBCP configured, URL mappings in web.xml | /4 |
| **Code Quality, Javadoc & Comments** | Class and method Javadoc present, meaningful identifiers, consistent formatting, no dead code or debug print | /4 |
| **Presentation / Demo Skills** | Clarity of delivery, ability to navigate the live system confidently, all modules demonstrated, professional conduct | /4 |
| **SUBTOTAL — Technical + Demo** | | **/70** |

### 6.2 Project Documentation [20 marks]

Refer to Part 4 for section- level breakdown. Overall documentation is also assessed on professional language and presentation, consistency between report and submitted code, and absence of plagiarized or AI- generated prose.

### 6.3 Viva Voce [10 marks]

The viva is conducted individually after each presentation is completed. It is a subject knowledge assessment. Students will be asked questions regarding their project and subject matter knowledge.

| Band | Descriptor | Mark Range |
| :--- | :--- | :---: |
| **Distinction** | Answers questions fluently and accurately across all topic areas. Demonstrates clear conceptual understanding, not just code recall. Handles follow-up questions confidently without contradiction. | 9 – 10 |
| **Merit** | Solid understanding of most topics. Minor gaps in one area but can recover when guided. Answers are consistent with submitted work. | 7 – 8 |
| **Pass** | Can answer basic questions about their system but struggles with the ‘why’ behind design decisions. Some vagueness but no major contradictions. | 5 – 6 |
| **Marginal Fail** | Frequent inability to explain code they submitted. Answers suggest familiarity with the domain but not their own implementation. Concern about authorship. | 3 – 4 |
| **Fail** | Cannot explain the submitted work at a basic level. Answers directly contradict the code. Academic integrity concern to be escalated immediately. | 0 – 2 |