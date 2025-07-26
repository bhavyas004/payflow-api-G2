-- Users table
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    is_first_login TINYINT(1) DEFAULT 1,
    email VARCHAR(100),
    contact_number VARCHAR(15),
    reset_password_required TINYINT(1) DEFAULT 0
);

-- Employees table with proper structure for onboarding
CREATE TABLE employees (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    total_experience VARCHAR(255), -- Formatted string like "2 years 3 months"
    created_by VARCHAR(255) NOT NULL, -- Reference to users.username
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    FOREIGN KEY (created_by) REFERENCES users(username)
);

-- Experience table with foreign key to employees
CREATE TABLE experience (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    job_title VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_experience INT, -- Experience in months for this specific job
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Insert sample users for testing
INSERT IGNORE INTO users (username, password, role, email) 
VALUES 
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPHzZenDe', 'ADMIN', 'admin@payflow.com'),
('hr', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPHzZenDe', 'HR', 'hr@payflow.com'),
('manager', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPHzZenDe', 'MANAGER', 'manager@payflow.com');
