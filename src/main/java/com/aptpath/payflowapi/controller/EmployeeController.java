package com.aptpath.payflowapi.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.service.EmployeeService;

@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/onboarding")
    public ResponseEntity<Map<String, Object>> onboardEmployee(
            @RequestBody EmployeeDTO employeeDTO,
            @RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix from token
            token = token.substring(7);
            
            // Create employee with all the onboarding data
            Employee employee = employeeService.createEmployee(employeeDTO, token);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.CREATED.value());
            response.put("message", "Employee onboarded successfully");
            response.put("data", Map.of(
                "id", employee.getId(),
                "fullName", employee.getFullName(),
                "age", employee.getAge(),
                "status", employee.getStatus().toString(),
                "totalExperience", employee.getTotalExperience(),
                "createdBy", employee.getCreatedBy().getUsername(),
                "createdAt", employee.getCreatedAt(),
                "experienceCount", employee.getExperiences().size()
            ));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("message", "Failed to onboard employee");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createEmployee(
            @RequestBody EmployeeDTO dto,
            @RequestHeader("Authorization") String token) {
        token = token.substring(7);
        Employee employee = employeeService.createEmployee(dto, token);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Employee created successfully");

        return ResponseEntity.ok(response);
    }
    @GetMapping("/stats")
    public Map<String, Long> getEmployeeStats() {
        long activeCount = employeeService.getEmployeeRepository().countByStatus("ACTIVE");
        long inactiveCount = employeeService.getEmployeeRepository().countByStatus("INACTIVE");
        Map<String, Long> result = new HashMap<>();
        result.put("ACTIVE", activeCount);
        result.put("INACTIVE", inactiveCount);
        return result;
    }

    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeService.getEmployeeRepository().findAll();
    }
}
