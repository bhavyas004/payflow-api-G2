package com.aptpath.payflowapi.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PayslipService {
    
    // In-memory storage for payslips (replace with database if needed)
    private final Map<String, Map<String, Object>> payslipStorage = new ConcurrentHashMap<>();
    
    public void savePayslip(Map<String, Object> payslipData) {
        String key = generatePayslipKey(payslipData);
        payslipStorage.put(key, payslipData);
        System.out.println("Saved payslip for employee: " + payslipData.get("employeeId") + 
                          " for " + payslipData.get("month") + " " + payslipData.get("year"));
    }
    
    public List<Map<String, Object>> getAllPayslips() {
        return new ArrayList<>(payslipStorage.values());
    }
    
    public List<Map<String, Object>> getPayslipsByEmployeeId(Integer employeeId) {
        return payslipStorage.values().stream()
            .filter(payslip -> employeeId.equals(payslip.get("employeeId")))
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> getPayslipStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalPayslips", payslipStorage.size());
        stats.put("totalEmployees", payslipStorage.values().stream()
            .map(payslip -> payslip.get("employeeId"))
            .distinct()
            .count());
        
        return stats;
    }
    
    private String generatePayslipKey(Map<String, Object> payslipData) {
        return payslipData.get("employeeId") + "_" + 
               payslipData.get("month") + "_" + 
               payslipData.get("year");
    }
}