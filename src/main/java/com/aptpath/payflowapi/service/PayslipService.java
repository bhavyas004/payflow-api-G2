package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.Payslip;
import com.aptpath.payflowapi.repository.PayslipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayslipService {
    
    @Autowired
    private PayslipRepository payslipRepository;

    // Add this method to PayslipService

    public Optional<Payslip> getPayslipByEmployeeMonthYear(Integer employeeId, String month, Integer year) {
        return payslipRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year);
    }
    
    public void savePayslip(Map<String, Object> payslipData) {
        try {
            // Check if payslip already exists
            Integer employeeId = (Integer) payslipData.get("employeeId");
            String month = (String) payslipData.get("month");
            Integer year = (Integer) payslipData.get("year");
            
            if (payslipRepository.existsByEmployeeIdAndMonthAndYear(employeeId, month, year)) {
                System.out.println("Payslip already exists for employee " + employeeId + " for " + month + " " + year);
                return;
            }
            
            // Create new payslip entity
            Payslip payslip = new Payslip();
            payslip.setEmployeeId(employeeId);
            payslip.setMonth(month);
            payslip.setYear(year);
            
            // Convert and set required fields
            payslip.setNetPay(convertToBigDecimal(payslipData.get("netPay")));
            payslip.setDeductions(convertToBigDecimal(payslipData.get("deductions")));
            
            // Generate download link
            String downloadLink = generateDownloadLink(employeeId, month, year);
            payslip.setDownloadLink(downloadLink);
            
            // Set generated time
            if (payslipData.get("generatedOn") != null) {
                payslip.setGeneratedOn((LocalDateTime) payslipData.get("generatedOn"));
            } else {
                payslip.setGeneratedOn(LocalDateTime.now());
            }
            
            // Save to database
            Payslip savedPayslip = payslipRepository.save(payslip);
            System.out.println("Successfully saved payslip with ID: " + savedPayslip.getPayslipId() + 
                              " for employee: " + employeeId + " for " + month + " " + year);
            
        } catch (Exception e) {
            System.err.println("Error saving payslip: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Map<String, Object>> getAllPayslips() {
        List<Payslip> payslips = payslipRepository.findAll();
        return payslips.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getPayslipsByEmployeeId(Integer employeeId) {
        List<Payslip> payslips = payslipRepository.findByEmployeeId(employeeId);
        return payslips.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> getPayslipStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalPayslips = payslipRepository.count();
        long totalEmployees = payslipRepository.findAll().stream()
                .mapToInt(Payslip::getEmployeeId)
                .distinct()
                .count();
        
        stats.put("totalPayslips", totalPayslips);
        stats.put("totalEmployees", totalEmployees);
        
        return stats;
    }
    
    // Helper method to convert Payslip entity to Map (only required fields)
    private Map<String, Object> convertToMap(Payslip payslip) {
        Map<String, Object> map = new HashMap<>();
        map.put("employeeId", payslip.getEmployeeId());
        map.put("month", payslip.getMonth());
        map.put("year", payslip.getYear());
        map.put("netPay", payslip.getNetPay());
        map.put("deductions", payslip.getDeductions());
        map.put("generatedOn", payslip.getGeneratedOn());
        map.put("downloadLink", payslip.getDownloadLink());
        return map;
    }
    
    // Helper method to generate download link
    private String generateDownloadLink(Integer employeeId, String month, Integer year) {
    // Use the full API URL with correct port
    return String.format("http://localhost:8080/payflowapi/payroll/payslips/download/%d/%s/%d", 
                        employeeId, month.toLowerCase(), year);
}
    
    // Helper method to safely convert to BigDecimal
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
}