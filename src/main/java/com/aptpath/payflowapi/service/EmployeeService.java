package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.UserRepository;
import com.aptpath.payflowapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Employee createEmployee(EmployeeDTO dto,String token) {
    	if (!jwtUtil.validateTokenForMultipleRoles(token, List.of("HR", "MANAGER"))) {
    	    throw new RuntimeException("Only HR or Manager can create employees");
    	}
    	String username = jwtUtil.extractUsername(token);
        User createdBy = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = new Employee();
                employee.setFullName(dto.getFullName());
                employee.setAge(dto.getAge());
                employee.setTotalExperience(dto.getTotalExperience());
                employee.setPastExperience(dto.getPastExperience());
                employee.setCreatedBy(createdBy);
                employee.setStatus(Employee.Status.ACTIVE);

        return employeeRepo.save(employee);
    }
}

