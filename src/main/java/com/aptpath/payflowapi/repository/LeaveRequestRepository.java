package com.aptpath.payflowapi.repository;

import com.aptpath.payflowapi.entity.LeaveRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
       @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.leaveType = 'UNPAID' AND lr.status = 'APPROVED' AND lr.leaveYear = :year")
       Integer getTotalUnpaidLeaveDaysForYear(@Param("employeeId") Integer employeeId, @Param("year") Integer year);
       // Count paid/unpaid leaves for LeaveBalance update
       @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.leaveType = :leaveType AND lr.status = :status AND lr.leaveYear = :year")
       Long countByEmployeeIdAndLeaveTypeAndStatusAndYear(@Param("employeeId") Integer employeeId, @Param("leaveType") String leaveType, @Param("status") String status, @Param("year") Integer year);
       // Count approved paid leaves for the year
       @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.leaveType = 'PAID' AND lr.status = 'APPROVED' AND lr.leaveYear = :year")
       Long countPaidLeavesForYear(@Param("employeeId") Integer employeeId, @Param("year") Integer year);

       // Count approved unpaid leaves for the month and year
       @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.leaveType = 'UNPAID' AND lr.status = 'APPROVED' AND FUNCTION('MONTH', lr.startDate) = :month AND lr.leaveYear = :year")
       Long countUnpaidLeavesForMonthAndYear(@Param("employeeId") Integer employeeId, @Param("month") int month, @Param("year") Integer year);
    
    // Find by employee
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Integer employeeId);
    Page<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Integer employeeId, Pageable pageable);

              // Find latest approved leave request for employee (for employeeName)
              Optional<LeaveRequest> findTopByEmployeeIdAndStatusOrderByApprovedDateDesc(Integer employeeId, String status);
    
    // Find by status
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);
    Page<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    // Find by employee and status
    List<LeaveRequest> findByEmployeeIdAndStatusOrderByCreatedAtDesc(Integer employeeId, String status);
    Page<LeaveRequest> findByEmployeeIdAndStatusOrderByCreatedAtDesc(Integer employeeId, String status, Pageable pageable);
    
    // Find by year
    List<LeaveRequest> findByLeaveYearOrderByCreatedAtDesc(Integer leaveYear);
    
    // Find by date range
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate >= :startDate AND lr.endDate <= :endDate ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Check for overlapping leaves
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.status = 'APPROVED' " +
           "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") Integer employeeId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    // Calculate total leave days
    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.leaveYear = :year AND lr.status = 'APPROVED'")
    Integer getTotalApprovedLeaveDays(@Param("employeeId") Integer employeeId, @Param("year") Integer year);
    
    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "AND lr.leaveYear = :year AND lr.status IN ('PENDING', 'APPROVED')")
    Integer getTotalPendingAndApprovedLeaveDays(@Param("employeeId") Integer employeeId, @Param("year") Integer year);
    
    // Statistics queries
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.leaveYear = :year")
    Long countByEmployeeAndYear(@Param("employeeId") Integer employeeId, @Param("year") Integer year);
    
    // All with pagination
    Page<LeaveRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
    // Add these methods to your existing LeaveRequestRepository interface

Long countByEmployeeEmailInAndStatus(List<String> employeeEmails, String status);
List<LeaveRequest> findByEmployeeEmailInOrderByCreatedAtDesc(List<String> employeeEmails);
List<LeaveRequest> findByEmployeeEmailInAndStatusOrderByCreatedAtDesc(List<String> employeeEmails, String status);

// Sum unpaidDays for approved leave requests (UNPAID or MIXED) for the month and year
    @Query("SELECT COALESCE(SUM(lr.unpaidDays), 0) FROM LeaveRequest lr WHERE lr.employeeId = :employeeId AND lr.unpaidDays > 0 AND lr.status = 'APPROVED' AND FUNCTION('MONTH', lr.startDate) = :month AND lr.leaveYear = :year")
    Integer sumUnpaidDaysForMonthAndYear(@Param("employeeId") Integer employeeId, @Param("month") int month, @Param("year") Integer year);
}