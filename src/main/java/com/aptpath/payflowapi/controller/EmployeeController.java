package com.aptpath.payflowapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aptpath.payflowapi.dto.AuthResponse;
import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.dto.LoginDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.service.EmployeeService;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.mapper.EmployeeMapper;
import com.aptpath.payflowapi.util.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;
@RestController
@RequestMapping("/onboard-employee")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

// Update the login method in your EmployeeController

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> employeeLogin(@RequestBody LoginDTO loginDTO) {
        System.out.println("Login attempt for email: " + loginDTO.getUsername());
        
        try {
            // Find employee by email
            Optional<Employee> employeeOpt = employeeRepository.findByEmail(loginDTO.getUsername());
            
            if (!employeeOpt.isPresent()) {
                System.out.println("Employee not found with email: " + loginDTO.getUsername());
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Invalid credentials"));
            }
            
            Employee employee = employeeOpt.get();
            System.out.println("Found employee: " + employee.getFullName());
            
            // Check if password is null
            if (employee.getPassword() == null) {
                System.out.println("Employee has no password set. Setting default password.");
                // Set a default password for testing (you can change this)
                String defaultPassword = "password123";
                String encodedPassword = passwordEncoder.encode(defaultPassword);
                employee.setPassword(encodedPassword);
                employeeRepository.save(employee);
                System.out.println("Default password set for employee: " + employee.getEmail());
            }
            
            // Check password
            if (!passwordEncoder.matches(loginDTO.getPassword(), employee.getPassword())) {
                System.out.println("Password mismatch for employee: " + employee.getEmail());
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Invalid credentials"));
            }
            
            System.out.println("Password verified for employee: " + employee.getEmail());
            
            // Check if employee is active
            if (employee.getStatus() != Employee.Status.ACTIVE) {
                System.out.println("Employee is not active: " + employee.getStatus());
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Employee account is inactive. Please contact HR."));
            }
            
            // Create JWT token with employee details
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "EMPLOYEE");
            claims.put("employeeId", employee.getId());
            claims.put("fullName", employee.getFullName());
            claims.put("status", employee.getStatus().toString());
            
            System.out.println("Generating token for employee: " + employee.getEmail());
            String token = jwtUtil.generateToken(employee.getEmail(), claims);
            System.out.println("Generated token: " + (token != null ? "Success" : "Failed"));
            
            if (token != null) {
                return ResponseEntity.ok(new AuthResponse(token, "Employee login successful"));
            } else {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Token generation failed"));
            }
            
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new AuthResponse(null, "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<String> setEmployeePassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Employee not found");
            }
            
            Employee employee = employeeOpt.get();
            // Encode the password
            String encodedPassword = passwordEncoder.encode(password);
            employee.setPassword(encodedPassword);
            employeeRepository.save(employee);
            
            return ResponseEntity.ok("Password set successfully for: " + employee.getFullName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error setting password: " + e.getMessage());
        }
    }
    @PostMapping("/add")
    public Employee addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return employeeService.onboardEmployee(employeeDTO);
    }

    @GetMapping("/employees")
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{fullName}/status")
    public ResponseEntity<?> updateStatusByName(@PathVariable String fullName, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            System.out.println("Received request to update status for: " + fullName + " to: " + status);
            
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Status cannot be empty\"}");
            }
            
            employeeService.updateStatusByName(fullName, status);
            return ResponseEntity.ok().body("{\"message\": \"Status updated successfully\"}");
        } catch (RuntimeException e) {
            System.err.println("Error in updateStatusByName: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Unexpected error in updateStatusByName: " + e.getMessage());
            return ResponseEntity.internalServerError().body("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    } 
}


