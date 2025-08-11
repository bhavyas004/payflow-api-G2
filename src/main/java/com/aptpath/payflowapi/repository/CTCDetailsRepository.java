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
    
    // Find all CTC records for an employee, ordered by effective date and created_at (latest first)
    @Query(value = "SELECT * FROM ctc_details WHERE employee_id = :employeeId " +
           "ORDER BY effective_from DESC, created_at DESC", nativeQuery = true)
    List<CTCDetails> findByEmployeeIdOrderByEffectiveFromDesc(@Param("employeeId") Integer employeeId);
    
    // Find current CTC for an employee (latest effective date, then latest created_at)
    @Query(value = "SELECT * FROM ctc_details c WHERE c.employee_id = :employeeId " +
           "AND c.effective_from <= :currentDate " +
           "ORDER BY c.effective_from DESC, c.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<CTCDetails> findCurrentCTCByEmployeeId(@Param("employeeId") Integer employeeId, 
                                                   @Param("currentDate") LocalDate currentDate);
    
    // Find CTC effective on a specific date (latest created_at for same effective_from)
    @Query(value = "SELECT * FROM ctc_details c WHERE c.employee_id = :employeeId " +
           "AND c.effective_from <= :effectiveDate " +
           "ORDER BY c.effective_from DESC, c.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<CTCDetails> findCTCByEmployeeIdAndDate(@Param("employeeId") Integer employeeId, 
                                                   @Param("effectiveDate") LocalDate effectiveDate);
    
    // Get all employees with CTC
    @Query("SELECT DISTINCT c.employeeId FROM CTCDetails c")
    List<Integer> findAllEmployeeIdsWithCTC();
    
    // Calculate average CTC
    @Query("SELECT AVG(c.totalCtc) FROM CTCDetails c WHERE c.effectiveFrom <= :currentDate")
    Double calculateAverageCTC(@Param("currentDate") LocalDate currentDate);
    
    // Get the latest CTC record for an employee (by effective_from, then created_at)
    @Query(value = "SELECT * FROM ctc_details WHERE employee_id = :employeeId " +
           "ORDER BY effective_from DESC, created_at DESC LIMIT 1", nativeQuery = true)
    Optional<CTCDetails> findTopByEmployeeIdOrderByEffectiveFromDesc(@Param("employeeId") Integer employeeId);
    
    List<CTCDetails> findByEmployeeId(Integer employeeId);
}
