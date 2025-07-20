package com.aptpath.payflowapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aptpath.payflowapi.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}

