package com.aptpath.payflowapi.repository;

import com.aptpath.payflowapi.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    
    // Find all payslips for an employee
    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(Integer employeeId);
    
    // Find payslip by employee, month and year
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Integer employeeId, String month, Integer year);
    
    // Find all payslips for a specific month and year
    List<Payslip> findByMonthAndYearOrderByEmployeeId(String month, Integer year);
    
    // Find all payslips for a specific year
    List<Payslip> findByYearOrderByEmployeeIdAscMonthAsc(Integer year);
    
    // Count total payslips
    Long countByMonthAndYear(String month, Integer year);
    
    // Get latest payslips (recent month)
    @Query("SELECT p FROM Payslip p WHERE p.year = :year AND p.month = :month")
    List<Payslip> findLatestPayslips(@Param("month") String month, @Param("year") Integer year);
}