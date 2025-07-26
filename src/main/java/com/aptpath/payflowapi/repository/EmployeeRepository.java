package com.aptpath.payflowapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aptpath.payflowapi.entity.Employee;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    long countByStatus(String status);
    Optional<Employee> findByFullName(String fullName);
}

