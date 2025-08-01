package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.CTCDetails;
import com.aptpath.payflowapi.repository.CTCDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional
public class CTCService {
    
    @Autowired
    private CTCDetailsRepository ctcRepository;
    
    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;
    
    // Add or update CTC
    public CTCDetails saveCTCDetails(CTCDetails ctcDetails) {
        // Calculate total CTC
        BigDecimal total = ctcDetails.getBasicSalary()
            .add(ctcDetails.getHra() != null ? ctcDetails.getHra() : BigDecimal.ZERO)
            .add(ctcDetails.getAllowances() != null ? ctcDetails.getAllowances() : BigDecimal.ZERO)
            .add(ctcDetails.getBonuses() != null ? ctcDetails.getBonuses() : BigDecimal.ZERO)
            .add(ctcDetails.getPfContribution() != null ? ctcDetails.getPfContribution() : BigDecimal.ZERO)
            .add(ctcDetails.getGratuity() != null ? ctcDetails.getGratuity() : BigDecimal.ZERO);
        
        ctcDetails.setTotalCtc(total);
        return ctcRepository.save(ctcDetails);
    }
    
    // Get CTC history for an employee
    public List<CTCDetails> getCTCHistory(Integer employeeId) {
        return ctcRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);
    }
    
    // Get current CTC for an employee
    public Optional<CTCDetails> getCurrentCTC(Integer employeeId) {
        return ctcRepository.findCurrentCTCByEmployeeId(employeeId, LocalDate.now());
    }
    
    // Get CTC effective on a specific date
    public Optional<CTCDetails> getCTCByDate(Integer employeeId, LocalDate date) {
        return ctcRepository.findCTCByEmployeeIdAndDate(employeeId, date);
    }
    
    // Get payroll statistics
    public Map<String, Object> getPayrollStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get all employees with CTC
        List<Integer> employeeIds = ctcRepository.findAllEmployeeIdsWithCTC();
        stats.put("totalEmployeesWithCTC", employeeIds.size());
        
        // Calculate total monthly payroll
        BigDecimal totalMonthlyPayroll = BigDecimal.ZERO;
        for (Integer employeeId : employeeIds) {
            Optional<CTCDetails> currentCTC = getCurrentCTC(employeeId);
            if (currentCTC.isPresent()) {
                // Convert annual CTC to monthly
                BigDecimal monthlySalary = currentCTC.get().getTotalCtc().divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
                totalMonthlyPayroll = totalMonthlyPayroll.add(monthlySalary);
            }
        }
        stats.put("totalMonthlyPayroll", totalMonthlyPayroll);
        
        // Calculate average CTC
        Double avgCTC = ctcRepository.calculateAverageCTC(LocalDate.now());
        stats.put("averageCTC", avgCTC != null ? avgCTC : 0.0);
        
        return stats;
    }

    public CTCDetails getCurrentCTCDetails(Integer employeeId) {
    // Implementation to get current CTC
    return ctcDetailsRepository.findTopByEmployeeIdOrderByEffectiveFromDesc(employeeId)
            .orElse(null);
}

}
