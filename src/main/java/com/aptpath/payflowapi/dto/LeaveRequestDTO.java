package com.aptpath.payflowapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class LeaveRequestDTO {
    
    @NotNull(message = "Employee ID is required")
    private Integer employeeId;
    
    @NotBlank(message = "Employee name is required")
    private String employeeName;
    
    @NotBlank(message = "Employee email is required")
    @Email(message = "Valid email is required")
    private String employeeEmail;
    
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @NotNull(message = "Total days is required")
    @Min(value = 1, message = "Total days must be at least 1")
    private Integer totalDays;
    
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 1000, message = "Reason must be between 10 and 1000 characters")
    private String reason;
    
    @NotNull(message = "Leave year is required")
    private Integer leaveYear;
    
    // Constructors
    public LeaveRequestDTO() {}
    
    // Getters and Setters
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Integer getLeaveYear() { return leaveYear; }
    public void setLeaveYear(Integer leaveYear) { this.leaveYear = leaveYear; }
}