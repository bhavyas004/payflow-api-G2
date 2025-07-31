package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.entity.LeaveRequest;
import com.aptpath.payflowapi.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class LeaveRequestController {
    
    @Autowired
    private LeaveRequestService leaveRequestService;
    
    // Apply for leave
    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody LeaveRequest leaveRequest) {
        try {
            System.out.println("Received leave request for: " + leaveRequest.getEmployeeName());
            
            LeaveRequest savedRequest = leaveRequestService.applyForLeave(leaveRequest);
            
            return ResponseEntity.ok(createSuccessResponse("Leave request submitted successfully", savedRequest));
            
        } catch (Exception e) {
            System.err.println("Error in leave request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get employee's leave requests
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaveRequests(
            @PathVariable Integer employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (page >= 0 && size > 0) {
                Page<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId, page, size);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", requests.getContent());
                response.put("totalElements", requests.getTotalElements());
                response.put("totalPages", requests.getTotalPages());
                response.put("currentPage", requests.getNumber());
                return ResponseEntity.ok(response);
            } else {
                List<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId);
                return ResponseEntity.ok(createSuccessResponse("Employee leave requests retrieved", requests));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get all leave requests
    @GetMapping("/all")
    public ResponseEntity<?> getAllLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LeaveRequest> requests = leaveRequestService.getAllLeaveRequests(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests.getContent());
            response.put("totalElements", requests.getTotalElements());
            response.put("totalPages", requests.getTotalPages());
            response.put("currentPage", requests.getNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get leave requests by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getLeaveRequestsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer employeeId) {
        try {
            if (page >= 0 && size > 0) {
                Page<LeaveRequest> requests;
                if (employeeId != null) {
                    // Filter by both status and employeeId
                    requests = leaveRequestService.getEmployeeLeaveRequestsByStatus(employeeId, status, page, size);
                } else {
                    requests = leaveRequestService.getLeaveRequestsByStatus(status, page, size);
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", requests.getContent());
                response.put("totalElements", requests.getTotalElements());
                response.put("totalPages", requests.getTotalPages());
                response.put("currentPage", requests.getNumber());
                return ResponseEntity.ok(response);
            } else {
                List<LeaveRequest> requests;
                if (employeeId != null) {
                    requests = leaveRequestService.getEmployeeLeaveRequestsByStatus(employeeId, status);
                } else {
                    requests = leaveRequestService.getLeaveRequestsByStatus(status);
                }
                return ResponseEntity.ok(createSuccessResponse("Leave requests by status retrieved", requests));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Approve leave request
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        try {
            String approvedBy = requestBody.getOrDefault("approvedBy", "HR");
            String remarks = requestBody.getOrDefault("remarks", "");
            
            LeaveRequest updatedRequest = leaveRequestService.approveLeaveRequest(id, approvedBy, remarks);
            return ResponseEntity.ok(createSuccessResponse("Leave request approved successfully", updatedRequest));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Reject leave request
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeaveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        try {
            String rejectedBy = requestBody.getOrDefault("rejectedBy", "HR");
            String remarks = requestBody.getOrDefault("remarks", "");
            
            LeaveRequest updatedRequest = leaveRequestService.rejectLeaveRequest(id, rejectedBy, remarks);
            return ResponseEntity.ok(createSuccessResponse("Leave request rejected successfully", updatedRequest));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Cancel leave request
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeaveRequest(
            @PathVariable Long id,
            @RequestParam Integer employeeId) {
        try {
            LeaveRequest updatedRequest = leaveRequestService.cancelLeaveRequest(id, employeeId);
            return ResponseEntity.ok(createSuccessResponse("Leave request cancelled successfully", updatedRequest));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get leave balance
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<?> getLeaveBalance(
            @PathVariable Integer employeeId,
            @RequestParam(defaultValue = "2025") Integer year) {
        try {
            Map<String, Object> balance = leaveRequestService.getLeaveBalance(employeeId, year);
            return ResponseEntity.ok(createSuccessResponse("Leave balance retrieved", balance));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get leave statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getLeaveStatistics() {
        try {
            Map<String, Object> stats = leaveRequestService.getLeaveStatistics();
            return ResponseEntity.ok(createSuccessResponse("Leave statistics retrieved", stats));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get comprehensive leave calculations for specific employee
    @GetMapping("/calculations/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaveCalculations(
            @PathVariable Integer employeeId,
            @RequestParam(required = false) Integer year) {
        try {
            Map<String, Object> leaveData = leaveRequestService.calculateEmployeeLeaveData(employeeId, year);
            return ResponseEntity.ok(createSuccessResponse("Employee leave calculations retrieved", leaveData));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get leave calculations for all employees
    @GetMapping("/calculations/all-employees")
    public ResponseEntity<?> getAllEmployeesLeaveCalculations(
            @RequestParam(required = false) Integer year) {
        try {
            Map<String, Object> allEmployeesData = leaveRequestService.calculateAllEmployeesLeaveData(year);
            return ResponseEntity.ok(createSuccessResponse("All employees leave calculations retrieved", allEmployeesData));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Sync leave balances with actual leave requests (maintenance endpoint)
    @PostMapping("/sync-balances")
    public ResponseEntity<?> syncLeaveBalances(@RequestParam(required = false) Integer year) {
        try {
            leaveRequestService.syncLeaveBalances(year);
            return ResponseEntity.ok(createSuccessResponse("Leave balances synchronized successfully", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Leave Request Controller is working!");
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