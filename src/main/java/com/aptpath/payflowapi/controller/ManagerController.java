package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/managers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ManagerController {
    
    @Autowired
    private ManagerService managerService;
    
    // Get all available managers for dropdown
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableManagers() {
        try {
            List<Map<String, Object>> managers = managerService.getAvailableManagers();
            return ResponseEntity.ok(createSuccessResponse("Available managers retrieved", managers));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Assign manager to employee
    @PostMapping("/assign")
    public ResponseEntity<?> assignManager(
            @RequestParam String employeeUsername,
            @RequestParam String managerUsername) {
        try {
            boolean success = managerService.assignManagerToEmployee(employeeUsername, managerUsername);
            if (success) {
                return ResponseEntity.ok(createSuccessResponse("Manager assigned successfully", null));
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Failed to assign manager"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get employees managed by a specific manager
    @GetMapping("/{managerUsername}/employees")
    public ResponseEntity<?> getEmployeesByManager(@PathVariable String managerUsername) {
        try {
            List<Map<String, Object>> employees = managerService.getEmployeesByManager(managerUsername);
            return ResponseEntity.ok(createSuccessResponse("Employees retrieved", employees));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get manager details for an employee
    @GetMapping("/employee/{employeeUsername}")
    public ResponseEntity<?> getEmployeeManager(@PathVariable String employeeUsername) {
        try {
            Map<String, Object> manager = managerService.getEmployeeManager(employeeUsername);
            if (manager != null) {
                return ResponseEntity.ok(createSuccessResponse("Manager details retrieved", manager));
            } else {
                return ResponseEntity.ok(createSuccessResponse("No manager assigned", null));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Auto-assign manager (used during onboarding)
    @PostMapping("/auto-assign")
    public ResponseEntity<?> autoAssignManager(
            @RequestParam String employeeUsername,
            @RequestParam String onboardingByUsername) {
        try {
            String assignedManager = managerService.autoAssignManager(onboardingByUsername);
            
            if (assignedManager != null) {
                boolean success = managerService.assignManagerToEmployee(employeeUsername, assignedManager);
                if (success) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("assignedManager", assignedManager);
                    return ResponseEntity.ok(createSuccessResponse("Manager auto-assigned successfully", result));
                }
            }
            
            return ResponseEntity.ok(createSuccessResponse("No manager auto-assigned", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Manager Controller is working!");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    // Utility methods
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }
}
