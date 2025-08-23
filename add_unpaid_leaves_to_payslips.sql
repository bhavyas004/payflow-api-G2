-- Add unpaid_leaves and unpaid_leave_deduction columns to payslips table
ALTER TABLE payslips ADD COLUMN unpaid_leaves INT DEFAULT 0;
ALTER TABLE payslips ADD COLUMN unpaid_leave_deduction DECIMAL(12,2) DEFAULT 0.00;
