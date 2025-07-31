-- Add manager column to employee table
ALTER TABLE employee 
ADD COLUMN manager VARCHAR(255);

-- Add foreign key constraint to ensure manager exists in users table
ALTER TABLE employee 
ADD CONSTRAINT fk_employee_manager 
FOREIGN KEY (manager) REFERENCES users(username);

-- Create indexes for better performance
CREATE INDEX idx_employee_manager ON employee(manager);
