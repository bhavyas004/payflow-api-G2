package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.entity.LeaveRequest;
import com.aptpath.payflowapi.service.LeaveRequestService;
import com.aptpath.payflowapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private JwtUtil jwtUtil;

    // Apply for leave
    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody LeaveRequest leaveRequest) {
        try {
            LeaveRequest savedRequest = leaveRequestService.applyForLeave(leaveRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave request submitted successfully");
            response.put("data", savedRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get employee leave requests
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaveRequests(@PathVariable Integer employeeId) {
        try {
            List<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get employee leave requests with pagination
    @GetMapping("/employee/{employeeId}/paginated")
    public ResponseEntity<?> getEmployeeLeaveRequestsPaginated(
            @PathVariable Integer employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId, page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests.getContent());
            response.put("totalPages", requests.getTotalPages());
            response.put("totalElements", requests.getTotalElements());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get all leave requests with pagination
    @GetMapping("/all")
    public ResponseEntity<?> getAllLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LeaveRequest> requests = leaveRequestService.getAllLeaveRequests(page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests.getContent());
            response.put("totalPages", requests.getTotalPages());
            response.put("totalElements", requests.getTotalElements());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get leave requests by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getLeaveRequestsByStatus(@PathVariable String status) {
        try {
            List<LeaveRequest> requests = leaveRequestService.getLeaveRequestsByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get leave requests by status with pagination
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<?> getLeaveRequestsByStatusPaginated(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LeaveRequest> requests = leaveRequestService.getLeaveRequestsByStatus(status, page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests.getContent());
            response.put("totalPages", requests.getTotalPages());
            response.put("totalElements", requests.getTotalElements());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get employee leave requests by status
    @GetMapping("/employee/{employeeId}/status/{status}")
    public ResponseEntity<?> getEmployeeLeaveRequestsByStatus(
            @PathVariable Integer employeeId,
            @PathVariable String status) {
        try {
            List<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveRequestsByStatus(employeeId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get leave request by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLeaveRequestById(@PathVariable Long id) {
        try {
            LeaveRequest request = leaveRequestService.getLeaveRequestById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Approve leave request - FIX: Return boolean, don't assign to LeaveRequest
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        try {
            String managerUsername = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            String remarks = request.get("remarks");
            
            // Fix: Don't assign boolean to LeaveRequest variable
            boolean success = leaveRequestService.approveLeaveRequest(id, remarks, managerUsername);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Leave request approved successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Failed to approve leave request"));
            }
        } catch (Exception e) {
            System.err.println("Error in approveLeaveRequest: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Reject leave request - FIX: Return boolean, don't assign to LeaveRequest
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeaveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        try {
            String managerUsername = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            String remarks = request.get("remarks");
            
            // Fix: Don't assign boolean to LeaveRequest variable
            boolean success = leaveRequestService.rejectLeaveRequest(id, remarks, managerUsername);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Leave request rejected successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Failed to reject leave request"));
            }
        } catch (Exception e) {
            System.err.println("Error in rejectLeaveRequest: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Cancel leave request
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeaveRequest(
            @PathVariable Long id,
            @RequestParam Integer employeeId) {
        try {
            LeaveRequest cancelledRequest = leaveRequestService.cancelLeaveRequest(id, employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave request cancelled successfully");
            response.put("data", cancelledRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Delete leave request (employee can delete only pending requests)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeaveRequest(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long employeeId = jwtUtil.extractEmployeeId(jwtToken);
            
            if (employeeId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Employee ID not found in token"));
            }
            
            boolean deleted = leaveRequestService.deleteLeaveRequest(id, employeeId.intValue());
            
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Leave request deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Failed to delete leave request"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get leave balance
    // Removed duplicate mapping for /balance/{employeeId} to resolve ambiguous mapping error

    // Get leave statistics
    @GetMapping("/statistics")
    public ResponseEntity<?> getLeaveStatistics() {
        try {
            Map<String, Object> stats = leaveRequestService.getLeaveStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get manager team leave requests
    @GetMapping("/manager/team")
    public ResponseEntity<?> getManagerTeamLeaveRequests(
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        try {
            String managerUsername = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            List<LeaveRequest> requests = leaveRequestService.getManagerTeamLeaveRequests(managerUsername, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get manager team leave stats
    @GetMapping("/manager/team/stats")
    public ResponseEntity<?> getManagerTeamLeaveStats(@RequestHeader("Authorization") String token) {
        try {
            String managerUsername = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Map<String, Object> stats = leaveRequestService.getManagerTeamLeaveStats(managerUsername);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get employee leave balance with comprehensive data
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaveBalance(@PathVariable Integer employeeId, 
                                                   @RequestParam(defaultValue = "2025") Integer year) {
        try {
            Map<String, Object> leaveData = leaveRequestService.calculateEmployeeLeaveData(employeeId, year);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Leave balance retrieved successfully");
            response.put("data", leaveData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

        // New endpoint: Get paid/unpaid leave breakdown for a date range
        @GetMapping("/balance/{employeeId}/breakdown")
        public ResponseEntity<?> getLeaveBreakdown(@PathVariable Integer employeeId,
                                                  @RequestParam String startDate,
                                                  @RequestParam String endDate,
                                                  @RequestParam(defaultValue = "2025") Integer year) {
            try {
                Map<String, Object> breakdown = leaveRequestService.calculateLeaveBreakdown(employeeId, startDate, endDate, year);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Leave breakdown calculated successfully");
                response.put("data", breakdown);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            }
        }

    // Helper method to create error response
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
}