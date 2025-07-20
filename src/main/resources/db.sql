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


CREATE TABLE employees (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    age INT NOT NULL,
    created_at DATETIME(6),
    full_name VARCHAR(255) NOT NULL,
    past_experience TEXT,
    status ENUM('ACTIVE', 'INACTIVE'),
    total_experience VARCHAR(255),
    created_by VARCHAR(255),
    FOREIGN KEY (created_by) REFERENCES users(username)
);
