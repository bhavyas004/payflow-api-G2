package com.aptpath.payflowapi.repository;

import com.aptpath.payflowapi.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    
    List<Payslip> findByEmployeeId(Integer employeeId);
    
    List<Payslip> findByMonthAndYear(String month, Integer year);
    
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Integer employeeId, String month, Integer year);
    
    boolean existsByEmployeeIdAndMonthAndYear(Integer employeeId, String month, Integer year);
}