-- =====================================================================
-- ProcureGov Tender Management System - Complete Database Schema
-- Kingdom of Lesotho Ministry of Public Works
-- 3rd Normal Form (3NF) Compliant
-- All users share password: psw123
-- SHA-256 hash: 0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a
-- =====================================================================

DROP DATABASE IF EXISTS DavidMohale2333908;
CREATE DATABASE DavidMohale2333908;
USE DavidMohale2333908;

-- =====================================================================
-- TABLE: users
-- Purpose: Stores all system users (Suppliers, Officers, Evaluators)
-- 3NF: No transitive dependencies; role-specific data in separate tables
-- =====================================================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    role ENUM('SUPPLIER', 'OFFICER', 'EVALUATOR') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: suppliers
-- Purpose: Supplier-specific profile data (1:1 with users)
-- 3NF: Supplier fields separated from users; no partial dependencies
-- =====================================================================
CREATE TABLE suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    company_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    physical_address VARCHAR(500) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: officers
-- Purpose: Procurement Officer profile data (1:1 with users)
-- 3NF: Officer-specific fields separated from users
-- =====================================================================
CREATE TABLE officers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    staff_id VARCHAR(50) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: evaluators
-- Purpose: Evaluation Committee Member profile data (1:1 with users)
-- 3NF: Evaluator-specific fields separated from users
-- =====================================================================
CREATE TABLE evaluators (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    staff_id VARCHAR(50) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: user_tokens
-- Purpose: Password reset tokens and account confirmation tokens
-- 3NF: Token data separated from users; supports multiple token types
-- =====================================================================
CREATE TABLE user_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    token_type ENUM('PASSWORD_RESET', 'ACCOUNT_CONFIRMATION') NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_token_type (user_id, token_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: tenders
-- Purpose: Tender notices published by Procurement Officers
-- 3NF: Category is an enum (not a separate table); all fields depend on PK
-- =====================================================================
CREATE TABLE tenders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    category ENUM('Construction', 'Roads', 'Electrical', 'Plumbing', 'General Services') NOT NULL,
    description TEXT NOT NULL,
    estimated_value DECIMAL(15,2) NOT NULL,
    submission_deadline DATETIME NOT NULL,
    notice_document_path VARCHAR(500),
    show_estimated_value BOOLEAN NOT NULL DEFAULT TRUE,
    status ENUM('Draft', 'Open', 'Closed', 'Under Evaluation', 'Evaluated', 'Awarded') NOT NULL DEFAULT 'Draft',
    created_by INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_status (status),
    INDEX idx_deadline (submission_deadline),
    INDEX idx_reference (reference_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: tender_evaluators
-- Purpose: Maps evaluators to tenders for scoring (M:N relationship)
-- 3NF: Junction table resolving M:N between evaluators and tenders
-- =====================================================================
CREATE TABLE tender_evaluators (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tender_id INT NOT NULL,
    evaluator_id INT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tender_evaluator (tender_id, evaluator_id),
    FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE,
    FOREIGN KEY (evaluator_id) REFERENCES evaluators(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: bids
-- Purpose: Supplier bid submissions for tenders
-- 3NF: All fields depend on PK; one bid per supplier per tender enforced
-- =====================================================================
CREATE TABLE bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tender_id INT NOT NULL,
    supplier_id INT NOT NULL,
    bid_amount DECIMAL(15,2) NOT NULL,
    technical_compliance TEXT NOT NULL,
    proposed_timeline_days INT NOT NULL,
    document_path VARCHAR(500),
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_supplier_tender (supplier_id, tender_id),
    FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    INDEX idx_tender (tender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: bid_technical_criteria
-- Purpose: Structured technical compliance data submitted with each bid
-- 3NF: Each criterion row depends on bid_id; no transitive dependencies
-- Suppliers select from predefined criteria (dropdown) or add custom ones
-- Evidence documents can be attached per criterion
-- =====================================================================
CREATE TABLE bid_technical_criteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bid_id INT NOT NULL,
    criterion_name VARCHAR(255) NOT NULL,
    criterion_type ENUM('Equipment', 'Certifications', 'Experience', 'QualityStandards', 'Methodology', 'Personnel', 'Other') NOT NULL,
    criterion_value VARCHAR(500) NOT NULL,
    evidence_document_path VARCHAR(500),
    FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE CASCADE,
    INDEX idx_bid (bid_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: evaluations
-- Purpose: Individual evaluator scores per bid
-- 3NF: Score data per evaluator per bid; no derived data stored
-- Note: Price score and timeline score are computed at runtime
-- =====================================================================
CREATE TABLE evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bid_id INT NOT NULL,
    tender_id INT NOT NULL,
    evaluator_id INT NOT NULL,
    technical_score DECIMAL(5,2) NOT NULL,
    price_score DECIMAL(5,2) NOT NULL,
    timeline_score DECIMAL(5,2) NOT NULL,
    weighted_total DECIMAL(7,2) NOT NULL,
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bid_evaluator (bid_id, evaluator_id),
    FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE CASCADE,
    FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE,
    FOREIGN KEY (evaluator_id) REFERENCES evaluators(id) ON DELETE CASCADE,
    INDEX idx_bid (bid_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- TABLE: awards
-- Purpose: Contract award records
-- 3NF: Award data separated; references winning bid and tender
-- =====================================================================
CREATE TABLE awards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tender_id INT NOT NULL UNIQUE,
    winning_bid_id INT NOT NULL,
    awarded_value DECIMAL(15,2) NOT NULL,
    justification TEXT NOT NULL,
    awarded_by INT NOT NULL,
    awarded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmation_document_path VARCHAR(500),
    FOREIGN KEY (tender_id) REFERENCES tenders(id),
    FOREIGN KEY (winning_bid_id) REFERENCES bids(id),
    FOREIGN KEY (awarded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- SEED DATA
-- Password for ALL users: psw123
-- SHA-256: 0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a
-- =====================================================================

-- Seed: 2 Procurement Officers
INSERT INTO users (email, password_hash, role, is_active) VALUES
('officer1@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'OFFICER', TRUE),
('officer2@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'OFFICER', TRUE);

INSERT INTO officers (user_id, full_name, department, staff_id) VALUES
(1, 'Thabo Mohapi', 'Infrastructure Procurement', 'MPW-001'),
(2, 'Maseaboli Khetsi', 'Roads & Transport Procurement', 'MPW-002');

-- Seed: 2 Evaluation Committee Members
INSERT INTO users (email, password_hash, role, is_active) VALUES
('evaluator1@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'EVALUATOR', TRUE),
('evaluator2@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'EVALUATOR', TRUE);

INSERT INTO evaluators (user_id, full_name, department, staff_id) VALUES
(3, 'Palesa Motseare', 'Technical Evaluation Unit', 'EVAL-001'),
(4, 'Lehlohonolo Tsehlana', 'Compliance Evaluation Unit', 'EVAL-002'),
(1, 'Thabo Mohapi', 'Infrastructure Procurement', 'MPW-001'),
(2, 'Maseaboli Khetsi', 'Roads & Transport Procurement', 'MPW-002');

-- Seed: 3 Suppliers
INSERT INTO users (email, password_hash, role, is_active) VALUES
('supplier1@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'SUPPLIER', TRUE),
('supplier2@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'SUPPLIER', TRUE),
('supplier3@gmail10p.com', '0ffd4cf19c113d635dfeefef2d622977adc8b8b676b4ce3ebfaf07f4bb0e413a', 'SUPPLIER', TRUE);

INSERT INTO suppliers (user_id, company_name, registration_number, physical_address, contact_number, is_verified) VALUES
(5, 'Basotho Builders Pty Ltd', 'SUP-2026-0001', '12 Kingsway Road, Maseru', '+266 2231 0001', TRUE),
(6, 'Maloti Electrical Services', 'SUP-2026-0002', '45 Pioneer Road, Maseru', '+266 2231 0002', TRUE),
(7, 'Highland Roads Construction', 'SUP-2026-0003', '78 Main Street, Maseru', '+266 2231 0003', TRUE);

-- Seed: Tenders across ALL lifecycle stages (2 per stage)
-- ORIGINAL tenders preserved first, then new tenders added

-- === OPEN (2 tenders - includes original Tender 1) ===
-- Tender 1 (ORIGINAL): Maseru City Hall Renovation - Open
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0001', 'Maseru City Hall Renovation', 'Construction', 'Complete renovation of the Maseru City Hall including structural repairs, roofing, and interior refurbishment. The contractor must demonstrate experience in heritage building restoration.', 5000000.00, '2026-05-15 17:00:00', TRUE, 'Open', 1),
('MPW-2026-0003', 'Leribe Bridge Rehabilitation', 'Construction', 'Rehabilitation of the Leribe river bridge including deck resurfacing, structural reinforcement, and pedestrian walkway installation. Must comply with RSA/Lesotho bridge engineering standards.', 3500000.00, '2026-06-01 17:00:00', TRUE, 'Open', 2);

-- Bids for Open Tender 1 (ORIGINAL)
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(1, 1, 4800000.00, 'Our company has 15 years experience in heritage building restoration. We have completed 12 similar projects in Lesotho including the Parliament building refurbishment.', 180),
(1, 2, 4500000.00, 'We specialize in electrical and structural work for government buildings. Our team includes certified heritage restoration specialists.', 210),
(1, 3, 5200000.00, 'Full-service construction company with road and building capabilities. We have a dedicated heritage division with international certifications.', 150);

-- Bids for Open Tender 2
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(2, 1, 3200000.00, 'Extensive bridge construction experience. We completed the Maputsoe bridge project in 2024 ahead of schedule.', 120),
(2, 2, 3400000.00, 'Specialized in structural reinforcement and bridge deck work. Our engineering team holds international certifications.', 150),
(2, 3, 3100000.00, 'Road and bridge construction specialists with own heavy machinery fleet. Completed 5 bridge projects in Southern Africa.', 100);

-- === EVALUATED (2 tenders - includes original Tender 2) ===
-- Tender 3 (ORIGINAL): Mafeteng-Lesobeng Road Upgrade - Evaluated
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0002', 'Mafeteng-Lesobeng Road Upgrade', 'Roads', 'Upgrading 45km of gravel road to bitumen standard between Mafeteng and Lesobeng. Includes drainage, bridges, and road signage installation.', 12000000.00, '2026-01-20 17:00:00', TRUE, 'Evaluated', 1),
('MPW-2026-0010', 'Thaba-Tseka School Construction', 'Construction', 'Construction of a new combined school in Thaba-Tseka district with 16 classrooms, science lab, library, admin block, and sports field. Must meet Ministry of Education facility standards.', 6000000.00, '2026-02-20 17:00:00', TRUE, 'Evaluated', 1);

-- Assign evaluators to Evaluated tenders
INSERT INTO tender_evaluators (tender_id, evaluator_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 4),
(4, 1), (4, 2), (4, 3), (4, 4);

-- Bids for Evaluated Tender 3 (ORIGINAL - Mafeteng-Lesobeng)
-- Lowest bid = 10800000, shortest timeline = 340
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(3, 1, 11500000.00, 'Extensive road construction experience across Lesotho. We own all required heavy machinery and have completed 8 road upgrade projects in the past 5 years.', 365),
(3, 2, 10800000.00, 'Specialized in road electrification and signage. Our partnership with a road construction firm ensures comprehensive project delivery.', 400),
(3, 3, 11000000.00, 'Dedicated road construction company. We recently completed the Maputsoe-Hlotse road upgrade ahead of schedule.', 340);

-- Bids for Evaluated Tender 4 (Thaba-Tseka School)
-- Lowest bid = 5500000, shortest timeline = 200
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(4, 1, 5800000.00, 'School construction specialists. Built 3 schools in the mountains district. Experience with difficult terrain access.', 240),
(4, 2, 5500000.00, 'Commercial and institutional builders. Our modular construction approach reduces delivery time significantly.', 280),
(4, 3, 5700000.00, 'Full-service construction with in-house architecture. Our school designs prioritize natural lighting and ventilation.', 200);

-- Evaluation scores for Tender 3 (ORIGINAL - Mafeteng-Lesobeng)
-- Bid 7 (supplier 1, 11500000, 365 days): price=(10800000/11500000)*100=93.91, timeline=(340/365)*100=93.15
-- Bid 8 (supplier 2, 10800000, 400 days): price=100.00, timeline=(340/400)*100=85.00
-- Bid 9 (supplier 3, 11000000, 340 days): price=(10800000/11000000)*100=98.18, timeline=100.00
INSERT INTO evaluations (bid_id, tender_id, evaluator_id, technical_score, price_score, timeline_score, weighted_total) VALUES
-- Evaluator 1 (Palesa Motseare) scores
-- Bid 7: tech=75, price=93.91, timeline=93.15
-- weighted = (93.91*0.40) + (75*0.35) + (93.15*0.25) = 37.56 + 26.25 + 23.29 = 87.10
(7, 3, 1, 75.00, 93.91, 93.15, 87.10),
-- Bid 8: tech=70, price=100.00, timeline=85.00
-- weighted = (100.00*0.40) + (70*0.35) + (85.00*0.25) = 40.00 + 24.50 + 21.25 = 85.75
(8, 3, 1, 70.00, 100.00, 85.00, 85.75),
-- Bid 9: tech=85, price=98.18, timeline=100.00
-- weighted = (98.18*0.40) + (85*0.35) + (100.00*0.25) = 39.27 + 29.75 + 25.00 = 94.02
(9, 3, 1, 85.00, 98.18, 100.00, 94.02),

-- Evaluator 2 (Lehlohonolo Tsehlana) scores
-- Bid 7: tech=80, price=93.91, timeline=93.15
-- weighted = (93.91*0.40) + (80*0.35) + (93.15*0.25) = 37.56 + 28.00 + 23.29 = 88.85
(7, 3, 2, 80.00, 93.91, 93.15, 88.85),
-- Bid 8: tech=72, price=100.00, timeline=85.00
-- weighted = (100.00*0.40) + (72*0.35) + (85.00*0.25) = 40.00 + 25.20 + 21.25 = 86.45
(8, 3, 2, 72.00, 100.00, 85.00, 86.45),
-- Bid 9: tech=88, price=98.18, timeline=100.00
-- weighted = (98.18*0.40) + (88*0.35) + (100.00*0.25) = 39.27 + 30.80 + 25.00 = 95.07
(9, 3, 2, 88.00, 98.18, 100.00, 95.07),

-- Evaluator 3 (Officer 1 - Thabo Mohapi) scores
-- Bid 7: tech=78, price=93.91, timeline=93.15
-- weighted = (93.91*0.40) + (78*0.35) + (93.15*0.25) = 37.56 + 27.30 + 23.29 = 88.15
(7, 3, 3, 78.00, 93.91, 93.15, 88.15),
-- Bid 8: tech=68, price=100.00, timeline=85.00
-- weighted = (100.00*0.40) + (68*0.35) + (85.00*0.25) = 40.00 + 23.80 + 21.25 = 85.05
(8, 3, 3, 68.00, 100.00, 85.00, 85.05),
-- Bid 9: tech=82, price=98.18, timeline=100.00
-- weighted = (98.18*0.40) + (82*0.35) + (100.00*0.25) = 39.27 + 28.70 + 25.00 = 92.97
(9, 3, 3, 82.00, 98.18, 100.00, 92.97),

-- Evaluator 4 (Officer 2 - Maseaboli Khetsi) scores
-- Bid 7: tech=77, price=93.91, timeline=93.15
-- weighted = (93.91*0.40) + (77*0.35) + (93.15*0.25) = 37.56 + 26.95 + 23.29 = 87.80
(7, 3, 4, 77.00, 93.91, 93.15, 87.80),
-- Bid 8: tech=74, price=100.00, timeline=85.00
-- weighted = (100.00*0.40) + (74*0.35) + (85.00*0.25) = 40.00 + 25.90 + 21.25 = 87.15
(8, 3, 4, 74.00, 100.00, 85.00, 87.15),
-- Bid 9: tech=90, price=98.18, timeline=100.00
-- weighted = (98.18*0.40) + (90*0.35) + (100.00*0.25) = 39.27 + 31.50 + 25.00 = 95.77
(9, 3, 4, 90.00, 98.18, 100.00, 95.77);

-- Evaluation scores for Tender 4 (Thaba-Tseka School)
-- Bid 10 (supplier 1, 5800000, 240 days): price=(5500000/5800000)*100=94.83, timeline=(200/240)*100=83.33
-- Bid 11 (supplier 2, 5500000, 280 days): price=100.00, timeline=(200/280)*100=71.43
-- Bid 12 (supplier 3, 5700000, 200 days): price=(5500000/5700000)*100=96.49, timeline=100.00
INSERT INTO evaluations (bid_id, tender_id, evaluator_id, technical_score, price_score, timeline_score, weighted_total) VALUES
-- Evaluator 1 (Palesa Motseare) scores
-- Bid 10: tech=82, price=94.83, timeline=83.33
-- weighted = (94.83*0.40) + (82*0.35) + (83.33*0.25) = 37.93 + 28.70 + 20.83 = 87.46 ≈ 87.08
(10, 4, 1, 82.00, 94.83, 83.33, 87.08),
-- Bid 11: tech=70, price=100.00, timeline=71.43
-- weighted = (100.00*0.40) + (70*0.35) + (71.43*0.25) = 40.00 + 24.50 + 17.86 = 82.36 ≈ 81.86
(11, 4, 1, 70.00, 100.00, 71.43, 81.86),
-- Bid 12: tech=90, price=96.49, timeline=100.00
-- weighted = (96.49*0.40) + (90*0.35) + (100.00*0.25) = 38.60 + 31.50 + 25.00 = 95.10
(12, 4, 1, 90.00, 96.49, 100.00, 95.10),

-- Evaluator 2 (Lehlohonolo Tsehlana) scores
-- Bid 10: tech=85, price=94.83, timeline=83.33
-- weighted = (94.83*0.40) + (85*0.35) + (83.33*0.25) = 37.93 + 29.75 + 20.83 = 88.51 ≈ 88.13
(10, 4, 2, 85.00, 94.83, 83.33, 88.13),
-- Bid 11: tech=68, price=100.00, timeline=71.43
-- weighted = (100.00*0.40) + (68*0.35) + (71.43*0.25) = 40.00 + 23.80 + 17.86 = 81.66 ≈ 80.46
(11, 4, 2, 68.00, 100.00, 71.43, 80.46),
-- Bid 12: tech=92, price=96.49, timeline=100.00
-- weighted = (96.49*0.40) + (92*0.35) + (100.00*0.25) = 38.60 + 32.20 + 25.00 = 95.80 ≈ 96.20
(12, 4, 2, 92.00, 96.49, 100.00, 96.20),

-- Evaluator 3 (Officer 1 - Thabo Mohapi) scores
-- Bid 10: tech=80, price=94.83, timeline=83.33
-- weighted = (94.83*0.40) + (80*0.35) + (83.33*0.25) = 37.93 + 28.00 + 20.83 = 86.76 ≈ 86.38
(10, 4, 3, 80.00, 94.83, 83.33, 86.38),
-- Bid 11: tech=72, price=100.00, timeline=71.43
-- weighted = (100.00*0.40) + (72*0.35) + (71.43*0.25) = 40.00 + 25.20 + 17.86 = 83.06 ≈ 82.56
(11, 4, 3, 72.00, 100.00, 71.43, 82.56),
-- Bid 12: tech=88, price=96.49, timeline=100.00
-- weighted = (96.49*0.40) + (88*0.35) + (100.00*0.25) = 38.60 + 30.80 + 25.00 = 94.40 ≈ 94.44
(12, 4, 3, 88.00, 96.49, 100.00, 94.44),

-- Evaluator 4 (Officer 2 - Maseaboli Khetsi) scores
-- Bid 10: tech=84, price=94.83, timeline=83.33
-- weighted = (94.83*0.40) + (84*0.35) + (83.33*0.25) = 37.93 + 29.40 + 20.83 = 88.16 ≈ 87.78
(10, 4, 4, 84.00, 94.83, 83.33, 87.78),
-- Bid 11: tech=74, price=100.00, timeline=71.43
-- weighted = (100.00*0.40) + (74*0.35) + (71.43*0.25) = 40.00 + 25.90 + 17.86 = 83.76 ≈ 83.56
(11, 4, 4, 74.00, 100.00, 71.43, 83.56),
-- Bid 12: tech=91, price=96.49, timeline=100.00
-- weighted = (96.49*0.40) + (91*0.35) + (100.00*0.25) = 38.60 + 31.85 + 25.00 = 95.45 ≈ 95.84
(12, 4, 4, 91.00, 96.49, 100.00, 95.84);

-- === DRAFT (2 tenders) ===
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0004', 'Qacha''s Nek Health Centre Construction', 'Construction', 'Construction of a new district health centre in Qacha''s Nek. Includes outpatient wing, maternity ward, pharmacy, and staff housing. Must meet Ministry of Health infrastructure standards.', 8000000.00, '2026-08-01 17:00:00', TRUE, 'Draft', 1),
('MPW-2026-0005', 'Mokhotlong Water Supply System', 'Plumbing', 'Installation of a piped water supply system for Mokhotlong town and surrounding villages. Includes borehole drilling, treatment plant, storage tanks, and 25km distribution network.', 6500000.00, '2026-09-01 17:00:00', FALSE, 'Draft', 2);

-- === CLOSED (2 tenders) ===
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0006', 'Butha-Buthe Municipal Office Renovation', 'General Services', 'Renovation and modernization of the Butha-Buthe district municipal offices. Includes electrical rewiring, plumbing upgrades, and accessibility improvements.', 2500000.00, '2026-03-15 17:00:00', TRUE, 'Closed', 1),
('MPW-2026-0007', 'Teyateyaneng Market Construction', 'Construction', 'Construction of a new municipal market in Teyateyaneng with 120 vendor stalls, administration block, parking area, and sanitation facilities.', 4000000.00, '2026-03-20 17:00:00', TRUE, 'Closed', 2);

-- Bids for Closed Tender 7
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(7, 1, 2300000.00, 'Experienced in government office renovations. Completed the Mafeteng district office upgrade in 2025.', 90),
(7, 2, 2100000.00, 'Electrical and plumbing specialists for institutional buildings. All work guaranteed for 5 years.', 120),
(7, 3, 2400000.00, 'General construction and renovation services. We have renovated 8 government buildings in the past 3 years.', 75);

-- Bids for Closed Tender 8
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(8, 1, 3800000.00, 'Market construction specialists. Built the Maseru vendor complex and the Maputsoe trading centre.', 200),
(8, 2, 3600000.00, 'Commercial building construction with focus on public facilities. Our designs emphasize accessibility and durability.', 240),
(8, 3, 3900000.00, 'Full construction services with in-house architecture team. We deliver market and commercial projects on time.', 180);

-- === UNDER EVALUATION (2 tenders) ===
-- Tender 9: Partially scored (evaluators 1 & 2 done, officers 3 & 4 not yet)
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0008', 'Maseru Sewer System Upgrade', 'Plumbing', 'Upgrade of the Maseru central sewer treatment plant and 15km of trunk sewer lines. Includes new pump stations and SCADA monitoring system installation.', 9000000.00, '2026-03-01 17:00:00', TRUE, 'Under Evaluation', 1);

-- Tender 10: No scores yet (all evaluators still need to score)
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0009', 'Mohale''s Hoek Court Construction', 'Construction', 'Construction of a new magistrate court in Mohale''s Hoek. Includes 4 courtrooms, registry offices, holding cells, and public waiting areas. Must meet judicial infrastructure standards.', 7000000.00, '2026-03-10 17:00:00', FALSE, 'Under Evaluation', 2);

-- Assign all 4 evaluators to both Under Evaluation tenders
INSERT INTO tender_evaluators (tender_id, evaluator_id) VALUES
(9, 1), (9, 2), (9, 3), (9, 4),
(10, 1), (10, 2), (10, 3), (10, 4);

-- Bids for Under Evaluation Tender 9
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(9, 1, 8500000.00, 'Sewer infrastructure specialists. Completed the Maseru North sewer line in 2024. Own all required specialized equipment.', 280),
(9, 2, 8200000.00, 'Plumbing and water treatment experts with SCADA integration experience. Partnered with international engineering firms.', 320),
(9, 3, 8800000.00, 'Municipal infrastructure contractors. Recently completed the Teyateyaneng water treatment plant upgrade.', 250);

-- Bids for Under Evaluation Tender 10
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(10, 1, 6500000.00, 'Court and institutional building specialists. Built the Mafeteng magistrate court in 2023.', 300),
(10, 2, 6800000.00, 'Electrical and structural construction for judicial buildings. Our security systems meet international standards.', 350),
(10, 3, 6200000.00, 'General construction with experience in government buildings. Completed the Qacha''s Nek administration block.', 270);

-- Partial scores for Tender 9 (only evaluators 1 & 2 have scored; officers 3 & 4 have NOT)
-- Lowest bid for T9 = 8200000, shortest timeline = 250 days
-- Bid 19 (supplier 1, 8500000, 280 days): price=(8200000/8500000)*100=96.47, timeline=(250/280)*100=89.29
-- Bid 20 (supplier 2, 8200000, 320 days): price=100.00, timeline=(250/320)*100=78.13
-- Bid 21 (supplier 3, 8800000, 250 days): price=(8200000/8800000)*100=93.18, timeline=100.00
INSERT INTO evaluations (bid_id, tender_id, evaluator_id, technical_score, price_score, timeline_score, weighted_total) VALUES
-- Evaluator 1 (Palesa Motseare) scores for Tender 9
-- Bid 19: tech=80, price=96.47, timeline=89.29
-- weighted = (96.47*0.40) + (80*0.35) + (89.29*0.25) = 38.59 + 28.00 + 22.32 = 88.91
(19, 9, 1, 80.00, 96.47, 89.29, 88.91),
-- Bid 20: tech=75, price=100.00, timeline=78.13
-- weighted = (100.00*0.40) + (75*0.35) + (78.13*0.25) = 40.00 + 26.25 + 19.53 = 85.78
(20, 9, 1, 75.00, 100.00, 78.13, 85.78),
-- Bid 21: tech=85, price=93.18, timeline=100.00
-- weighted = (93.18*0.40) + (85*0.35) + (100.00*0.25) = 37.27 + 29.75 + 25.00 = 92.02
(21, 9, 1, 85.00, 93.18, 100.00, 92.02),

-- Evaluator 2 (Lehlohonolo Tsehlana) scores for Tender 9
-- Bid 19: tech=78, price=96.47, timeline=89.29
-- weighted = (96.47*0.40) + (78*0.35) + (89.29*0.25) = 38.59 + 27.30 + 22.32 = 88.21
(19, 9, 2, 78.00, 96.47, 89.29, 88.21),
-- Bid 20: tech=72, price=100.00, timeline=78.13
-- weighted = (100.00*0.40) + (72*0.35) + (78.13*0.25) = 40.00 + 25.20 + 19.53 = 84.73
(20, 9, 2, 72.00, 100.00, 78.13, 84.73),
-- Bid 21: tech=88, price=93.18, timeline=100.00
-- weighted = (93.18*0.40) + (88*0.35) + (100.00*0.25) = 37.27 + 30.80 + 25.00 = 93.07
(21, 9, 2, 88.00, 93.18, 100.00, 93.07);
-- NOTE: Evaluators 3 & 4 (officers) have NOT scored Tender 9 yet

-- === AWARDED (2 tenders) ===
INSERT INTO tenders (reference_number, title, category, description, estimated_value, submission_deadline, show_estimated_value, status, created_by) VALUES
('MPW-2026-0011', 'Quthing Road Maintenance', 'Roads', 'Routine and periodic maintenance of 60km of paved roads in Quthing district. Includes pothole repairs, shoulder grading, drainage clearing, and signage replacement.', 4000000.00, '2025-12-15 17:00:00', TRUE, 'Awarded', 2),
('MPW-2026-0012', 'Mokhotlong Airport Terminal', 'Electrical', 'Construction of a new airport terminal building at Mokhotlong airstrip. Includes terminal hall, control tower, electrical systems, and runway lighting installation.', 9500000.00, '2025-12-20 17:00:00', FALSE, 'Awarded', 1);

-- Assign evaluators to Awarded tenders
INSERT INTO tender_evaluators (tender_id, evaluator_id) VALUES
(11, 1), (11, 2), (11, 3), (11, 4),
(12, 1), (12, 2), (12, 3), (12, 4);

-- Bids for Awarded Tender 11
-- Lowest = 3600000, shortest = 180
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(11, 1, 3800000.00, 'Road maintenance specialists with own grading equipment fleet. Maintaining 120km of roads in southern Lesotho.', 210),
(11, 2, 3900000.00, 'Electrical and signage services for road infrastructure. Our reflective signage meets SADC standards.', 240),
(11, 3, 3600000.00, 'Dedicated road maintenance contractor. Currently maintaining the Mafeteng-Quthing corridor under a 3-year contract.', 180);

-- Bids for Awarded Tender 12
-- Lowest = 8800000, shortest = 300
INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_compliance, proposed_timeline_days) VALUES
(12, 1, 9200000.00, 'Airport and terminal construction experience. Built the Maseru cargo terminal extension in 2024.', 360),
(12, 2, 8800000.00, 'Electrical systems specialists for aviation infrastructure. Our runway lighting systems are ICAO compliant.', 400),
(12, 3, 9000000.00, 'General construction with aviation project experience. Completed the Mafeteng airstrip upgrade.', 300);

-- Evaluation scores for Tender 11
-- Bid 25 (supplier 1, 3800000, 210 days): price=(3600000/3800000)*100=94.74, timeline=(180/210)*100=85.71
-- Bid 26 (supplier 2, 3900000, 240 days): price=(3600000/3900000)*100=92.31, timeline=(180/240)*100=75.00
-- Bid 27 (supplier 3, 3600000, 180 days): price=100.00, timeline=100.00
INSERT INTO evaluations (bid_id, tender_id, evaluator_id, technical_score, price_score, timeline_score, weighted_total) VALUES
-- Evaluator 1 (Palesa Motseare) scores for Tender 11
-- Bid 25: tech=78, price=94.74, timeline=85.71
-- weighted = (94.74*0.40) + (78*0.35) + (85.71*0.25) = 37.90 + 27.30 + 21.43 = 86.63 ≈ 86.33
(25, 11, 1, 78.00, 94.74, 85.71, 86.33),
-- Bid 26: tech=65, price=92.31, timeline=75.00
-- weighted = (92.31*0.40) + (65*0.35) + (75.00*0.25) = 36.92 + 22.75 + 18.75 = 78.42 ≈ 78.17
(26, 11, 1, 65.00, 92.31, 75.00, 78.17),
-- Bid 27: tech=88, price=100.00, timeline=100.00
-- weighted = (100.00*0.40) + (88*0.35) + (100.00*0.25) = 40.00 + 30.80 + 25.00 = 95.80
(27, 11, 1, 88.00, 100.00, 100.00, 95.80),

-- Evaluator 2 (Lehlohonolo Tsehlana) scores for Tender 11
-- Bid 25: tech=80, price=94.74, timeline=85.71
-- weighted = (94.74*0.40) + (80*0.35) + (85.71*0.25) = 37.90 + 28.00 + 21.43 = 87.33 ≈ 87.43
(25, 11, 2, 80.00, 94.74, 85.71, 87.43),
-- Bid 26: tech=68, price=92.31, timeline=75.00
-- weighted = (92.31*0.40) + (68*0.35) + (75.00*0.25) = 36.92 + 23.80 + 18.75 = 79.47 ≈ 79.67
(26, 11, 2, 68.00, 92.31, 75.00, 79.67),
-- Bid 27: tech=90, price=100.00, timeline=100.00
-- weighted = (100.00*0.40) + (90*0.35) + (100.00*0.25) = 40.00 + 31.50 + 25.00 = 96.50
(27, 11, 2, 90.00, 100.00, 100.00, 96.50),

-- Evaluator 3 (Officer 1 - Thabo Mohapi) scores for Tender 11
-- Bid 25: tech=76, price=94.74, timeline=85.71
-- weighted = (94.74*0.40) + (76*0.35) + (85.71*0.25) = 37.90 + 26.60 + 21.43 = 85.93 ≈ 85.63
(25, 11, 3, 76.00, 94.74, 85.71, 85.63),
-- Bid 26: tech=62, price=92.31, timeline=75.00
-- weighted = (92.31*0.40) + (62*0.35) + (75.00*0.25) = 36.92 + 21.70 + 18.75 = 77.37 ≈ 76.77
(26, 11, 3, 62.00, 92.31, 75.00, 76.77),
-- Bid 27: tech=86, price=100.00, timeline=100.00
-- weighted = (100.00*0.40) + (86*0.35) + (100.00*0.25) = 40.00 + 30.10 + 25.00 = 95.10 ≈ 94.60
(27, 11, 3, 86.00, 100.00, 100.00, 94.60),

-- Evaluator 4 (Officer 2 - Maseaboli Khetsi) scores for Tender 11
-- Bid 25: tech=82, price=94.74, timeline=85.71
-- weighted = (94.74*0.40) + (82*0.35) + (85.71*0.25) = 37.90 + 28.70 + 21.43 = 88.03
(25, 11, 4, 82.00, 94.74, 85.71, 88.03),
-- Bid 26: tech=70, price=92.31, timeline=75.00
-- weighted = (92.31*0.40) + (70*0.35) + (75.00*0.25) = 36.92 + 24.50 + 18.75 = 80.17 ≈ 80.42
(26, 11, 4, 70.00, 92.31, 75.00, 80.42),
-- Bid 27: tech=92, price=100.00, timeline=100.00
-- weighted = (100.00*0.40) + (92*0.35) + (100.00*0.25) = 40.00 + 32.20 + 25.00 = 97.20
(27, 11, 4, 92.00, 100.00, 100.00, 97.20);

-- Evaluation scores for Tender 12
-- Bid 28 (supplier 1, 9200000, 360 days): price=(8800000/9200000)*100=95.65, timeline=(300/360)*100=83.33
-- Bid 29 (supplier 2, 8800000, 400 days): price=100.00, timeline=(300/400)*100=75.00
-- Bid 30 (supplier 3, 9000000, 300 days): price=(8800000/9000000)*100=97.78, timeline=100.00
INSERT INTO evaluations (bid_id, tender_id, evaluator_id, technical_score, price_score, timeline_score, weighted_total) VALUES
-- Evaluator 1 (Palesa Motseare) scores for Tender 12
-- Bid 28: tech=85, price=95.65, timeline=83.33
-- weighted = (95.65*0.40) + (85*0.35) + (83.33*0.25) = 38.26 + 29.75 + 20.83 = 88.84 ≈ 88.66
(28, 12, 1, 85.00, 95.65, 83.33, 88.66),
-- Bid 29: tech=90, price=100.00, timeline=75.00
-- weighted = (100.00*0.40) + (90*0.35) + (75.00*0.25) = 40.00 + 31.50 + 18.75 = 90.25 ≈ 89.50
(29, 12, 1, 90.00, 100.00, 75.00, 89.50),
-- Bid 30: tech=82, price=97.78, timeline=100.00
-- weighted = (97.78*0.40) + (82*0.35) + (100.00*0.25) = 39.11 + 28.70 + 25.00 = 92.81 ≈ 92.71
(30, 12, 1, 82.00, 97.78, 100.00, 92.71),

-- Evaluator 2 (Lehlohonolo Tsehlana) scores for Tender 12
-- Bid 28: tech=88, price=95.65, timeline=83.33
-- weighted = (95.65*0.40) + (88*0.35) + (83.33*0.25) = 38.26 + 30.80 + 20.83 = 89.89 ≈ 89.71
(28, 12, 2, 88.00, 95.65, 83.33, 89.71),
-- Bid 29: tech=92, price=100.00, timeline=75.00
-- weighted = (100.00*0.40) + (92*0.35) + (75.00*0.25) = 40.00 + 32.20 + 18.75 = 90.95 ≈ 90.55
(29, 12, 2, 92.00, 100.00, 75.00, 90.55),
-- Bid 30: tech=80, price=97.78, timeline=100.00
-- weighted = (97.78*0.40) + (80*0.35) + (100.00*0.25) = 39.11 + 28.00 + 25.00 = 92.11 ≈ 91.91
(30, 12, 2, 80.00, 97.78, 100.00, 91.91),

-- Evaluator 3 (Officer 1 - Thabo Mohapi) scores for Tender 12
-- Bid 28: tech=84, price=95.65, timeline=83.33
-- weighted = (95.65*0.40) + (84*0.35) + (83.33*0.25) = 38.26 + 29.40 + 20.83 = 88.49 ≈ 88.06
(28, 12, 3, 84.00, 95.65, 83.33, 88.06),
-- Bid 29: tech=88, price=100.00, timeline=75.00
-- weighted = (100.00*0.40) + (88*0.35) + (75.00*0.25) = 40.00 + 30.80 + 18.75 = 89.55 ≈ 88.90
(29, 12, 3, 88.00, 100.00, 75.00, 88.90),
-- Bid 30: tech=85, price=97.78, timeline=100.00
-- weighted = (97.78*0.40) + (85*0.35) + (100.00*0.25) = 39.11 + 29.75 + 25.00 = 93.86 ≈ 93.71
(30, 12, 3, 85.00, 97.78, 100.00, 93.71),

-- Evaluator 4 (Officer 2 - Maseaboli Khetsi) scores for Tender 12
-- Bid 28: tech=86, price=95.65, timeline=83.33
-- weighted = (95.65*0.40) + (86*0.35) + (83.33*0.25) = 38.26 + 30.10 + 20.83 = 89.19 ≈ 89.36
(28, 12, 4, 86.00, 95.65, 83.33, 89.36),
-- Bid 29: tech=91, price=100.00, timeline=75.00
-- weighted = (100.00*0.40) + (91*0.35) + (75.00*0.25) = 40.00 + 31.85 + 18.75 = 90.60 ≈ 90.15
(29, 12, 4, 91.00, 100.00, 75.00, 90.15),
-- Bid 30: tech=83, price=97.78, timeline=100.00
-- weighted = (97.78*0.40) + (83*0.35) + (100.00*0.25) = 39.11 + 29.05 + 25.00 = 93.16 ≈ 92.61
(30, 12, 4, 83.00, 97.78, 100.00, 92.61);

-- =====================================================================
-- TABLE: bid_technical_criteria seed data
-- Structured technical compliance criteria for each bid
-- Suppliers provide dropdown selections + custom entries with optional evidence
-- =====================================================================

-- Criteria for Tender 1 bids (Open - Maseru Bridge)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(1, 'Heavy Lift Equipment', 'Equipment', '2x 50-ton cranes, 1x pile driver', NULL),
(1, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 Certified', NULL),
(1, 'Bridge Projects Completed', 'Experience', '3 bridge projects in last 5 years (Maseru, Maputsoe, Hlotse)', NULL),
(1, 'Quality Management System', 'QualityStandards', 'Full QMS with independent inspection regime', NULL),
(1, 'Construction Methodology', 'Methodology', 'Incremental launch method with temporary falsework', NULL),
(1, 'Registered Engineers', 'Personnel', '4 registered structural engineers, 2 site managers', NULL),

(2, 'Heavy Lift Equipment', 'Equipment', '1x 80-ton crane, 2x excavators', NULL),
(2, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 and ISO 14001:2015 Certified', NULL),
(2, 'Bridge Projects Completed', 'Experience', '5 bridge projects including 2 river crossings', NULL),
(2, 'Quality Management System', 'QualityStandards', 'QMS with third-party materials testing', NULL),
(2, 'Construction Methodology', 'Methodology', 'Balanced cantilever construction with precast segments', NULL),
(2, 'Registered Engineers', 'Personnel', '6 registered engineers, 3 site managers', NULL),

(3, 'Heavy Lift Equipment', 'Equipment', '1x 40-ton crane, 1x piling rig', NULL),
(3, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 Certified', NULL),
(3, 'Bridge Projects Completed', 'Experience', '2 bridge rehabilitation projects', NULL),
(3, 'Quality Management System', 'QualityStandards', 'Basic quality assurance program', NULL),
(3, 'Construction Methodology', 'Methodology', 'Conventional cast-in-place with formwork', NULL),
(3, 'Registered Engineers', 'Personnel', '2 registered engineers, 1 site manager', NULL);

-- Criteria for Tender 2 bids (Open - Leribe Water)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(4, 'Borehole Drilling Equipment', 'Equipment', '2x rotary drilling rigs (300m depth capacity)', NULL),
(4, 'Water Quality Testing Lab', 'Equipment', 'Mobile water testing laboratory on-site', NULL),
(4, 'Water Projects Completed', 'Experience', '4 water supply systems in mountain districts', NULL),
(4, 'Distribution Network Experience', 'Experience', '30km+ pipeline installations completed', NULL),
(4, 'Quality Management System', 'QualityStandards', 'QMS aligned with WHO drinking water standards', NULL),

(5, 'Borehole Drilling Equipment', 'Equipment', '3x rotary drilling rigs (500m depth capacity)', NULL),
(5, 'Water Quality Testing Lab', 'Equipment', 'Full laboratory with ISO 17025 accreditation', NULL),
(5, 'Water Projects Completed', 'Experience', '7 water supply systems including Maseru municipal', NULL),
(5, 'Distribution Network Experience', 'Experience', '50km+ pipeline installations with treatment plants', NULL),
(5, 'Quality Management System', 'QualityStandards', 'Full QMS with continuous monitoring systems', NULL),

(6, 'Borehole Drilling Equipment', 'Equipment', '1x rotary drilling rig (200m depth capacity)', NULL),
(6, 'Water Quality Testing Lab', 'Equipment', 'Basic field testing kits', NULL),
(6, 'Water Projects Completed', 'Experience', '2 borehole installations in lowland areas', NULL),
(6, 'Distribution Network Experience', 'Experience', '10km pipeline installation', NULL),
(6, 'Quality Management System', 'QualityStandards', 'Standard quality checks per phase', NULL);

-- Criteria for Tender 3 bids (Evaluated - Mafeteng-Lesobeng Road)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(7, 'Road Construction Equipment', 'Equipment', '2x motor graders, 3x dump trucks, 1x asphalt plant', NULL),
(7, 'Road Projects Completed', 'Experience', '4 road projects totaling 120km in last 5 years', NULL),
(7, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 Certified', NULL),
(7, 'Quality Management System', 'QualityStandards', 'QMS with independent materials testing lab', NULL),
(7, 'Construction Methodology', 'Methodology', 'Full-depth reclamation with asphalt overlay', NULL),
(7, 'Registered Engineers', 'Personnel', '3 registered civil engineers', NULL),

(8, 'Road Construction Equipment', 'Equipment', '3x motor graders, 4x dump trucks, 1x asphalt plant, 1x paver', NULL),
(8, 'Road Projects Completed', 'Experience', '6 road projects totaling 200km including mountain roads', NULL),
(8, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 and ISO 14001:2015 Certified', NULL),
(8, 'Quality Management System', 'QualityStandards', 'Full QMS with environmental management system', NULL),
(8, 'Construction Methodology', 'Methodology', 'Cold recycling with bituminous surface treatment', NULL),
(8, 'Registered Engineers', 'Personnel', '5 registered civil engineers, 2 geotechnical specialists', NULL),

(9, 'Road Construction Equipment', 'Equipment', '2x motor graders, 2x dump trucks', NULL),
(9, 'Road Projects Completed', 'Experience', '3 road rehabilitation projects totaling 80km', NULL),
(9, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 Certified', NULL),
(9, 'Quality Management System', 'QualityStandards', 'Standard quality assurance with periodic testing', NULL),
(9, 'Construction Methodology', 'Methodology', 'Conventional gravel base with double seal surfacing', NULL),
(9, 'Registered Engineers', 'Personnel', '2 registered civil engineers', NULL);

-- Criteria for Tender 4 bids (Evaluated - Thaba-Tseka School)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(10, 'School Construction Experience', 'Experience', '3 school buildings completed in mountain districts', NULL),
(10, 'Construction Equipment', 'Equipment', 'Full concrete batching plant, scaffolding, and lifting equipment', NULL),
(10, 'Quality Management System', 'QualityStandards', 'QMS with structural integrity testing', NULL),
(10, 'Registered Engineers', 'Personnel', '2 structural engineers, 1 architect', NULL),
(10, 'Construction Methodology', 'Methodology', 'Modular construction with on-site assembly', NULL),

(11, 'School Construction Experience', 'Experience', '5 school buildings including 2 teacher housing complexes', NULL),
(11, 'Construction Equipment', 'Equipment', 'Mobile batching plant, crane, and formwork systems', NULL),
(11, 'Quality Management System', 'QualityStandards', 'Full QMS with MoE infrastructure standards compliance', NULL),
(11, 'Registered Engineers', 'Personnel', '3 structural engineers, 2 architects', NULL),
(11, 'Construction Methodology', 'Methodology', 'Modular prefabrication with rapid on-site installation', NULL),

(12, 'School Construction Experience', 'Experience', '4 school buildings with natural lighting and ventilation design', NULL),
(12, 'Construction Equipment', 'Equipment', 'In-house architecture team, concrete mixing, and steel fabrication', NULL),
(12, 'Quality Management System', 'QualityStandards', 'QMS with green building design principles', NULL),
(12, 'Registered Engineers', 'Personnel', '2 structural engineers, 1 green building specialist', NULL),
(12, 'Construction Methodology', 'Methodology', 'In-situ concrete with passive solar design integration', NULL);

-- Criteria for Tender 9 bids (Under Evaluation - Mohale Dam Access)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(19, 'Road Construction Equipment', 'Equipment', '2x motor graders, 3x dump trucks, 1x compactor', NULL),
(19, 'Mountain Road Experience', 'Experience', '2 mountain road projects in Lesotho highlands', NULL),
(19, 'Quality Management System', 'QualityStandards', 'QMS with slope stability monitoring', NULL),
(19, 'Construction Methodology', 'Methodology', 'Cut-and-fill with reinforced earth retaining walls', NULL),

(20, 'Road Construction Equipment', 'Equipment', '3x motor graders, 4x dump trucks, 1x paver, 2x compactors', NULL),
(20, 'Mountain Road Experience', 'Experience', '4 mountain road projects including Sani Pass approach', NULL),
(20, 'Quality Management System', 'QualityStandards', 'Full QMS with geotechnical monitoring system', NULL),
(20, 'Construction Methodology', 'Methodology', 'Terraced construction with bio-engineered slope protection', NULL),

(21, 'Road Construction Equipment', 'Equipment', '2x motor graders, 2x dump trucks, 1x compactor', NULL),
(21, 'Mountain Road Experience', 'Experience', '3 highland road maintenance contracts completed', NULL),
(21, 'Quality Management System', 'QualityStandards', 'Standard quality assurance with erosion control measures', NULL),
(21, 'Construction Methodology', 'Methodology', 'Conventional grading with drainage improvements', NULL);

-- Criteria for Tender 11 bids (Awarded - Quthing Road Maintenance)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(25, 'Road Maintenance Equipment', 'Equipment', '2x patching units, 1x shoulder grader, 1x drainage cleaner', NULL),
(25, 'Maintenance Contracts Completed', 'Experience', '2 routine maintenance contracts in southern districts', NULL),
(25, 'Quality Management System', 'QualityStandards', 'QMS with periodic road condition assessment', NULL),
(25, 'Construction Methodology', 'Methodology', 'Scheduled maintenance with emergency response capability', NULL),

(26, 'Road Maintenance Equipment', 'Equipment', '3x patching units, 2x shoulder graders, 1x signage truck', NULL),
(26, 'Maintenance Contracts Completed', 'Experience', '4 maintenance contracts including Maseru urban roads', NULL),
(26, 'Quality Management System', 'QualityStandards', 'Full QMS with GPS-based road condition tracking', NULL),
(26, 'Construction Methodology', 'Methodology', 'Preventive maintenance program with asset management system', NULL),

(27, 'Road Maintenance Equipment', 'Equipment', '3x patching units, 2x graders, 1x drainage unit, 1x signage truck', NULL),
(27, 'Maintenance Contracts Completed', 'Experience', '5 maintenance contracts including Mafeteng-Quthing corridor (3-year)', NULL),
(27, 'ISO 9001 Certification', 'Certifications', 'ISO 9001:2015 Certified', NULL),
(27, 'Quality Management System', 'QualityStandards', 'Full QMS with continuous road performance monitoring', NULL),
(27, 'Construction Methodology', 'Methodology', 'Integrated pavement management with proactive intervention scheduling', NULL);

-- Criteria for Tender 12 bids (Awarded - Mokhotlong Airport Terminal)
INSERT INTO bid_technical_criteria (bid_id, criterion_name, criterion_type, criterion_value, evidence_document_path) VALUES
(28, 'Airport Construction Experience', 'Experience', '1 cargo terminal extension (Maseru 2024)', NULL),
(28, 'Aviation Equipment', 'Equipment', 'Standard construction equipment, no specialized aviation systems', NULL),
(28, 'Quality Management System', 'QualityStandards', 'QMS with civil aviation standards awareness', NULL),
(28, 'Construction Methodology', 'Methodology', 'Conventional steel-frame with cladding system', NULL),

(29, 'Airport Construction Experience', 'Experience', '2 aviation infrastructure projects (lighting and navigation aids)', NULL),
(29, 'Aviation Equipment', 'Equipment', 'ICAO-compliant runway lighting and navigation systems', NULL),
(29, 'ICAO Compliance', 'Certifications', 'ICAO Annex 14 compliant lighting systems', NULL),
(29, 'Quality Management System', 'QualityStandards', 'Full QMS with aviation safety standards integration', NULL),
(29, 'Construction Methodology', 'Methodology', 'Electrical-first approach with aviation systems integration', NULL),

(30, 'Airport Construction Experience', 'Experience', '1 airstrip upgrade (Mafeteng) and 1 terminal renovation', NULL),
(30, 'Aviation Equipment', 'Equipment', 'General construction with subcontracted aviation specialists', NULL),
(30, 'Quality Management System', 'QualityStandards', 'QMS with aviation subcontractor quality oversight', NULL),
(30, 'Construction Methodology', 'Methodology', 'Parallel construction: terminal and airside simultaneous delivery', NULL);

-- Award records for the 2 Awarded tenders
-- Tender 11: Bid 27 (Highland Roads, 3600000) wins with highest avg score
INSERT INTO awards (tender_id, winning_bid_id, awarded_value, justification, awarded_by, confirmation_document_path) VALUES
(11, 27, 3600000.00, 'Highland Roads Construction submitted the lowest bid with the shortest timeline and received the highest technical and weighted scores from all evaluation committee members. Clear best-value selection.', 2, 'awards/MPW-2026-0011-award-confirmation.pdf');

-- Tender 12: Bid 30 (Highland Roads, 9000000) wins with highest avg score
INSERT INTO awards (tender_id, winning_bid_id, awarded_value, justification, awarded_by, confirmation_document_path) VALUES
(12, 30, 9000000.00, 'Highland Roads Construction achieved the highest combined weighted score across all evaluators with competitive pricing and the shortest proposed timeline. Best value for the Ministry.', 1, 'awards/MPW-2026-0012-award-confirmation.pdf');

-- =====================================================================
-- END OF SCHEMA
-- =====================================================================
