package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ctc_details")
public class CTCDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ctc_id")
    private Long ctcId;
    
    @Column(name = "employee_id")
    private Integer employeeId;
    
    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
    
    @Column(name = "basic_salary")
    private BigDecimal basicSalary;
    
    @Column(name = "hra")
    private BigDecimal hra;
    
    @Column(name = "allowances")
    private BigDecimal allowances;
    
    @Column(name = "bonuses")
    private BigDecimal bonuses;
    
    @Column(name = "pf_contribution")
    private BigDecimal pfContribution;
    
    @Column(name = "gratuity")
    private BigDecimal gratuity;
    
    @Column(name = "total_ctc")
    private BigDecimal totalCtc;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public CTCDetails() {}
    
    // Getters and Setters
    public Long getCtcId() { return ctcId; }
    public void setCtcId(Long ctcId) { this.ctcId = ctcId; }
    
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    
    public BigDecimal getHra() { return hra; }
    public void setHra(BigDecimal hra) { this.hra = hra; }
    
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    
    public BigDecimal getBonuses() { return bonuses; }
    public void setBonuses(BigDecimal bonuses) { this.bonuses = bonuses; }
    
    public BigDecimal getPfContribution() { return pfContribution; }
    public void setPfContribution(BigDecimal pfContribution) { this.pfContribution = pfContribution; }
    
    public BigDecimal getGratuity() { return gratuity; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }
    
    public BigDecimal getTotalCtc() { return totalCtc; }
    public void setTotalCtc(BigDecimal totalCtc) { this.totalCtc = totalCtc; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}