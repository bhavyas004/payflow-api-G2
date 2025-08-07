-- Script to fix the ctc_details table structure
-- This will drop the existing table and recreate it with the simplified structure

-- Drop the existing table (backup data first if needed)
DROP TABLE IF EXISTS ctc_details;

-- Create the simplified ctc_details table
CREATE TABLE ctc_details (
    ctc_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    effective_from DATE NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    hra DECIMAL(12,2) DEFAULT 0.00,
    allowances DECIMAL(12,2) DEFAULT 0.00,
    bonuses DECIMAL(12,2) DEFAULT 0.00,
    pf_contribution DECIMAL(12,2) DEFAULT 0.00,
    gratuity DECIMAL(12,2) DEFAULT 0.00,
    total_ctc DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) DEFAULT NULL,
    INDEX idx_employee_effective (employee_id, effective_from)
);

-- Verify the table structure
DESCRIBE ctc_details;
