package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id")
    private Integer employeeId;
    
    @Column(name = "employee_name")
    private String employeeName;
    
    @Column(name = "employee_email")
    private String employeeEmail;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "total_days")
    private Integer totalDays;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "status")
    private String status = "PENDING";
    
    @Column(name = "applied_date")
    private LocalDateTime appliedDate;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "leave_year")
    private Integer leaveYear;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        appliedDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public LeaveRequest() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate = appliedDate; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public Integer getLeaveYear() { return leaveYear; }
    public void setLeaveYear(Integer leaveYear) { this.leaveYear = leaveYear; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}