package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.entity.CTCDetails;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.service.CTCService;
import com.aptpath.payflowapi.service.EmployeeService;
import com.aptpath.payflowapi.service.PayslipService;
import com.aptpath.payflowapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payroll")
@CrossOrigin(origins = "*")
public class PayrollController {
    
    @Autowired
    private CTCService ctcService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PayslipService payslipService;

    @Autowired
    private EmployeeService employeeService;
    
    // Add CTC
    @PostMapping("/ctc/add")
    public ResponseEntity<?> addCTC(@RequestBody CTCDetails ctcDetails, 
                                   @RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            ctcDetails.setCreatedBy(username);
            
            CTCDetails savedCTC = ctcService.saveCTCDetails(ctcDetails);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CTC details saved successfully");
            response.put("data", savedCTC);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get CTC history for an employee
    @GetMapping("/ctc/{employeeId}/history")
    public ResponseEntity<?> getCTCHistory(@PathVariable Integer employeeId) {
        try {
            List<CTCDetails> ctcHistory = ctcService.getCTCHistory(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ctcHistory);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get current CTC for an employee
    @GetMapping("/ctc/{employeeId}/current")
    public ResponseEntity<?> getCurrentCTC(@PathVariable Integer employeeId) {
        try {
            Optional<CTCDetails> currentCTC = ctcService.getCurrentCTC(employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", currentCTC.orElse(null));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    // Get payroll statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getPayrollStats() {
        try {
            Map<String, Object> stats = ctcService.getPayrollStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
   // Update only the generateMonthlyPayslips method

@PostMapping("/payslips/generate")
public ResponseEntity<?> generateMonthlyPayslips(@RequestBody Map<String, Object> request) {
    try {
        String month = (String) request.get("month");
        Integer year = (Integer) request.get("year");

        // Get all employees with better error handling
        List<Employee> employees;
        try {
            // Try to get active employees first
            employees = employeeService.findActiveEmployees();
            
            // If no active employees found, try all employees
            if (employees.isEmpty()) {
                employees = employeeService.getAllEmployees();
                System.out.println("No active employees found, using all employees");
            }
        } catch (Exception e) {
            // If findActiveEmployees fails (due to enum issues), fallback to all employees
            System.out.println("Error finding active employees: " + e.getMessage());
            employees = employeeService.getAllEmployees();
        }
        
        if (employees.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("No employees found in the system"));
        }

        System.out.println("Found " + employees.size() + " employees to process");
        List<Map<String, Object>> generatedPayslips = new ArrayList<>();

        for (Employee employee : employees) {
            try {
                System.out.println("Processing employee: " + employee.getId());
                
                // Get employee's current CTC
                CTCDetails ctcDetails = ctcService.getCurrentCTCDetails(employee.getId());
                
                if (ctcDetails != null) {
                    System.out.println("Found CTC details for employee: " + employee.getId());
                    
                    // Generate payslip data from CTC
                    Map<String, Object> payslipData = generatePayslipFromCTC(employee, ctcDetails, month, year);
                    
                    // Save payslip
                    payslipService.savePayslip(payslipData);
                    generatedPayslips.add(payslipData);
                    
                    System.out.println("Generated payslip for employee: " + employee.getId());
                } else {
                    System.out.println("No CTC details found for employee: " + employee.getId());
                }
            } catch (Exception e) {
                System.out.println("Error generating payslip for employee " + employee.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Monthly payslips generated successfully");
        response.put("generated", generatedPayslips.size());
        response.put("totalEmployees", employees.size());
        response.put("data", generatedPayslips);
        
        System.out.println("Successfully generated " + generatedPayslips.size() + " payslips out of " + employees.size() + " employees");
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        System.out.println("Error in generateMonthlyPayslips: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
    }
}
    // Get all payslips (calculated from CTC)
    @GetMapping("/payslips")
    public ResponseEntity<?> getAllPayslips() {
        try {
            List<Map<String, Object>> payslips = payslipService.getAllPayslips();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", payslips);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get payslips for specific employee
    @GetMapping("/payslips/employee/{employeeId}")
    public ResponseEntity<?> getEmployeePayslips(@PathVariable Integer employeeId) {
        try {
            List<Map<String, Object>> payslips = payslipService.getPayslipsByEmployeeId(employeeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", payslips);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Calculate salary breakdown from CTC
    @GetMapping("/calculate/{employeeId}")
    public ResponseEntity<?> calculateSalaryBreakdown(@PathVariable Integer employeeId) {
        try {
            // FIXED METHOD NAME
            Employee employee = employeeService.findEmployeeById(employeeId);
            if (employee == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Employee not found"));
            }

            // FIXED METHOD NAME
            CTCDetails ctcDetails = ctcService.getCurrentCTCDetails(employeeId);
            if (ctcDetails == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No CTC details found for employee"));
            }

            Map<String, Object> salaryBreakdown = calculateMonthlySalaryFromCTC(ctcDetails);
            salaryBreakdown.put("employeeId", employeeId);
            salaryBreakdown.put("employeeName", employee.getFullName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", salaryBreakdown);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Get payslip statistics
    @GetMapping("/payslips/stats")
    public ResponseEntity<?> getPayslipStats() {
        try {
            Map<String, Object> stats = payslipService.getPayslipStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Helper method to generate payslip data from CTC
    private Map<String, Object> generatePayslipFromCTC(Employee employee, CTCDetails ctcDetails, String month, Integer year) {
        Map<String, Object> payslip = new HashMap<>();
        
        // Employee details
        payslip.put("employeeId", employee.getId());
        payslip.put("employeeName", employee.getFullName());
        payslip.put("month", month);
        payslip.put("year", year);
        
        // Calculate monthly amounts from annual CTC
        Map<String, Object> salaryBreakdown = calculateMonthlySalaryFromCTC(ctcDetails);
        payslip.putAll(salaryBreakdown);
        
        // Add generation metadata
        payslip.put("generatedOn", java.time.LocalDateTime.now());
        payslip.put("status", "GENERATED");
        
        return payslip;
    }

    // Helper method to calculate monthly salary from CTC
    private Map<String, Object> calculateMonthlySalaryFromCTC(CTCDetails ctcDetails) {
        Map<String, Object> breakdown = new HashMap<>();
        
        // Convert annual amounts to monthly (divide by 12)
        BigDecimal monthlyBasicSalary = ctcDetails.getBasicSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyHra = ctcDetails.getHra().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyAllowances = ctcDetails.getAllowances().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyBonuses = ctcDetails.getBonuses().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyPfContribution = ctcDetails.getPfContribution().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyGratuity = ctcDetails.getGratuity().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyTotalCtc = ctcDetails.getTotalCtc().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        
        // Calculate gross salary (earnings)
        BigDecimal grossSalary = monthlyBasicSalary
            .add(monthlyHra)
            .add(monthlyAllowances)
            .add(monthlyBonuses);
        
        // Calculate deductions (PF contribution + simple tax calculation)
        BigDecimal totalDeductions = monthlyPfContribution
            .add(calculateSimpleTax(monthlyTotalCtc)); // Simple tax calculation
        
        // Calculate net pay
        BigDecimal netPay = grossSalary.subtract(totalDeductions);
        
        // Add to breakdown
        breakdown.put("basicSalary", monthlyBasicSalary);
        breakdown.put("hra", monthlyHra);
        breakdown.put("allowances", monthlyAllowances);
        breakdown.put("bonuses", monthlyBonuses);
        breakdown.put("grossSalary", grossSalary);
        breakdown.put("pfContribution", monthlyPfContribution);
        breakdown.put("gratuity", monthlyGratuity);
        breakdown.put("tax", calculateSimpleTax(monthlyTotalCtc));
        breakdown.put("deductions", totalDeductions);
        breakdown.put("netPay", netPay);
        breakdown.put("totalCtc", monthlyTotalCtc);
        
        return breakdown;
    }

    // Simple tax calculation (10% of gross salary as example)
    private BigDecimal calculateSimpleTax(BigDecimal monthlySalary) {
        // Simple tax calculation - 10% if monthly salary > 50,000
        if (monthlySalary.compareTo(BigDecimal.valueOf(50000)) > 0) {
            return monthlySalary.multiply(BigDecimal.valueOf(0.10));
        }
        return BigDecimal.ZERO;
    }

    // Helper method to create error response
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}