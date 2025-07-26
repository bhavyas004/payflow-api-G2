package com.aptpath.payflowapi.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import java.util.List;

@Data
public class EmployeeDTO {
    private String fullName;
    private int age;
    private String totalExperience; // Will be calculated by backend
    private String createdBy; // Username of creator
    private LocalDateTime createdAt; // Auto-generated
    private String status; // ACTIVE or INACTIVE
    private List<ExperienceDTO> experiences; // Past experiences

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getTotalExperience() {
        return totalExperience;
    }
    
    public void setTotalExperience(String totalExperience) {
        this.totalExperience = totalExperience;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<ExperienceDTO> getExperiences() {
        return experiences;
    }
    
    public void setExperiences(List<ExperienceDTO> experiences) {
        this.experiences = experiences;
    }

    @Data
    public static class ExperienceDTO {
        private String company;
        private LocalDate startDate;
        private LocalDate endDate;
        // Note: totalExperience will be calculated by backend
        
        public void setCompany(String company) {
            this.company = company;
        }
        
        public String getCompany() {
            return company;
        }
        
        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
        
        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }
        
        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
 
