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
        try {
            // Find employee by email
            Optional<Employee> employeeOpt = employeeRepository.findByEmail(loginDTO.getUsername());
            
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Invalid credentials"));
            }
            
            Employee employee = employeeOpt.get();
            
            // Check if password is null
            if (employee.getPassword() == null) {
                // Set a default password for testing (you can change this)
                String defaultPassword = "password123";
                String encodedPassword = passwordEncoder.encode(defaultPassword);
                employee.setPassword(encodedPassword);
                employeeRepository.save(employee);
            }
            
            // Check password
            if (!passwordEncoder.matches(loginDTO.getPassword(), employee.getPassword())) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Invalid credentials"));
            }
            
            // Check if employee is active
            if (employee.getStatus() != Employee.Status.ACTIVE) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Employee account is inactive. Please contact HR."));
            }
            
            // Create JWT token with employee details
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "EMPLOYEE");
            claims.put("employeeId", employee.getId());
            claims.put("fullName", employee.getFullName());
            claims.put("status", employee.getStatus().toString());
            
            String token = jwtUtil.generateToken(employee.getEmail(), claims);
            
            if (token != null) {
                return ResponseEntity.ok(new AuthResponse(token, "Employee login successful"));
            } else {
                return ResponseEntity.badRequest().body(new AuthResponse(null, "Token generation failed"));
            }
            
        } catch (Exception e) {
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
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            // Call the service method with manager assignment
            Employee savedEmployee = employeeService.onboardEmployee(employeeDTO, employeeDTO.getManager());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee onboarded successfully");
            response.put("employee", savedEmployee);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
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
            
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Status cannot be empty\"}");
            }
            
            employeeService.updateStatusByName(fullName, status);
            return ResponseEntity.ok().body("{\"message\": \"Status updated successfully\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    } 
}


