package com.aptpath.payflowapi.mapper;

import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Experience;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public EmployeeDTO toDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setAge(employee.getAge());
        dto.setEmail(employee.getEmail());
        dto.setPassword(null); // Do not expose password
        dto.setStatus(employee.getStatus().name());
        dto.setTotalExperience(calculateTotalExperience(employee.getExperiences()));
        dto.setCreatedBy(employee.getCreatedBy() != null ? employee.getCreatedBy().getUsername() : null);
        dto.setCreatedAt(employee.getCreatedAt());
        if (employee.getExperiences() != null) {
            List<EmployeeDTO.ExperienceDTO> expDTOs = employee.getExperiences().stream().map(this::toExperienceDTO).collect(Collectors.toList());
            dto.setExperiences(expDTOs);
        }
        return dto;
    }

    public EmployeeDTO.ExperienceDTO toExperienceDTO(Experience exp) {
        EmployeeDTO.ExperienceDTO dto = new EmployeeDTO.ExperienceDTO();
        dto.setCompanyName(exp.getCompanyName());
        dto.setStartDate(exp.getStartDate());
        dto.setEndDate(exp.getEndDate());
        dto.setTotalExperience(exp.getTotalExperience());
        return dto;
    }

    private String calculateTotalExperience(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return "0 months";
        }
        
        int totalMonths = experiences.stream()
                .mapToInt(exp -> parseExperienceToMonths(exp.getTotalExperience()))
                .sum();
        
        return formatExperienceMonths(totalMonths);
    }

    private int parseExperienceToMonths(String experienceStr) {
        if (experienceStr == null || experienceStr.trim().isEmpty()) {
            return 0;
        }
        
        int totalMonths = 0;
        String[] parts = experienceStr.toLowerCase().split(" ");
        
        for (int i = 0; i < parts.length - 1; i++) {
            try {
                int value = Integer.parseInt(parts[i]);
                String unit = parts[i + 1];
                
                if (unit.startsWith("year")) {
                    totalMonths += value * 12;
                } else if (unit.startsWith("month")) {
                    totalMonths += value;
                }
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
        
        return totalMonths;
    }

    private String formatExperienceMonths(int totalMonths) {
        if (totalMonths == 0) {
            return "0 months";
        }
        
        int years = totalMonths / 12;
        int months = totalMonths % 12;
        
        StringBuilder result = new StringBuilder();
        if (years > 0) {
            result.append(years).append(years == 1 ? " year" : " years");
        }
        if (months > 0) {
            if (years > 0) {
                result.append(" ");
            }
            result.append(months).append(months == 1 ? " month" : " months");
        }
        
        return result.toString();
    }
}
