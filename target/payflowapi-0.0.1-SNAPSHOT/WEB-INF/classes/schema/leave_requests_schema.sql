-- Create leave_requests table
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    employee_name VARCHAR(255) NOT NULL,
    employee_email VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT NOT NULL,
    leave_year INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(255),
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    INDEX idx_employee_id (employee_id),
    INDEX idx_status (status),
    INDEX idx_leave_year (leave_year),
    INDEX idx_dates (start_date, end_date)
);

-- Insert sample data (optional)
-- INSERT INTO leave_requests (employee_id, employee_name, employee_email, start_date, end_date, total_days, reason, leave_year, status)
-- VALUES 
-- (1, 'John Doe', 'john.doe@company.com', '2025-08-15', '2025-08-17', 3, 'Personal work', 2025, 'PENDING'),
-- (1, 'John Doe', 'john.doe@company.com', '2025-09-01', '2025-09-05', 5, 'Vacation', 2025, 'APPROVED');
