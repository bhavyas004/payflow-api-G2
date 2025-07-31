package com.aptpath.payflowapi.repository;

import com.aptpath.payflowapi.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    
    Optional<LeaveBalance> findByEmployeeIdAndLeaveYear(Integer employeeId, Integer leaveYear);
    
    Optional<LeaveBalance> findByEmployeeId(Integer employeeId);
    
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employeeId = :employeeId ORDER BY lb.leaveYear DESC LIMIT 1")
    Optional<LeaveBalance> findLatestByEmployeeId(@Param("employeeId") Integer employeeId);
    
    boolean existsByEmployeeIdAndLeaveYear(Integer employeeId, Integer leaveYear);
}