package com.aptpath.payflowapi.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.aptpath.payflowapi.entity.LeaveRequest.LeaveStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponseDTO {
    private Long id;
    private Integer employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private Integer leaveYear;
    private LeaveStatus status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
