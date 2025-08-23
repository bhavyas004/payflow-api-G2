package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.LeaveBalance;
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

    @Autowired
    private com.aptpath.payflowapi.repository.LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private com.aptpath.payflowapi.repository.LeaveBalanceRepository leaveBalanceRepository;

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
                return; // Payslip already exists, skip
            }

            // Get unpaid leaves from leave_balance table for the year
            Integer unpaidLeaves = 0;
            Optional<LeaveBalance> leaveBalanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year);
            if (leaveBalanceOpt.isPresent()) {
                LeaveBalance leaveBalance = leaveBalanceOpt.get();
                unpaidLeaves = leaveBalance.getUnpaidLeaves();
            }

            // Calculate per day salary (example: assume 30 days in month)
            BigDecimal grossSalary = convertToBigDecimal(payslipData.get("grossSalary"));
            BigDecimal perDaySalary = grossSalary.divide(BigDecimal.valueOf(30), BigDecimal.ROUND_HALF_UP);
            BigDecimal unpaidLeaveDeduction = perDaySalary.multiply(BigDecimal.valueOf(unpaidLeaves));

            // Create new payslip entity
            Payslip payslip = new Payslip();
            payslip.setEmployeeId(employeeId);
            payslip.setMonth(month);
            payslip.setYear(year);

            // Convert and set required fields
            BigDecimal netPay = convertToBigDecimal(payslipData.get("netPay"));
            BigDecimal deductions = convertToBigDecimal(payslipData.get("deductions"));

            deductions = deductions.add(unpaidLeaveDeduction);
            netPay = netPay.subtract(unpaidLeaveDeduction);

            payslip.setNetPay(netPay);
            payslip.setDeductions(deductions);
            payslip.setUnpaidLeaves(unpaidLeaves);
            payslip.setUnpaidLeaveDeduction(unpaidLeaveDeduction);
            if (payslipData.get("generatedOn") != null) {
                payslip.setGeneratedOn((LocalDateTime) payslipData.get("generatedOn"));
            } else {
                payslip.setGeneratedOn(LocalDateTime.now());
            }

            // Save to database
            payslipRepository.save(payslip);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save payslip: " + e.getMessage(), e);
        }
    }

    // Helper to convert month name to number
    private int getMonthNumber(String monthName) {
        Map<String, Integer> months = new HashMap<>();
        months.put("january", 1);
        months.put("february", 2);
        months.put("march", 3);
        months.put("april", 4);
        months.put("may", 5);
        months.put("june", 6);
        months.put("july", 7);
        months.put("august", 8);
        months.put("september", 9);
        months.put("october", 10);
        months.put("november", 11);
        months.put("december", 12);
        return months.getOrDefault(monthName.toLowerCase(), 1);
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
    map.put("unpaidLeaves", payslip.getUnpaidLeaves());
    map.put("unpaidLeaveDeduction", payslip.getUnpaidLeaveDeduction());
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