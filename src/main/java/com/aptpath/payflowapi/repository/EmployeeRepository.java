package com.aptpath.payflowapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aptpath.payflowapi.entity.Employee;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    long countByStatus(Employee.Status status);
    Optional<Employee> findByFullName(String fullName);
    List<Employee> findAllByFullName(String fullName); 
    Optional<Employee> findByEmail(String email);// For handling duplicates
    
    // Manager-related methods
    List<Employee> findByManager(String manager);
    long countByManager(String manager);
    List<Employee> findByManagerIsNull();
    List<Employee> findByStatus(Employee.Status status);
}

