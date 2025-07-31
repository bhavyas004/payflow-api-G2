package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManagerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // Get all available managers (users with role MANAGER or HR)
    public List<Map<String, Object>> getAvailableManagers() {
        List<User> managers = userRepository.findByRoleIn(List.of("MANAGER", "HR"));
        
        return managers.stream().map(manager -> {
            Map<String, Object> managerInfo = new HashMap<>();
            managerInfo.put("username", manager.getUsername());
            managerInfo.put("fullName", manager.getUsername()); // Use username as display name
            managerInfo.put("role", manager.getRole());
            managerInfo.put("email", manager.getEmail());
            
            // Count how many employees this manager currently manages
            long employeeCount = employeeRepository.countByManager(manager.getUsername());
            managerInfo.put("employeeCount", employeeCount);
            
            return managerInfo;
        }).collect(Collectors.toList());
    }
    
    // Assign manager to employee
    public boolean assignManagerToEmployee(String employeeEmail, String managerUsername) {
        try {
            // Validate that the manager exists and has appropriate role
            Optional<User> managerOpt = userRepository.findByUsername(managerUsername);
            if (managerOpt.isEmpty()) {
                throw new RuntimeException("Manager not found with username: " + managerUsername);
            }
            
            User manager = managerOpt.get();
            if (!List.of("MANAGER", "HR").contains(manager.getRole())) {
                throw new RuntimeException("User is not authorized to be a manager");
            }
            
            // Find and update employee by email
            Optional<Employee> employeeOpt = employeeRepository.findByEmail(employeeEmail);
            if (employeeOpt.isEmpty()) {
                throw new RuntimeException("Employee not found with email: " + employeeEmail);
            }
            
            Employee employee = employeeOpt.get();
            employee.setManager(managerUsername);
            employeeRepository.save(employee);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign manager: " + e.getMessage());
        }
    }
    
    // Auto-assign manager based on role and logic
    public String autoAssignManager(String onboardingByUsername) {
        try {
            // Get the user who is doing the onboarding
            Optional<User> onboardingByOpt = userRepository.findByUsername(onboardingByUsername);
            if (onboardingByOpt.isEmpty()) {
                return null;
            }
            
            User onboardingBy = onboardingByOpt.get();
            
            // If onboarding is done by a MANAGER, they become the manager
            if ("MANAGER".equals(onboardingBy.getRole())) {
                return onboardingByUsername;
            }
            
            // If onboarding is done by HR, find the manager with least employees
            if ("HR".equals(onboardingBy.getRole())) {
                List<User> managers = userRepository.findByRole("MANAGER");
                if (managers.isEmpty()) {
                    // If no managers available, HR becomes the manager
                    return onboardingByUsername;
                }
                
                // Find manager with least employees
                String bestManager = null;
                long minEmployees = Long.MAX_VALUE;
                
                for (User manager : managers) {
                    long employeeCount = employeeRepository.countByManager(manager.getUsername());
                    if (employeeCount < minEmployees) {
                        minEmployees = employeeCount;
                        bestManager = manager.getUsername();
                    }
                }
                
                return bestManager != null ? bestManager : onboardingByUsername;
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    // Get employees managed by a specific manager
    public List<Map<String, Object>> getEmployeesByManager(String managerUsername) {
        List<Employee> employees = employeeRepository.findByManager(managerUsername);
        
        return employees.stream().map(employee -> {
            Map<String, Object> empInfo = new HashMap<>();
            empInfo.put("id", employee.getId());
            empInfo.put("fullName", employee.getFullName());
            empInfo.put("email", employee.getEmail());
            empInfo.put("status", employee.getStatus());
            
            return empInfo;
        }).collect(Collectors.toList());
    }
    
    // Get manager details for an employee
    public Map<String, Object> getEmployeeManager(String employeeEmail) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(employeeEmail);
        if (employeeOpt.isEmpty()) {
            return null;
        }
        
        Employee employee = employeeOpt.get();
        if (employee.getManager() == null) {
            return null;
        }
        
        Optional<User> managerOpt = userRepository.findByUsername(employee.getManager());
        if (managerOpt.isEmpty()) {
            return null;
        }
        
        User manager = managerOpt.get();
        Map<String, Object> managerInfo = new HashMap<>();
        managerInfo.put("username", manager.getUsername());
        managerInfo.put("fullName", manager.getUsername()); // Use username as display name
        managerInfo.put("role", manager.getRole());
        managerInfo.put("email", manager.getEmail());
        
        return managerInfo;
    }
}
