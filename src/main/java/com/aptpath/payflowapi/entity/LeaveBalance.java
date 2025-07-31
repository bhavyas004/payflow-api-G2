package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balance")
public class LeaveBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", unique = true)
    private Integer employeeId;
    
    @Column(name = "employee_name")
    private String employeeName;
    
    @Column(name = "total_leaves_per_year")
    private Integer totalLeavesPerYear = 12;
    
    @Column(name = "used_leaves")
    private Integer usedLeaves = 0;
    
    @Column(name = "remaining_leaves")
    private Integer remainingLeaves = 12;
    
    @Column(name = "leave_year")
    private Integer leaveYear;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (remainingLeaves == null) {
            remainingLeaves = totalLeavesPerYear - usedLeaves;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        remainingLeaves = totalLeavesPerYear - usedLeaves;
    }
    
    // Constructors
    public LeaveBalance() {}
    
    public LeaveBalance(Integer employeeId, String employeeName, Integer leaveYear) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveYear = leaveYear;
        this.totalLeavesPerYear = 12;
        this.usedLeaves = 0;
        this.remainingLeaves = 12;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public Integer getTotalLeavesPerYear() { return totalLeavesPerYear; }
    public void setTotalLeavesPerYear(Integer totalLeavesPerYear) { this.totalLeavesPerYear = totalLeavesPerYear; }
    
    public Integer getUsedLeaves() { return usedLeaves; }
    public void setUsedLeaves(Integer usedLeaves) { this.usedLeaves = usedLeaves; }
    
    public Integer getRemainingLeaves() { return remainingLeaves; }
    public void setRemainingLeaves(Integer remainingLeaves) { this.remainingLeaves = remainingLeaves; }
    
    public Integer getLeaveYear() { return leaveYear; }
    public void setLeaveYear(Integer leaveYear) { this.leaveYear = leaveYear; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}