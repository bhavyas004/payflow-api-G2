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
    
    @Column(name = "employee_id")
    private Integer employeeId;
    
    @Column(name = "month")
    private String month;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "net_pay")
    private BigDecimal netPay;
    
    @Column(name = "deductions")
    private BigDecimal deductions;
    
    @Column(name = "generated_on")
    private LocalDateTime generatedOn;
    
    @Column(name = "download_link")
    private String downloadLink;
    
    @PrePersist
    protected void onCreate() {
        generatedOn = LocalDateTime.now();
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
}