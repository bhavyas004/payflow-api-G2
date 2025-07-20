package com.aptpath.payflowapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.service.EmployeeService;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
	@Autowired
	private EmployeeService employeeService;

	@PostMapping("/create")
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeDTO dto,@RequestHeader("Authorization") String token) {
    	token = token.substring(7);
        Employee employee = employeeService.createEmployee(dto,token);
        return ResponseEntity.ok("Employee details added sucessfully");
    }
}
