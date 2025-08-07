package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.CTCDetails;
import com.aptpath.payflowapi.repository.CTCDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    
    // Add or update CTC with real-world calculations
    public CTCDetails saveCTCDetails(CTCDetails ctcDetails) {
        // Validate basic salary
        if (ctcDetails.getBasicSalary() == null || ctcDetails.getBasicSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Basic salary must be greater than zero");
        }
        
        // Auto-calculate components if not provided (for backward compatibility)
        if (ctcDetails.getHra() == null) {
            ctcDetails.setHra(calculateHRA(ctcDetails.getBasicSalary(), true)); // Default to metro
        }
        
        if (ctcDetails.getPfContribution() == null) {
            ctcDetails.setPfContribution(calculatePFContribution(ctcDetails.getBasicSalary()));
        }
        
        if (ctcDetails.getGratuity() == null) {
            ctcDetails.setGratuity(calculateGratuity(ctcDetails.getBasicSalary()));
        }
        
        // Ensure allowances and bonuses are not null
        if (ctcDetails.getAllowances() == null) {
            ctcDetails.setAllowances(BigDecimal.ZERO);
        }
        
        if (ctcDetails.getBonuses() == null) {
            ctcDetails.setBonuses(BigDecimal.ZERO);
        }
        
        // Calculate total CTC with all components
        BigDecimal total = ctcDetails.getBasicSalary()
            .add(ctcDetails.getHra())
            .add(ctcDetails.getAllowances())
            .add(ctcDetails.getBonuses())
            .add(ctcDetails.getPfContribution())
            .add(ctcDetails.getGratuity());
        
        ctcDetails.setTotalCtc(total);
        return ctcRepository.save(ctcDetails);
    }
    
    // Real-world HRA calculation
    public BigDecimal calculateHRA(BigDecimal basicSalary, boolean isMetroCity) {
        BigDecimal hraPercentage = isMetroCity ? BigDecimal.valueOf(0.50) : BigDecimal.valueOf(0.40);
        return basicSalary.multiply(hraPercentage).setScale(0, RoundingMode.HALF_UP);
    }
    
    // PF Contribution calculation (employer's contribution)
    public BigDecimal calculatePFContribution(BigDecimal basicSalary) {
        return basicSalary.multiply(BigDecimal.valueOf(0.12)).setScale(0, RoundingMode.HALF_UP);
    }
    
    // Gratuity calculation (annual provision)
    public BigDecimal calculateGratuity(BigDecimal basicSalary) {
        return basicSalary.multiply(BigDecimal.valueOf(0.0481)).setScale(0, RoundingMode.HALF_UP);
    }
    
    // Validate CTC structure
    public Map<String, Object> validateCTCStructure(CTCDetails ctcDetails) {
        Map<String, Object> validation = new HashMap<>();
        boolean isValid = true;
        List<String> errors = new java.util.ArrayList<>();
        
        // Basic salary should be 40-60% of total CTC
        BigDecimal basicPercentage = ctcDetails.getBasicSalary()
            .multiply(BigDecimal.valueOf(100))
            .divide(ctcDetails.getTotalCtc(), 2, RoundingMode.HALF_UP);
            
        if (basicPercentage.compareTo(BigDecimal.valueOf(40)) < 0) {
            errors.add("Basic salary is less than 40% of CTC, which is unusual");
            isValid = false;
        }
        
        if (basicPercentage.compareTo(BigDecimal.valueOf(60)) > 0) {
            errors.add("Basic salary is more than 60% of CTC, which may not be tax-efficient");
        }
        
        validation.put("isValid", isValid);
        validation.put("errors", errors);
        validation.put("basicSalaryPercentage", basicPercentage);
        
        return validation;
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
