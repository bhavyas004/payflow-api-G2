package com.aptpath.payflowapi.repository;

import com.aptpath.payflowapi.entity.LeaveRequest;
import com.aptpath.payflowapi.entity.LeaveRequest.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Integer employeeId);
    
    List<LeaveRequest> findByEmployeeIdAndLeaveYear(Integer employeeId, Integer leaveYear);
    
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    List<LeaveRequest> findByEmployeeIdAndStatus(Integer employeeId, LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.leaveYear = :year AND lr.status = 'APPROVED'")
    List<LeaveRequest> findApprovedLeavesByEmployeeAndYear(@Param("employeeId") Integer employeeId, 
                                                          @Param("year") Integer year);
    
    @Query("SELECT SUM(lr.totalDays) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.leaveYear = :year AND lr.status = 'APPROVED'")
    Integer getTotalApprovedDaysByEmployeeAndYear(@Param("employeeId") Integer employeeId, 
                                                 @Param("year") Integer year);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate " +
           "AND lr.employeeId = :employeeId AND lr.status IN ('PENDING', 'APPROVED')")
    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") Integer employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
