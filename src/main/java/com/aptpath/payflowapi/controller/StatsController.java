package com.aptpath.payflowapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aptpath.payflowapi.repository.UserRepository;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.entity.Employee;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class StatsController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/users/total")
    public ResponseEntity<Map<String, Long>> getTotalUsersCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalUsers", userRepository.countTotalUsers());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/hr")
    public ResponseEntity<Map<String, Long>> getHRCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalHRs", userRepository.countByRoleIgnoreCase("HR"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/managers")
    public ResponseEntity<Map<String, Long>> getManagersCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalManagers", userRepository.countByRoleIgnoreCase("MANAGER"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/total")
    public ResponseEntity<Map<String, Long>> getTotalEmployeesCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalEmployees", employeeRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/active")
    public ResponseEntity<Map<String, Long>> getActiveEmployeesCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalActiveEmployees", employeeRepository.countByStatus(Employee.Status.ACTIVE));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/inactive")
    public ResponseEntity<Map<String, Long>> getInactiveEmployeesCount() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalInactiveEmployees", employeeRepository.countByStatus(Employee.Status.INACTIVE));
        return ResponseEntity.ok(response);
    }
}