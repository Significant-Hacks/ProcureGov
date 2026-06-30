# ProcureGov - Tender Management System

[![GitHub Repo](https://img.shields.io/badge/GitHub-ProcureGov-blue?logo=github)](https://github.com/Significant-Hacks/ProcureGov)

## Overview
ProcureGov is a web-based Tender Management System for the Ministry of Public Works of the Kingdom of Lesotho. It supports three user roles: Procurement Officers, Suppliers, and Evaluation Committee Members.

## Technology Stack
- **Java EE**: Servlets, JSP, JSTL
- **Database**: MySQL (via XAMPP)
- **Server**: Apache Tomcat (via XAMPP or NetBeans bundled)
- **Build**: NetBeans Ant project
- **Email**: JavaMail API with SMTP (Gmail)
- **Security**: SHA-256 password hashing, session-based auth, 3-attempt lockout, email verification, code-based password reset

## Quick Start (from GitHub)

### Clone & Deploy in Minutes
1. **Clone the repo**:
   ```
   git clone https://github.com/Significant-Hacks/ProcureGov.git
   cd ProcureGov
   ```
2. **Start XAMPP** — enable MySQL and Tomcat
3. **Import the database** — run `sql/schema.sql` in phpMyAdmin or via CLI: `mysql -u root < sql/schema.sql`
4. **Deploy the pre-built WAR** — copy `dist/DavidMohale2333908.war` into `tomcat/webapps/` (Tomcat auto-deploys it)
5. **Open in browser**: `http://localhost:8080/DavidMohale2333908/`
6. **Login** using any credential from the table below (all passwords: `psw123`)

No IDE required. The WAR inside `dist/` is ready to deploy.

---

## Setup Instructions (XAMPP)

### 1. Start XAMPP Services
1. Open XAMPP Control Panel
2. Start **MySQL** and **Apache** (Tomcat)

### 2. Create the Database
1. Open phpMyAdmin: `http://localhost/phpmyadmin`
2. Go to the **Import** tab
3. Select `sql/schema.sql` from this project
4. Click **Go** to execute

Alternatively, via MySQL command line:
```
mysql -u root < sql/schema.sql
```

### 3. Configure Database Connection
Edit `web/META-INF/context.xml` and update:
- `username="root"` — your MySQL username (default XAMPP: root, no password)
- `password=""` — your MySQL password (default XAMPP: empty)
- `url="jdbc:mysql://localhost:3306/DavidMohale2333908?..."` — confirm port 3306

### 4. Configure NetBeans (One-time Setup)
1. **Tools** → **Servers** → **Add Server** → Apache Tomcat (use XAMPP's Tomcat or NetBeans bundled)
2. Right-click project → **Properties** → **Run** → select Tomcat server
3. All `javax.servlet` IDE errors will resolve once Tomcat is linked

**Note**: No absolute paths are stored in project files. NetBeans auto-configures server paths when you assign a server. Your lecturer will do the same on their machine.

### 5. Environment Configuration
Email settings are configured in `web/WEB-INF/web.xml` as context parameters:
- `smtpHost` — SMTP server (default: smtp.gmail.com)
- `smtpPort` — SMTP port (default: 587)
- `smtpUser` — sender email address
- `smtpPassword` — email app password

**Important**: No external properties files are used. All configuration is via JNDI (`context.xml`) and context parameters (`web.xml`).

### 6. Deploy & Run

**Option A — NetBeans:**
1. Right-click project → **Clean and Build**
2. Right-click project → **Run**
3. Open browser: `http://localhost:8080/DavidMohale2333908/`

**Option B — Direct WAR Deploy (No IDE):**
1. Copy `dist/DavidMohale2333908.war` into `tomcat/webapps/`
2. Tomcat will auto-extract and deploy
3. Open browser: `http://localhost:8080/DavidMohale2333908/`

---

## User Credentials (All passwords: `psw123`)

### Procurement Officers
| Email | Password | Name | Department | Staff ID |
|-------|----------|------|------------|----------|
| officer1@gmail10p.com | psw123 | Thabo Mohapi | Infrastructure Procurement | MPW-001 |
| officer2@gmail10p.com | psw123 | Maseaboli Khetsi | Roads & Transport Procurement | MPW-002 |

### Evaluation Committee Members
| Email | Password | Name | Department | Staff ID |
|-------|----------|------|------------|----------|
| evaluator1@gmail10p.com | psw123 | Palesa Motseare | Technical Evaluation Unit | EVAL-001 |
| evaluator2@gmail10p.com | psw123 | Lehlohonolo Tsehlana | Compliance Evaluation Unit | EVAL-002 |

### Suppliers
| Email | Password | Company Name | Reg. Number | Contact |
|-------|----------|--------------|-------------|---------|
| supplier1@gmail10p.com | psw123 | Basotho Builders Pty Ltd | SUP-2026-0001 | +266 2231 0001 |
| supplier2@gmail10p.com | psw123 | Maloti Electrical Services | SUP-2026-0002 | +266 2231 0002 |
| supplier3@gmail10p.com | psw123 | Highland Roads Construction | SUP-2026-0003 | +266 2231 0003 |

---

## Module 1 — User Registration & Authentication (10 Marks)

### Features Implemented
- **Login**: POST-based authentication with SHA-256 password verification
- **Logout**: Explicit HttpSession invalidation
- **Register**: Supplier self-registration with auto-generated registration number
- **Forgot Password**: 6-digit code sent to email, code-based password reset flow
- **Reset Password**: Code verification + new password update
- **Email Verification**: Registration requires email verification via 6-digit code before account creation
- **Account Confirmation**: Token-based account activation
- **Lockout**: 3 failed login attempts locks the account
- **Role-based Redirect**: OFFICER→/officer-dashboard, SUPPLIER→/supplier-dashboard, EVALUATOR→/evaluator-dashboard
- **Auth Filter**: Protects all authenticated pages from unauthorized access

### Test Flows
1. **Login Test**: Use any credential above → should redirect to role dashboard
2. **Register Test**: /register → fill all fields → should create account in users + suppliers tables
3. **Lockout Test**: /login → wrong password 3 times → account locked message
4. **Password Reset**: /forgot-password → enter email → receive 6-digit code → /reset-password → enter code + new password
5. **Logout Test**: /logout → session invalidated → back to login page

---

## Project Structure
See `_docs/File-Structure.md` for complete file listing.
See `_docs/Deployment-Guide.md` for deployment instructions.

---

## Repository
- **GitHub**: [github.com/Significant-Hacks/ProcureGov](https://github.com/Significant-Hacks/ProcureGov)
- **Pre-built WAR**: Included in `dist/` — ready to drop into any Tomcat instance
- **All dependencies**: JARs in `web/WEB-INF/lib/` — no manual downloads needed
