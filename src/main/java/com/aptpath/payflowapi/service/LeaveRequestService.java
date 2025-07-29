package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.dto.LeaveRequestDTO;
import com.aptpath.payflowapi.dto.LeaveRequestResponseDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.LeaveRequest;
import com.aptpath.payflowapi.entity.LeaveRequest.LeaveStatus;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    
    // Default annual leave allocation (can be made configurable)
    private static final int DEFAULT_ANNUAL_LEAVE_DAYS = 12;

    @Transactional
    public LeaveRequestResponseDTO applyForLeave(LeaveRequestDTO requestDTO) {
        // Validate employee exists
        Optional<Employee> employeeOpt = employeeRepository.findById(requestDTO.getEmployeeId());
        if (!employeeOpt.isPresent()) {
            throw new RuntimeException("Employee not found with ID: " + requestDTO.getEmployeeId());
        }

        Employee employee = employeeOpt.get();
        
        // Validate dates
        validateLeaveDates(requestDTO.getStartDate(), requestDTO.getEndDate());
        
        // Check for overlapping leave requests
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
            requestDTO.getEmployeeId(), 
            requestDTO.getStartDate(), 
            requestDTO.getEndDate()
        );
        
        if (!overlappingLeaves.isEmpty()) {
            throw new RuntimeException("You already have a leave request for overlapping dates");
        }
        
        // Check leave balance
        validateLeaveBalance(requestDTO.getEmployeeId(), requestDTO.getLeaveYear(), requestDTO.getTotalDays());
        
        // Create leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
            .employeeId(requestDTO.getEmployeeId())
            .employeeName(requestDTO.getEmployeeName())
            .employeeEmail(requestDTO.getEmployeeEmail())
            .startDate(requestDTO.getStartDate())
            .endDate(requestDTO.getEndDate())
            .totalDays(requestDTO.getTotalDays())
            .reason(requestDTO.getReason())
            .leaveYear(requestDTO.getLeaveYear())
            .status(LeaveStatus.PENDING)
            .build();
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        return mapToResponseDTO(savedRequest);
    }

    public List<LeaveRequestResponseDTO> getEmployeeLeaveRequests(Integer employeeId) {
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return requests.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    public List<LeaveRequestResponseDTO> getAllPendingLeaveRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
        return requests.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponseDTO approveLeaveRequest(Long requestId, String approvedBy) {
        Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new RuntimeException("Leave request not found with ID: " + requestId);
        }

        LeaveRequest request = requestOpt.get();
        
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in pending status");
        }

        request.setStatus(LeaveStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(LocalDateTime.now());
        
        LeaveRequest savedRequest = leaveRequestRepository.save(request);
        return mapToResponseDTO(savedRequest);
    }

    @Transactional
    public LeaveRequestResponseDTO rejectLeaveRequest(Long requestId, String rejectedBy, String rejectionReason) {
        Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new RuntimeException("Leave request not found with ID: " + requestId);
        }

        LeaveRequest request = requestOpt.get();
        
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in pending status");
        }

        request.setStatus(LeaveStatus.REJECTED);
        request.setApprovedBy(rejectedBy);
        request.setApprovedAt(LocalDateTime.now());
        request.setRejectionReason(rejectionReason);
        
        LeaveRequest savedRequest = leaveRequestRepository.save(request);
        return mapToResponseDTO(savedRequest);
    }

    public int getLeaveBalance(Integer employeeId, Integer year) {
        Integer usedDays = leaveRequestRepository.getTotalApprovedDaysByEmployeeAndYear(employeeId, year);
        if (usedDays == null) {
            usedDays = 0;
        }
        return DEFAULT_ANNUAL_LEAVE_DAYS - usedDays;
    }

    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
        
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }
    }

    private void validateLeaveBalance(Integer employeeId, Integer year, Integer requestedDays) {
        int availableBalance = getLeaveBalance(employeeId, year);
        
        if (requestedDays > availableBalance) {
            throw new RuntimeException(
                String.format("Insufficient leave balance. Requested: %d days, Available: %d days", 
                    requestedDays, availableBalance)
            );
        }
    }

    private LeaveRequestResponseDTO mapToResponseDTO(LeaveRequest request) {
        return LeaveRequestResponseDTO.builder()
            .id(request.getId())
            .employeeId(request.getEmployeeId())
            .employeeName(request.getEmployeeName())
            .employeeEmail(request.getEmployeeEmail())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .totalDays(request.getTotalDays())
            .reason(request.getReason())
            .leaveYear(request.getLeaveYear())
            .status(request.getStatus())
            .approvedBy(request.getApprovedBy())
            .approvedAt(request.getApprovedAt())
            .rejectionReason(request.getRejectionReason())
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt())
            .build();
    }
}
