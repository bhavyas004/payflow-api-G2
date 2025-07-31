-- Create leave_requests table if it doesn't exist
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
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
    approved_by VARCHAR(255),
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_employee_id (employee_id),
    INDEX idx_status (status),
    INDEX idx_leave_year (leave_year),
    INDEX idx_created_at (created_at)
);

-- Check if table was created
DESCRIBE leave_requests;
