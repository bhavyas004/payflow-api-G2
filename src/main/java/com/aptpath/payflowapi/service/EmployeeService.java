package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.Experience;
import com.aptpath.payflowapi.repository.EmployeeRepository;
import com.aptpath.payflowapi.repository.ExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExperienceRepository experienceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee onboardEmployee(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setAge(dto.getAge());
        employee.setEmail(dto.getEmail());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        employee.setStatus(Employee.Status.valueOf(dto.getStatus().toUpperCase()));

        List<Experience> experienceList = new ArrayList<>();
        for (EmployeeDTO.ExperienceDTO expDto : dto.getExperiences()) {
            Experience exp = new Experience();
            exp.setEmployee(employee);
            exp.setCompanyName(expDto.getCompanyName());
            exp.setStartDate(expDto.getStartDate());
            exp.setEndDate(expDto.getEndDate());
            exp.setTotalExperience(calculateExperience(expDto.getStartDate(), expDto.getEndDate()));
            experienceList.add(exp);
        }
        employee.setExperiences(experienceList);

        Employee savedEmployee = employeeRepository.save(employee);
        experienceRepository.saveAll(experienceList);
        return savedEmployee;
    }

    private String calculateExperience(java.util.Date start, java.util.Date end) {
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();
        Period period = Period.between(startDate, endDate);
        return period.getYears() + " years, " + period.getMonths() + " months";
    }

    public void updateStatusByName(String fullName, String status) {
    Employee employee = employeeRepository.findByFullName(fullName)
        .orElseThrow(() -> new RuntimeException("Employee not found"));
    employee.setStatus(Employee.Status.valueOf(status.toUpperCase()));
    employeeRepository.save(employee);
}
}