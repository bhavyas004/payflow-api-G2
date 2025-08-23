package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payslips")
public class Payslip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payslip_id")
    private Long payslipId;
    
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;
    
    @Column(name = "month", nullable = false)
    private String month;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "net_pay", nullable = false, precision = 12, scale = 2)
    private BigDecimal netPay;
    
    @Column(name = "deductions", precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;
    
    @Column(name = "generated_on")
    private LocalDateTime generatedOn;
    
    @Column(name = "download_link", length = 500)
    private String downloadLink;
    
    @Column(name = "unpaid_leaves", nullable = false)
    private Integer unpaidLeaves = 0;
    
    @Column(name = "unpaid_leave_deduction", precision = 12, scale = 2)
    private BigDecimal unpaidLeaveDeduction = BigDecimal.ZERO;
    
    @PrePersist
    protected void onCreate() {
        if (generatedOn == null) {
            generatedOn = LocalDateTime.now();
        }
        if (deductions == null) {
            deductions = BigDecimal.ZERO;
        }
    }
    
    // Constructors
    public Payslip() {}
    
    // Getters and Setters
    public Long getPayslipId() { return payslipId; }
    public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }
    
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    
    public LocalDateTime getGeneratedOn() { return generatedOn; }
    public void setGeneratedOn(LocalDateTime generatedOn) { this.generatedOn = generatedOn; }
    
    public String getDownloadLink() { return downloadLink; }
    public void setDownloadLink(String downloadLink) { this.downloadLink = downloadLink; }
    
    public Integer getUnpaidLeaves() { return unpaidLeaves; }
    public void setUnpaidLeaves(Integer unpaidLeaves) { this.unpaidLeaves = unpaidLeaves; }
    
    public BigDecimal getUnpaidLeaveDeduction() { return unpaidLeaveDeduction; }
    public void setUnpaidLeaveDeduction(BigDecimal unpaidLeaveDeduction) { this.unpaidLeaveDeduction = unpaidLeaveDeduction; }
}