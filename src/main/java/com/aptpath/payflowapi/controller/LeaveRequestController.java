package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.dto.LeaveRequestDTO;
import com.aptpath.payflowapi.dto.LeaveRequestResponseDTO;
import com.aptpath.payflowapi.service.LeaveRequestService;
import com.aptpath.payflowapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final JwtUtil jwtUtil;

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody LeaveRequestDTO requestDTO, 
                                         HttpServletRequest request) {
        try {
            // Extract employee ID from JWT token for security
            String token = extractTokenFromHeader(request);
            if (token != null) {
                String username = jwtUtil.extractUsername(token);
                Integer tokenEmployeeId = jwtUtil.extractClaim(token, claims -> claims.get("employeeId", Integer.class));
                
                // Ensure the employee can only apply for their own leave
                if (tokenEmployeeId != null && !tokenEmployeeId.equals(requestDTO.getEmployeeId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only apply for your own leave"));
                }
            }

            LeaveRequestResponseDTO response = leaveRequestService.applyForLeave(requestDTO);
            return ResponseEntity.ok(createSuccessResponse("Leave request submitted successfully", response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaveRequests(@PathVariable Integer employeeId,
                                                    HttpServletRequest request) {
        try {
            // Verify that the employee can only view their own requests
            String token = extractTokenFromHeader(request);
            if (token != null) {
                Integer tokenEmployeeId = jwtUtil.extractClaim(token, claims -> claims.get("employeeId", Integer.class));
                
                if (tokenEmployeeId != null && !tokenEmployeeId.equals(employeeId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only view your own leave requests"));
                }
            }

            List<LeaveRequestResponseDTO> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId);
            return ResponseEntity.ok(createSuccessResponse("Leave requests retrieved successfully", requests));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingLeaveRequests() {
        try {
            List<LeaveRequestResponseDTO> requests = leaveRequestService.getAllPendingLeaveRequests();
            return ResponseEntity.ok(createSuccessResponse("Pending leave requests retrieved successfully", requests));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<?> approveLeaveRequest(@PathVariable Long requestId,
                                               HttpServletRequest request) {
        try {
            String token = extractTokenFromHeader(request);
            String approvedBy = "Unknown";
            
            if (token != null) {
                approvedBy = jwtUtil.extractUsername(token);
            }

            LeaveRequestResponseDTO response = leaveRequestService.approveLeaveRequest(requestId, approvedBy);
            return ResponseEntity.ok(createSuccessResponse("Leave request approved successfully", response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectLeaveRequest(@PathVariable Long requestId,
                                              @RequestBody Map<String, String> rejectionData,
                                              HttpServletRequest request) {
        try {
            String token = extractTokenFromHeader(request);
            String rejectedBy = "Unknown";
            
            if (token != null) {
                rejectedBy = jwtUtil.extractUsername(token);
            }

            String rejectionReason = rejectionData.get("rejectionReason");
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Rejection reason is required"));
            }

            LeaveRequestResponseDTO response = leaveRequestService.rejectLeaveRequest(requestId, rejectedBy, rejectionReason);
            return ResponseEntity.ok(createSuccessResponse("Leave request rejected successfully", response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/balance/{employeeId}/{year}")
    public ResponseEntity<?> getLeaveBalance(@PathVariable Integer employeeId,
                                           @PathVariable Integer year,
                                           HttpServletRequest request) {
        try {
            // Verify that the employee can only view their own balance
            String token = extractTokenFromHeader(request);
            if (token != null) {
                Integer tokenEmployeeId = jwtUtil.extractClaim(token, claims -> claims.get("employeeId", Integer.class));
                
                if (tokenEmployeeId != null && !tokenEmployeeId.equals(employeeId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("You can only view your own leave balance"));
                }
            }

            int balance = leaveRequestService.getLeaveBalance(employeeId, year);
            
            Map<String, Object> balanceData = new HashMap<>();
            balanceData.put("employeeId", employeeId);
            balanceData.put("year", year);
            balanceData.put("remainingDays", balance);
            balanceData.put("totalDays", 12); // Default annual leave
            balanceData.put("usedDays", 12 - balance);
            
            return ResponseEntity.ok(createSuccessResponse("Leave balance retrieved successfully", balanceData));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(e.getMessage()));
        }
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
