package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.dto.AuthResponse;
import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.dto.LoginDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.Experience;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.ExperienceRepository;
import com.aptpath.payflowapi.repository.UserRepository;
import com.aptpath.payflowapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExperienceRepository experienceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthService authService;
    @Autowired
    private ManagerService managerService;

    public Employee onboardEmployee(EmployeeDTO dto, String specificManagerUsername) {
    // Add debug logging
    System.out.println("Manager from DTO: " + dto.getManager());
    System.out.println("Specific manager username: " + specificManagerUsername);
    
    // Extract username from JWT token
    String username = jwtUtil.extractUsername(getTokenFromRequest());
    User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    Employee employee = new Employee();
    employee.setFullName(dto.getFullName());
    employee.setAge(dto.getAge());
    employee.setEmail(dto.getEmail());
    employee.setPassword(passwordEncoder.encode(dto.getPassword()));
    employee.setStatus(Employee.Status.valueOf(dto.getStatus().toUpperCase()));
    employee.setCreatedBy(currentUser);
    employee.setCreatedAt(LocalDateTime.now());

    // Determine manager BEFORE saving employee
    String assignedManager = null;
    if (specificManagerUsername != null && !specificManagerUsername.trim().isEmpty()) {
        assignedManager = specificManagerUsername;
        System.out.println("Using specific manager from parameter: " + assignedManager);
    } else if (dto.getManager() != null && !dto.getManager().trim().isEmpty()) {
        assignedManager = dto.getManager();
        System.out.println("Using manager from form selection: " + assignedManager);
    } else {
        try {
            assignedManager = managerService.autoAssignManager(currentUser.getUsername());
            System.out.println("Auto-assigned manager: " + assignedManager);
        } catch (Exception e) {
            System.out.println("Auto-assign failed, will proceed without manager: " + e.getMessage());
        }
    }
    
    // Set manager in employee entity BEFORE saving
    if (assignedManager != null && !assignedManager.trim().isEmpty()) {
        employee.setManager(assignedManager); // Make sure this method exists in Employee entity
        System.out.println("Setting manager in employee entity: " + assignedManager);
    }

    List<Experience> experienceList = new ArrayList<>();
    for (EmployeeDTO.ExperienceDTO expDto : dto.getExperiences()) {
        Experience exp = new Experience();
        exp.setEmployee(employee);
        exp.setCompanyName(expDto.getCompanyName());
        exp.setStartDate(expDto.getStartDate());
        exp.setEndDate(expDto.getEndDate());
        exp.setTotalExperience(calculateExperienceInMonths(expDto.getStartDate(), expDto.getEndDate()));
        experienceList.add(exp);
    }
    employee.setExperiences(experienceList);

    // Save the employee with manager info
    Employee savedEmployee = employeeRepository.save(employee);
    experienceRepository.saveAll(experienceList);
    
    // Also assign in manager service for relationship tracking
    try {
        if (assignedManager != null && !assignedManager.trim().isEmpty()) {
            System.out.println("Assigning manager " + assignedManager + " to employee " + savedEmployee.getEmail());
            managerService.assignManagerToEmployee(savedEmployee.getEmail(), assignedManager);
        }
    } catch (Exception e) {
        System.out.println("Warning: Failed to assign manager in manager service: " + e.getMessage());
        e.printStackTrace();
        // Don't fail the entire onboarding process if manager assignment fails
    }
    
    return savedEmployee;
}

    private String calculateExperienceInMonths(java.util.Date start, java.util.Date end) {
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();
        Period period = Period.between(startDate, endDate);
        
        int years = period.getYears();
        int months = period.getMonths();
        
        if (years > 0 && months > 0) {
            return years + " years " + months + " months";
        } else if (years > 0) {
            return years + " year" + (years > 1 ? "s" : "");
        } else {
            return months + " month" + (months > 1 ? "s" : "");
        }
    }

    public void updateStatusByName(String fullName, String status) {
        try {
            System.out.println("Updating status for employee: " + fullName + " to: " + status);
            
            // Find all employees with the given name
            List<Employee> employees = employeeRepository.findAllByFullName(fullName);
            
            if (employees.isEmpty()) {
                throw new RuntimeException("Employee not found with name: " + fullName);
            }
            
            // If multiple employees found, log a warning but update the first one
            if (employees.size() > 1) {
                System.out.println("Warning: Multiple employees found with name '" + fullName + "'. Updating the first one found.");
            }
            
            Employee employee = employees.get(0);  // Get the first employee
            Employee.Status newStatus = Employee.Status.valueOf(status.toUpperCase());
            employee.setStatus(newStatus);
            employeeRepository.save(employee);
            
            System.out.println("Successfully updated employee status");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + status + ". Valid values are ACTIVE, INACTIVE");
        } catch (Exception e) {
            System.err.println("Error updating employee status: " + e.getMessage());
            throw new RuntimeException("Failed to update employee status: " + e.getMessage());
        }
    }

    private String getTokenFromRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("JWT token not found in request");
    }
    // ...existing code...

public AuthResponse employeeLogin(LoginDTO loginDTO) {
    Employee employee = employeeRepository.findByEmail(loginDTO.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    if (!passwordEncoder.matches(loginDTO.getPassword(), employee.getPassword())) {
        throw new RuntimeException("Invalid credentials");
    }

    // Check if employee is active
    if (employee.getStatus() != Employee.Status.ACTIVE) {
        throw new RuntimeException("Employee account is inactive. Please contact HR.");
    }

    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "EMPLOYEE");
    claims.put("employeeId", employee.getId());
    claims.put("fullName", employee.getFullName());
    claims.put("status", employee.getStatus().toString());

    String token = jwtUtil.generateToken(employee.getEmail(), claims);
    
    return new AuthResponse(token, "Employee login successful");
}


}