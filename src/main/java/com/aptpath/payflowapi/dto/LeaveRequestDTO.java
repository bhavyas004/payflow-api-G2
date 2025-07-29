package com.aptpath.payflowapi.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO {
    private Integer employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private Integer leaveYear;
}
