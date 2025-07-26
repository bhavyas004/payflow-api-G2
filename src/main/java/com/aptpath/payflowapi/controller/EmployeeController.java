package com.aptpath.payflowapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.service.EmployeeService;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.mapper.EmployeeMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/onboard-employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @PostMapping("/add")
    public Employee addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return employeeService.onboardEmployee(employeeDTO);
    }

    @GetMapping("/employees")
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{fullName}/status")
    public ResponseEntity<?> updateStatusByName(@PathVariable String fullName, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        employeeService.updateStatusByName(fullName, status);
        return ResponseEntity.ok().build();
    } 

}