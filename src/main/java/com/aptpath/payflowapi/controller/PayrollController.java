package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.entity.CTCDetails;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.service.CTCService;
import com.aptpath.payflowapi.service.EmployeeService;
import com.aptpath.payflowapi.service.PayslipPDFService;
import com.aptpath.payflowapi.service.PayslipService;
import com.aptpath.payflowapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Add these imports at the top

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    // Preview CTC calculation
    @PostMapping("/ctc/preview")
    public ResponseEntity<?> previewCTCCalculation(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal basicSalary = new BigDecimal(request.get("basicSalary").toString());
            BigDecimal allowances = request.get("allowances") != null ? 
                new BigDecimal(request.get("allowances").toString()) : BigDecimal.ZERO;
            BigDecimal bonuses = request.get("bonuses") != null ? 
                new BigDecimal(request.get("bonuses").toString()) : BigDecimal.ZERO;
            Boolean isMetroCity = request.get("isMetroCity") != null ? 
                (Boolean) request.get("isMetroCity") : true;

            // Calculate components
            BigDecimal hra = ctcService.calculateHRA(basicSalary, isMetroCity);
            BigDecimal pfContribution = ctcService.calculatePFContribution(basicSalary);
            BigDecimal gratuity = ctcService.calculateGratuity(basicSalary);
            BigDecimal totalCTC = basicSalary.add(hra).add(allowances).add(bonuses).add(pfContribution).add(gratuity);

            Map<String, Object> calculation = new HashMap<>();
            calculation.put("basicSalary", basicSalary);
            calculation.put("hra", hra);
            calculation.put("allowances", allowances);
            calculation.put("bonuses", bonuses);
            calculation.put("pfContribution", pfContribution);
            calculation.put("gratuity", gratuity);
            calculation.put("totalCTC", totalCTC);
            calculation.put("isMetroCity", isMetroCity);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", calculation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Invalid calculation parameters: " + e.getMessage()));
        }
    }

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

    // Update the generateMonthlyPayslips method

    @PostMapping("/payslips/generate")
    public ResponseEntity<?> generateMonthlyPayslips(@RequestBody Map<String, Object> request) {
        try {
            // Handle month as either Integer or String
            Object monthObj = request.get("month");
            Object yearObj = request.get("year");
            List<Integer> employeeIds = (List<Integer>) request.get("employeeIds");
            
            // Validate input
            if (monthObj == null || yearObj == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Month and year are required"));
            }

            // Convert month to string (month name)
            String month = convertMonthToString(monthObj);
            Integer year = convertToInteger(yearObj);

            // Get employees based on selection
            List<Employee> employees;
            if (employeeIds != null && !employeeIds.isEmpty()) {
                employees = new ArrayList<>();
                for (Integer employeeId : employeeIds) {
                    Employee employee = employeeService.findEmployeeById(employeeId);
                    if (employee != null) {
                        employees.add(employee);
                    }
                }
            } else {
                // If no specific employees selected, process all employees
                employees = employeeService.getAllEmployees();
            }

            if (employees.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("No employees found in the system"));
            }

            List<Map<String, Object>> generatedPayslips = new ArrayList<>();
            int employeesWithCTC = 0;

            for (Employee employee : employees) {
                try {
                    // Get employee's current CTC
                    CTCDetails ctcDetails = ctcService.getCurrentCTCDetails(employee.getId());

                    if (ctcDetails != null) {
                        employeesWithCTC++;

                        // Generate payslip data from CTC
                        Map<String, Object> payslipData = generatePayslipFromCTC(employee, ctcDetails, month, year);

                        // Save payslip
                        try {
                            payslipService.savePayslip(payslipData);
                            generatedPayslips.add(payslipData);
                        } catch (Exception saveException) {
                            // Continue processing other employees even if one fails
                        }
                    }
                } catch (Exception e) {
                    // Continue processing other employees even if one fails
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", employeeIds != null && !employeeIds.isEmpty() 
                ? "Payslips generated for selected employees" 
                : "Monthly payslips generated for all employees");
            response.put("generated", generatedPayslips.size());
            response.put("totalEmployees", employees.size());
            response.put("employeesWithCTC", employeesWithCTC);
            response.put("selectedEmployees", employeeIds != null ? employeeIds.size() : 0);
            response.put("data", generatedPayslips);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
    // Add this method to PayrollController

    // Update the downloadPayslip method

    @Autowired
    private PayslipPDFService payslipPDFService;

    // Update the downloadPayslip method

    // Add this method or update the existing one

    @GetMapping("/payslips/download/{employeeId}/{month}/{year}")
    public ResponseEntity<?> downloadPayslip(@PathVariable Integer employeeId,
            @PathVariable String month,
            @PathVariable Integer year) {
        try {
            // Generate HTML payslip
            byte[] htmlBytes = payslipPDFService.generatePayslipPDF(employeeId, month.toUpperCase(), year);

            // Set headers for HTML download with proper filename
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("payslip_" + employeeId + "_" + month + "_" + year + ".html")
                    .build());
            headers.setContentLength(htmlBytes.length);
            headers.set("Access-Control-Expose-Headers", "Content-Disposition");

            return new ResponseEntity<>(htmlBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate payslip: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
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
    private Map<String, Object> generatePayslipFromCTC(Employee employee, CTCDetails ctcDetails, String month,
            Integer year) {
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
        BigDecimal monthlyBasicSalary = ctcDetails.getBasicSalary().divide(BigDecimal.valueOf(12), 2,
                RoundingMode.HALF_UP);
        BigDecimal monthlyHra = ctcDetails.getHra().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyAllowances = ctcDetails.getAllowances().divide(BigDecimal.valueOf(12), 2,
                RoundingMode.HALF_UP);
        BigDecimal monthlyBonuses = ctcDetails.getBonuses().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyPfContribution = ctcDetails.getPfContribution().divide(BigDecimal.valueOf(12), 2,
                RoundingMode.HALF_UP);
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
    
    // Helper method to convert month number to month name
    private String convertMonthToString(Object monthObj) {
        if (monthObj instanceof Integer) {
            Integer monthNum = (Integer) monthObj;
            String[] months = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                              "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
            if (monthNum >= 1 && monthNum <= 12) {
                return months[monthNum - 1];
            }
        } else if (monthObj instanceof String) {
            return ((String) monthObj).toUpperCase();
        }
        throw new IllegalArgumentException("Invalid month format");
    }
    
    // Helper method to convert object to Integer
    private Integer convertToInteger(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        } else if (obj instanceof Double) {
            return ((Double) obj).intValue();
        }
        throw new IllegalArgumentException("Invalid year format");
    }
}