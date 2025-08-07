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
    @Column(name = "ctc_id", nullable = false)
    private Long ctcId;
    
    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    
    @Column(name = "basic_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary;
    
    @Column(name = "hra", nullable = true, precision = 12, scale = 2, columnDefinition = "decimal(12,2) DEFAULT 0.00")
    private BigDecimal hra;
    
    @Column(name = "allowances", nullable = true, precision = 12, scale = 2, columnDefinition = "decimal(12,2) DEFAULT 0.00")
    private BigDecimal allowances;
    
    @Column(name = "bonuses", nullable = true, precision = 12, scale = 2, columnDefinition = "decimal(12,2) DEFAULT 0.00")
    private BigDecimal bonuses;
    
    @Column(name = "pf_contribution", nullable = true, precision = 12, scale = 2, columnDefinition = "decimal(12,2) DEFAULT 0.00")
    private BigDecimal pfContribution;
    
    @Column(name = "gratuity", nullable = true, precision = 12, scale = 2, columnDefinition = "decimal(12,2) DEFAULT 0.00")
    private BigDecimal gratuity;
    
    @Column(name = "total_ctc", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCtc;
    
    @Column(name = "created_at", nullable = true, columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", nullable = true, length = 255)
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