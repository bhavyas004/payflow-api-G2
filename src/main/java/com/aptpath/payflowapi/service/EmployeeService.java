package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.Experience;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.UserRepository;
import com.aptpath.payflowapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private JwtUtil jwtUtil;

    public Employee createEmployee(EmployeeDTO empDto, String token) {
        // Validate token and extract user information
        if (!jwtUtil.validateTokenForMultipleRoles(token, List.of("HR", "MANAGER"))) {
            throw new RuntimeException("Only HR or Manager can create employees");
        }
        String username = jwtUtil.extractUsername(token);
        User createdBy = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new Employee entity
        Employee employee = new Employee();
        employee.setFullName(empDto.getFullName());
        employee.setAge(empDto.getAge());
        employee.setCreatedBy(createdBy);
        employee.setCreatedAt(LocalDateTime.now()); // Auto-generated
        employee.setStatus(empDto.getStatus() != null ? 
            Employee.Status.valueOf(empDto.getStatus().toUpperCase()) : Employee.Status.ACTIVE);
        
        // Process experiences and calculate total experience
        List<Experience> expList = new ArrayList<>();
        int totalExperienceMonths = 0;
        
        if (empDto.getExperiences() != null) {
            for (EmployeeDTO.ExperienceDTO dto : empDto.getExperiences()) {
                Experience exp = new Experience();
                exp.setCompanyName(dto.getCompany());
                exp.setStartDate(dto.getStartDate());
                exp.setEndDate(dto.getEndDate());
                
                // Calculate experience in months for this job
                int experienceMonths = calculateTotalExperienceInMonths(dto.getStartDate(), dto.getEndDate());
                exp.setTotalExperience(experienceMonths);
                exp.setEmployee(employee); // Set foreign key reference
                
                expList.add(exp);
                totalExperienceMonths += experienceMonths;
            }
        }
        
        // Set total experience as a formatted string
        int years = totalExperienceMonths / 12;
        int months = totalExperienceMonths % 12;
        String totalExpStr = years > 0 ? years + " years " + months + " months" : months + " months";
        employee.setTotalExperience(totalExpStr);
        
        // Set experiences (this will trigger the setter in Employee entity)
        employee.setExperiences(expList);
        
        return employeeRepo.save(employee);
    }
    public EmployeeRepository getEmployeeRepository() {
        return employeeRepo;
    }
    private int calculateTotalExperienceInMonths(LocalDate start, LocalDate end) {
        Period period = Period.between(start, end);
        return period.getYears() * 12 + period.getMonths();
    }
}

