package com.aptpath.payflowapi.repository;

import com.aptpath.payflowapi.entity.CTCDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CTCDetailsRepository extends JpaRepository<CTCDetails, Long> {
    
    // Find all CTC records for an employee, ordered by effective date (latest first)
    List<CTCDetails> findByEmployeeIdOrderByEffectiveFromDesc(Integer employeeId);
    
    // Find current CTC for an employee (latest effective date)
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId " +
           "AND c.effectiveFrom <= :currentDate " +
           "ORDER BY c.effectiveFrom DESC")
    Optional<CTCDetails> findCurrentCTCByEmployeeId(@Param("employeeId") Integer employeeId, 
                                                   @Param("currentDate") LocalDate currentDate);
    
    // Find CTC effective on a specific date
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId " +
           "AND c.effectiveFrom <= :effectiveDate " +
           "ORDER BY c.effectiveFrom DESC")
    Optional<CTCDetails> findCTCByEmployeeIdAndDate(@Param("employeeId") Integer employeeId, 
                                                   @Param("effectiveDate") LocalDate effectiveDate);
    
    // Get all employees with CTC
    @Query("SELECT DISTINCT c.employeeId FROM CTCDetails c")
    List<Integer> findAllEmployeeIdsWithCTC();
    
    // Calculate average CTC
    @Query("SELECT AVG(c.totalCtc) FROM CTCDetails c WHERE c.effectiveFrom <= :currentDate")
    Double calculateAverageCTC(@Param("currentDate") LocalDate currentDate);
    
    Optional<CTCDetails> findTopByEmployeeIdOrderByEffectiveFromDesc(Integer employeeId);
    
    List<CTCDetails> findByEmployeeId(Integer employeeId);
}
