package com.aptpath.payflowapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aptpath.payflowapi.entity.Employee;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    long countByStatus(Employee.Status status);
    Optional<Employee> findByFullName(String fullName);
    List<Employee> findAllByFullName(String fullName); // For handling duplicates
}

