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
        dto.setFullName(employee.getFullName());
        dto.setAge(employee.getAge());
        dto.setEmail(employee.getEmail());
        dto.setPassword(null); // Do not expose password
        dto.setStatus(employee.getStatus().name());
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
}
