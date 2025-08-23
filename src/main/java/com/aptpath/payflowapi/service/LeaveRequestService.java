package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.LeaveBalance;
import com.aptpath.payflowapi.entity.LeaveRequest;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.LeaveBalanceRepository;
import com.aptpath.payflowapi.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class LeaveRequestService {
    // New service: Calculate paid/unpaid leave breakdown for a date range
    public Map<String, Object> calculateLeaveBreakdown(Integer employeeId, String startDateStr, String endDateStr, Integer year) {
        Map<String, Object> result = new HashMap<>();
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        int totalDays = calculateWorkingDays(startDate, endDate);
        // Get leave balance for the year
        LeaveBalance leaveBalance = getOrCreateLeaveBalance(employeeId, "Employee", year);
        int paidLeaveQuota = leaveBalance.getTotalLeavesPerYear();
        int usedLeaves = leaveBalance.getUsedLeaves();
        int paidDaysLeft = Math.max(0, paidLeaveQuota - usedLeaves);
        int paidDays = 0;
        int unpaidDays = totalDays;
        if (paidDaysLeft <= 0) {
            // No paid leave left, all requested days are unpaid
            paidDays = 0;
            unpaidDays = totalDays;
        } else if (totalDays > paidDaysLeft) {
            // Requested days exceed paid leave left
            paidDays = paidDaysLeft;
            unpaidDays = totalDays - paidDaysLeft;
        } else {
            // All requested days are paid
            paidDays = totalDays;
            unpaidDays = 0;
        }
        result.put("paidDays", paidDays);
        result.put("unpaidDays", unpaidDays);
        result.put("totalDays", totalDays);
        return result;
    }
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;
    
    @Autowired
    private ManagerService managerService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // Apply for leave
    public LeaveRequest applyForLeave(LeaveRequest leaveRequest) {
        validateLeaveRequest(leaveRequest);
        // Exclude weekends from leave days calculation
        int workingDays = calculateWorkingDays(leaveRequest.getStartDate(), leaveRequest.getEndDate());
        leaveRequest.setTotalDays(workingDays);
        // Ensure employee has a leave balance record
        LeaveBalance leaveBalance = getOrCreateLeaveBalance(
            leaveRequest.getEmployeeId(),
            leaveRequest.getEmployeeName(),
            leaveRequest.getLeaveYear()
        );
        // Check for overlapping leaves
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
            leaveRequest.getEmployeeId(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate()
        );
        if (!overlappingLeaves.isEmpty()) {
            throw new RuntimeException("You already have approved leave during this period");
        }
        int paidLeaveQuota = leaveBalance.getTotalLeavesPerYear();
        // Sum all approved paid leave days for the year
        int totalApprovedPaidLeaveDays = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(
            leaveRequest.getEmployeeId(), "APPROVED")
            .stream()
            .filter(req -> req.getLeaveYear().equals(leaveRequest.getLeaveYear()))
            .mapToInt(LeaveRequest::getPaidDays)
            .sum();
        int paidDaysLeft = Math.max(0, paidLeaveQuota - totalApprovedPaidLeaveDays);
        // --- Paid/Unpaid leave categorization based on remainingLeaves ---
        int remainingLeaves = leaveBalance.getRemainingLeaves();
        int paidDays = 0;
        int unpaidDays = leaveRequest.getTotalDays();
        if (remainingLeaves <= 0) {
            paidDays = 0;
            unpaidDays = leaveRequest.getTotalDays();
            leaveRequest.setLeaveType("UNPAID");
        } else if (leaveRequest.getTotalDays() > remainingLeaves) {
            paidDays = remainingLeaves;
            unpaidDays = leaveRequest.getTotalDays() - remainingLeaves;
            leaveRequest.setLeaveType("MIXED");
        } else {
            paidDays = leaveRequest.getTotalDays();
            unpaidDays = 0;
            leaveRequest.setLeaveType("PAID");
        }
        leaveRequest.setPaidDays(paidDays);
        leaveRequest.setUnpaidDays(unpaidDays);
        leaveRequest.setStatus("PENDING");
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        // Update leave balance to reflect pending leaves
        Integer employeeId = leaveRequest.getEmployeeId();
        Integer year = leaveRequest.getLeaveYear();
        // Calculate actual used and pending leaves
        List<LeaveRequest> approvedRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "APPROVED");
        int approvedPaidDays = approvedRequests.stream()
            .filter(req -> req.getLeaveYear().equals(year))
            .mapToInt(LeaveRequest::getPaidDays)
            .sum();
        int approvedTotalDays = approvedRequests.stream()
            .filter(req -> req.getLeaveYear().equals(year))
            .mapToInt(LeaveRequest::getTotalDays)
            .sum();
        int actualUsedLeaves = approvedTotalDays;
        int unpaidLeaves = Math.max(0, actualUsedLeaves - paidLeaveQuota);
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "PENDING");
        int pendingDays = pendingRequests.stream()
            .filter(req -> req.getLeaveYear().equals(year))
            .mapToInt(LeaveRequest::getTotalDays)
            .sum();
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year);
        LeaveBalance balance = balanceOpt.orElseGet(() -> getOrCreateLeaveBalance(employeeId, leaveRequest.getEmployeeName(), year));
        balance.setUsedLeaves(actualUsedLeaves);
        balance.setPendingLeaves(pendingDays);
        balance.setUnpaidLeaves(unpaidLeaves); // Always calculate as excess over quota
        balance.setRemainingLeaves(Math.max(0, paidLeaveQuota - approvedPaidDays));
        leaveBalanceRepository.save(balance);
        return savedRequest;
    }

    // Helper to calculate working days (excluding weekends)
    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
    
    // Get or create leave balance for employee
    private LeaveBalance getOrCreateLeaveBalance(Integer employeeId, String employeeName, Integer year) {
        Optional<LeaveBalance> existingBalance = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year);
        
        if (existingBalance.isPresent()) {
            return existingBalance.get();
        } else {
            // Create new leave balance for the year
            LeaveBalance newBalance = new LeaveBalance(employeeId, employeeName, year);
            return leaveBalanceRepository.save(newBalance);
        }
    }
    
    // Get employee leave requests
    public List<LeaveRequest> getEmployeeLeaveRequests(Integer employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }
    
    public Page<LeaveRequest> getEmployeeLeaveRequests(Integer employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }
    
    // Get all leave requests
    public Page<LeaveRequest> getAllLeaveRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    // Get leave requests by status
    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        return leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public Page<LeaveRequest> getLeaveRequestsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
    
    // Get employee leave requests by status
    public List<LeaveRequest> getEmployeeLeaveRequestsByStatus(Integer employeeId, String status) {
        return leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, status);
    }
    
    public Page<LeaveRequest> getEmployeeLeaveRequestsByStatus(Integer employeeId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, status, pageable);
    }
    
    // Get leave request by ID
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found with ID: " + id));
    }
    
    // Approve leave request
    public boolean approveLeaveRequest(Long requestId, String remarks, String approvedBy) {
        try {
            Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new RuntimeException("Leave request not found");
            }
            LeaveRequest request = requestOpt.get();
            if (!"PENDING".equals(request.getStatus())) {
                throw new RuntimeException("Leave request is not in pending status");
            }
            // Calculate paid leave quota based on total approved paid leave days
            LeaveBalance leaveBalance = getOrCreateLeaveBalance(request.getEmployeeId(), request.getEmployeeName(), request.getLeaveYear());
            int paidLeaveQuota = leaveBalance.getTotalLeavesPerYear();
            int totalApprovedPaidLeaveDays = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(
                request.getEmployeeId(), "APPROVED")
                .stream()
                .filter(req -> req.getLeaveYear().equals(request.getLeaveYear()) && "PAID".equals(req.getLeaveType()))
                .mapToInt(LeaveRequest::getPaidDays)
                .sum();
            int paidDaysLeft = Math.max(0, paidLeaveQuota - totalApprovedPaidLeaveDays);
            int paidDays = 0;
            int unpaidDays = request.getTotalDays();
                // Explicit logic for categorization
                // --- Paid/Unpaid leave categorization based on remainingLeaves ---
                int remainingLeaves = leaveBalance.getRemainingLeaves();
                if (remainingLeaves <= 0) {
                    // No paid leave left, all days are unpaid
                    paidDays = 0;
                    unpaidDays = request.getTotalDays();
                    request.setLeaveType("UNPAID");
                } else if (request.getTotalDays() > remainingLeaves) {
                    // Some paid, some unpaid
                    paidDays = remainingLeaves;
                    unpaidDays = request.getTotalDays() - remainingLeaves;
                    request.setLeaveType("MIXED");
                } else {
                    // All paid
                    paidDays = request.getTotalDays();
                    unpaidDays = 0;
                    request.setLeaveType("PAID");
                }
            request.setPaidDays(paidDays);
            request.setUnpaidDays(unpaidDays);
            // Update request status
            request.setStatus("APPROVED");
            request.setApprovedBy(approvedBy);
            request.setApprovedDate(LocalDateTime.now());
            request.setRemarks(remarks);
            leaveRequestRepository.save(request);
            // Update leave balance in DB
            updateLeaveBalance(request.getEmployeeId(), request.getLeaveYear(), request.getTotalDays());
            // Send approval email to employee
            try {
                Optional<Employee> employeeOpt = employeeRepository.findByEmail(request.getEmployeeEmail());
                if (employeeOpt.isPresent()) {
                    Employee employee = employeeOpt.get();
                    emailService.sendLeaveApprovalEmail(
                        request.getEmployeeEmail(),
                        employee.getFullName(),
                        request.getStartDate().toString(),
                        request.getEndDate().toString(),
                        remarks
                    );
                }
            } catch (Exception emailError) {
                System.err.println("Failed to send approval email: " + emailError.getMessage());
                // Don't fail the approval process if email fails
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error approving leave request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to approve leave request: " + e.getMessage());
        }
    }

    // Reject leave request
    public boolean rejectLeaveRequest(Long requestId, String remarks, String rejectedBy) {
        try {
            Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new RuntimeException("Leave request not found");
            }
            
            LeaveRequest request = requestOpt.get();
            
            if (!"PENDING".equals(request.getStatus())) {
                throw new RuntimeException("Leave request is not in pending status");
            }
            
            // Update request status
            request.setStatus("REJECTED");
            request.setRejectedBy(rejectedBy);
            request.setRejectedAt(LocalDateTime.now());
            request.setRemarks(remarks);
            
            leaveRequestRepository.save(request);
            
            // Send rejection email to employee
            try {
                Optional<Employee> employeeOpt = employeeRepository.findByEmail(request.getEmployeeEmail());
                if (employeeOpt.isPresent()) {
                    Employee employee = employeeOpt.get();
                    emailService.sendLeaveRejectionEmail(
                        request.getEmployeeEmail(),
                        employee.getFullName(),
                        request.getStartDate().toString(),
                        request.getEndDate().toString(),
                        remarks
                    );
                }
            } catch (Exception emailError) {
                System.err.println("Failed to send rejection email: " + emailError.getMessage());
                // Don't fail the rejection process if email fails
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error rejecting leave request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to reject leave request: " + e.getMessage());
        }
    }
    
    // Cancel leave request
    public LeaveRequest cancelLeaveRequest(Long id, Integer employeeId) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        
        if (!leaveRequest.getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("You can only cancel your own leave requests");
        }
        
        if ("APPROVED".equals(leaveRequest.getStatus()) && 
            leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel leave that has already started");
        }
        
        if ("REJECTED".equals(leaveRequest.getStatus()) || 
            "CANCELLED".equals(leaveRequest.getStatus())) {
            throw new RuntimeException("Leave request is already " + leaveRequest.getStatus().toLowerCase());
        }
        
        // If it was approved, restore the leave balance
        if ("APPROVED".equals(leaveRequest.getStatus())) {
            restoreLeaveBalance(leaveRequest.getEmployeeId(), leaveRequest.getLeaveYear(), leaveRequest.getTotalDays());
        }
        
        leaveRequest.setStatus("CANCELLED");
        leaveRequest.setApprovedDate(LocalDateTime.now());
        
        return leaveRequestRepository.save(leaveRequest);
    }
    
    // Delete leave request (only pending requests can be deleted)
    public boolean deleteLeaveRequest(Long id, Integer employeeId) {
        try {
            LeaveRequest leaveRequest = getLeaveRequestById(id);
            
            // Only the employee who created the request can delete it
            if (!leaveRequest.getEmployeeId().equals(employeeId)) {
                throw new RuntimeException("You can only delete your own leave requests");
            }
            
            // Only pending requests can be deleted
            if (!"PENDING".equals(leaveRequest.getStatus())) {
                throw new RuntimeException("Only pending leave requests can be deleted. Current status: " + leaveRequest.getStatus());
            }
            
            // Delete the leave request
            leaveRequestRepository.delete(leaveRequest);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error deleting leave request: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete leave request: " + e.getMessage());
        }
    }
    
    // Update leave balance when leave is approved
    private void updateLeaveBalance(Integer employeeId, Integer year, Integer days) {
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year);
        List<LeaveRequest> approvedRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "APPROVED");
        int approvedPaidDays = approvedRequests.stream()
            .filter(req -> req.getLeaveYear().equals(year))
            .mapToInt(LeaveRequest::getPaidDays)
            .sum();
        int approvedUnpaidDays = approvedRequests.stream()
            .filter(req -> req.getLeaveYear().equals(year))
            .mapToInt(LeaveRequest::getUnpaidDays)
            .sum();
        int actualUsedLeaves = approvedPaidDays + approvedUnpaidDays;
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "PENDING");
        int pendingDays = pendingRequests.stream()
            .filter(req -> req.getLeaveYear().equals(year))
            .mapToInt(LeaveRequest::getTotalDays)
            .sum();
        LeaveBalance balance;
        if (balanceOpt.isPresent()) {
            balance = balanceOpt.get();
        } else {
            String employeeName = "";
            Optional<LeaveRequest> latestApproved = leaveRequestRepository.findTopByEmployeeIdAndStatusOrderByApprovedDateDesc(employeeId, "APPROVED");
            if (latestApproved.isPresent()) {
                employeeName = latestApproved.get().getEmployeeName();
            }
            balance = new LeaveBalance();
            balance.setEmployeeId(employeeId);
            balance.setEmployeeName(employeeName);
            balance.setLeaveYear(year);
            balance.setTotalLeavesPerYear(12); // Default quota
        }
        balance.setUsedLeaves(actualUsedLeaves);
        balance.setUnpaidLeaves(approvedUnpaidDays);
        balance.setPendingLeaves(pendingDays);
        balance.setRemainingLeaves(Math.max(0, balance.getTotalLeavesPerYear() - approvedPaidDays));
        leaveBalanceRepository.save(balance);
    }
    
    // Restore leave balance when approved leave is cancelled
    private void restoreLeaveBalance(Integer employeeId, Integer year, Integer days) {
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year);
        
        if (balanceOpt.isPresent()) {
            LeaveBalance balance = balanceOpt.get();
            // Recalculate all totals to ensure accuracy
            int totalUnpaidLeavesForYear = leaveRequestRepository.getTotalUnpaidLeaveDaysForYear(employeeId, year);
            int paidLeaves = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "APPROVED")
                .stream()
                .filter(req -> req.getLeaveYear().equals(year) && "PAID".equals(req.getLeaveType()))
                .mapToInt(LeaveRequest::getTotalDays)
                .sum();
            int totalUsedLeaves = paidLeaves + totalUnpaidLeavesForYear;
            
            balance.setUsedLeaves(totalUsedLeaves);
            balance.setUnpaidLeaves(totalUnpaidLeavesForYear);
            balance.setRemainingLeaves(balance.getTotalLeavesPerYear() - totalUsedLeaves);
            leaveBalanceRepository.save(balance);
        }
    }
    
    // Get leave balance
    public Map<String, Object> getLeaveBalance(Integer employeeId, Integer year) {
        LeaveBalance balance = getOrCreateLeaveBalance(employeeId, "Employee", year);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", balance.getTotalLeavesPerYear());
        result.put("used", balance.getUsedLeaves());
        result.put("remaining", balance.getRemainingLeaves());
        result.put("year", balance.getLeaveYear());
        
        return result;
    }
    
    // Get leave statistics
    public Map<String, Object> getLeaveStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPending", leaveRequestRepository.countByStatus("PENDING"));
        stats.put("totalApproved", leaveRequestRepository.countByStatus("APPROVED"));
        stats.put("totalRejected", leaveRequestRepository.countByStatus("REJECTED"));
        stats.put("totalCancelled", leaveRequestRepository.countByStatus("CANCELLED"));
        
        return stats;
    }
    
    // Calculate comprehensive leave data for an employee
    public Map<String, Object> calculateEmployeeLeaveData(Integer employeeId, Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        final Integer finalYear = year;
        
        Map<String, Object> leaveData = new HashMap<>();
        
        // Get or create leave balance record
        LeaveBalance leaveBalance = getOrCreateLeaveBalance(employeeId, "Employee " + employeeId, finalYear);
        
        // Calculate actual used leaves from approved leave requests
        List<LeaveRequest> approvedRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "APPROVED");
        Integer actualUsedLeaves = approvedRequests.stream()
                .filter(req -> req.getLeaveYear().equals(finalYear))
                .mapToInt(LeaveRequest::getTotalDays)
                .sum();
        
        // Calculate pending leaves
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "PENDING");
        Integer pendingLeaves = pendingRequests.stream()
                .filter(req -> req.getLeaveYear().equals(finalYear))
                .mapToInt(LeaveRequest::getTotalDays)
                .sum();
        
        // Update leave balance if it's out of sync
        // Also calculate unpaid leaves for accurate balance
        int totalUnpaidLeavesForYear = leaveRequestRepository.getTotalUnpaidLeaveDaysForYear(employeeId, finalYear);
        
        if (!actualUsedLeaves.equals(leaveBalance.getUsedLeaves()) || 
            !Integer.valueOf(totalUnpaidLeavesForYear).equals(leaveBalance.getUnpaidLeaves())) {
            leaveBalance.setUsedLeaves(actualUsedLeaves);
            leaveBalance.setUnpaidLeaves(totalUnpaidLeavesForYear);
            leaveBalance.setRemainingLeaves(leaveBalance.getTotalLeavesPerYear() - actualUsedLeaves);
            leaveBalanceRepository.save(leaveBalance);
        }
        
        // Calculate remaining leaves (only deduct approved leaves, not pending)
        int remainingLeaves = leaveBalance.getTotalLeavesPerYear() - actualUsedLeaves;
        
        // Prepare response data
        leaveData.put("totalLeavesPerYear", leaveBalance.getTotalLeavesPerYear());
        leaveData.put("usedLeaves", actualUsedLeaves);
        leaveData.put("unpaidLeaves", leaveBalance.getUnpaidLeaves());
        leaveData.put("pendingLeaves", pendingLeaves);
        leaveData.put("remainingLeaves", Math.max(0, remainingLeaves));
        leaveData.put("leaveYear", finalYear);
        leaveData.put("employeeId", employeeId);
        leaveData.put("employeeName", leaveBalance.getEmployeeName());
        
        // Add additional statistics
        leaveData.put("totalRequests", getTotalLeaveRequests(employeeId, finalYear));
        leaveData.put("approvedRequests", getApprovedLeaveRequests(employeeId, finalYear));
        leaveData.put("pendingRequests", getPendingLeaveRequests(employeeId, finalYear));
        leaveData.put("rejectedRequests", getRejectedLeaveRequests(employeeId, finalYear));
        
        return leaveData;
    }
    
    // Get leave calculations for all employees
    public Map<String, Object> calculateAllEmployeesLeaveData(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        Map<String, Object> allEmployeesData = new HashMap<>();
        List<LeaveBalance> allBalances = leaveBalanceRepository.findAll();
        
        for (LeaveBalance balance : allBalances) {
            if (balance.getLeaveYear().equals(year)) {
                Map<String, Object> employeeData = calculateEmployeeLeaveData(balance.getEmployeeId(), year);
                allEmployeesData.put("employee_" + balance.getEmployeeId(), employeeData);
            }
        }
        
        return allEmployeesData;
    }
    
    // Sync leave balances with actual leave requests (maintenance function)
    public void syncLeaveBalances(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        final Integer finalYear = year;
        
        List<LeaveBalance> allBalances = leaveBalanceRepository.findAll();
        
        for (LeaveBalance balance : allBalances) {
            if (balance.getLeaveYear().equals(finalYear)) {
                List<LeaveRequest> approvedRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(balance.getEmployeeId(), "APPROVED");
                Integer actualUsedLeaves = approvedRequests.stream()
                        .filter(req -> req.getLeaveYear().equals(finalYear))
                        .mapToInt(LeaveRequest::getTotalDays)
                        .sum();
                
                balance.setUsedLeaves(actualUsedLeaves);
                balance.setRemainingLeaves(balance.getTotalLeavesPerYear() - actualUsedLeaves);
                    // Also update unpaidLeaves field
                    int totalUnpaidLeavesForYear = leaveRequestRepository.getTotalUnpaidLeaveDaysForYear(balance.getEmployeeId(), finalYear);
                    balance.setUnpaidLeaves(totalUnpaidLeavesForYear);
                leaveBalanceRepository.save(balance);
            }
        }
    }
    
    // Helper methods for additional statistics
    private Long getTotalLeaveRequests(Integer employeeId, Integer year) {
        List<LeaveRequest> allRequests = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return allRequests.stream()
                .filter(req -> req.getLeaveYear().equals(year))
                .count();
    }
    
    private Long getApprovedLeaveRequests(Integer employeeId, Integer year) {
        List<LeaveRequest> approvedRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "APPROVED");
        return approvedRequests.stream()
                .filter(req -> req.getLeaveYear().equals(year))
                .count();
    }
    
    private Long getPendingLeaveRequests(Integer employeeId, Integer year) {
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "PENDING");
        return pendingRequests.stream()
                .filter(req -> req.getLeaveYear().equals(year))
                .count();
    }
    
    private Long getRejectedLeaveRequests(Integer employeeId, Integer year) {
        List<LeaveRequest> rejectedRequests = leaveRequestRepository.findByEmployeeIdAndStatusOrderByCreatedAtDesc(employeeId, "REJECTED");
        return rejectedRequests.stream()
                .filter(req -> req.getLeaveYear().equals(year))
                .count();
    }
    
    // Get manager team leave stats
    public Map<String, Object> getManagerTeamLeaveStats(String managerUsername) {
        try {
            // Get list of employees managed by this manager
            List<String> teamEmails = managerService.getTeamMemberEmails(managerUsername);
            
            Map<String, Object> stats = new HashMap<>();
            
            if (teamEmails.isEmpty()) {
                // No team members, return zeros
                stats.put("PENDING", 0L);
                stats.put("APPROVED", 0L);
                stats.put("REJECTED", 0L);
                stats.put("CANCELLED", 0L);
            } else {
                // Count leave requests by status for team members
                stats.put("PENDING", leaveRequestRepository.countByEmployeeEmailInAndStatus(teamEmails, "PENDING"));
                stats.put("APPROVED", leaveRequestRepository.countByEmployeeEmailInAndStatus(teamEmails, "APPROVED"));
                stats.put("REJECTED", leaveRequestRepository.countByEmployeeEmailInAndStatus(teamEmails, "REJECTED"));
                stats.put("CANCELLED", leaveRequestRepository.countByEmployeeEmailInAndStatus(teamEmails, "CANCELLED"));
            }
            
            return stats;
        } catch (Exception e) {
            System.err.println("Error getting manager team leave stats: " + e.getMessage());
            // Return default stats in case of error
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("PENDING", 0L);
            defaultStats.put("APPROVED", 0L);
            defaultStats.put("REJECTED", 0L);
            defaultStats.put("CANCELLED", 0L);
            return defaultStats;
        }
    }
    
    // Get manager team leave requests
    public List<LeaveRequest> getManagerTeamLeaveRequests(String managerUsername, String status) {
        try {
            // Get list of employees managed by this manager
            List<String> teamEmails = managerService.getTeamMemberEmails(managerUsername);
            
            if (teamEmails.isEmpty()) {
                return new ArrayList<>();
            }
            
            if (status != null && !status.equals("ALL")) {
                return leaveRequestRepository.findByEmployeeEmailInAndStatusOrderByCreatedAtDesc(teamEmails, status);
            } else {
                return leaveRequestRepository.findByEmployeeEmailInOrderByCreatedAtDesc(teamEmails);
            }
        } catch (Exception e) {
            System.err.println("Error getting manager team leave requests: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // Validate leave request
    private void validateLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
        
        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }
        
        if (leaveRequest.getTotalDays() <= 0) {
            throw new RuntimeException("Total days must be greater than 0");
        }
        
        if (leaveRequest.getReason() == null || leaveRequest.getReason().trim().length() < 10) {
            throw new RuntimeException("Reason must be at least 10 characters long");
        }
    }
}