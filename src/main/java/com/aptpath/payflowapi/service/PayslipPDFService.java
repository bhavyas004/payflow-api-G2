package com.aptpath.payflowapi.service;

import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.Payslip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PayslipPDFService {
    
    @Autowired
    private PayslipService payslipService;
    
    @Autowired
    private EmployeeService employeeService;
    
    public byte[] generatePayslipPDF(Integer employeeId, String month, Integer year) throws IOException {
        // Get payslip data
        Optional<Payslip> payslipOpt = payslipService.getPayslipByEmployeeMonthYear(employeeId, month, year);
        if (payslipOpt.isEmpty()) {
            throw new RuntimeException("Payslip not found for employee " + employeeId + " for " + month + " " + year);
        }
        
        Payslip payslip = payslipOpt.get();
        Employee employee = employeeService.findEmployeeById(employeeId);
        
        if (employee == null) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }
        
        // Generate HTML content
        String htmlContent = generatePayslipHTML(payslip, employee);
        return htmlContent.getBytes(StandardCharsets.UTF_8);
    }
    
    private String generatePayslipHTML(Payslip payslip, Employee employee) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        // Calculate salary components
        BigDecimal grossSalary = payslip.getNetPay().add(payslip.getDeductions());
        BigDecimal basicSalary = grossSalary.multiply(new BigDecimal("0.8"));
        BigDecimal hra = grossSalary.multiply(new BigDecimal("0.1"));
        BigDecimal allowances = grossSalary.multiply(new BigDecimal("0.05"));
        BigDecimal bonuses = grossSalary.multiply(new BigDecimal("0.05"));
        BigDecimal pfContribution = payslip.getDeductions().multiply(new BigDecimal("0.6"));
        BigDecimal tax = payslip.getDeductions().multiply(new BigDecimal("0.4"));
        
        // Safe string formatting with proper null handling
        String employeeName = employee.getFullName() != null ? employee.getFullName() : "N/A";
        String generatedDate = payslip.getGeneratedOn().format(formatter);
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Payslip - ").append(payslip.getMonth()).append(" ").append(payslip.getYear()).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; font-size: 14px; background: white; }\n");
        html.append("        .header { text-align: center; border-bottom: 3px solid #333; padding-bottom: 15px; margin-bottom: 25px; }\n");
        html.append("        .company-name { font-size: 24px; font-weight: bold; color: #2c3e50; margin-bottom: 8px; }\n");
        html.append("        .payslip-title { font-size: 18px; color: #34495e; }\n");
        html.append("        .info-section { margin: 25px 0; background: #f8f9fa; padding: 15px; border-radius: 5px; }\n");
        html.append("        .info-row { display: flex; justify-content: space-between; margin: 8px 0; padding: 5px 0; }\n");
        html.append("        .info-label { font-weight: bold; color: #2c3e50; }\n");
        html.append("        .salary-table { width: 100%; border-collapse: collapse; margin: 25px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .salary-table th, .salary-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
        html.append("        .salary-table th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-weight: bold; }\n");
        html.append("        .salary-table tr:nth-child(even) { background-color: #f8f9fa; }\n");
        html.append("        .total-row { font-weight: bold; background: #e3f2fd !important; color: #1976d2; }\n");
        html.append("        .net-pay-row { font-weight: bold; background: #c8e6c9 !important; color: #388e3c; font-size: 16px; }\n");
        html.append("        .amount { text-align: right; font-family: 'Courier New', monospace; font-weight: bold; }\n");
        html.append("        .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #7f8c8d; border-top: 1px solid #ecf0f1; padding-top: 20px; }\n");
        html.append("        @media print { body { margin: 0; } .header { page-break-after: avoid; } }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("    <div class=\"header\">\n");
        html.append("        <div class=\"company-name\">PayFlow Solutions</div>\n");
        html.append("        <div class=\"payslip-title\">Salary Slip for ").append(payslip.getMonth()).append(" ").append(payslip.getYear()).append("</div>\n");
        html.append("    </div>\n");
        
        // Employee Information
        html.append("    <div class=\"info-section\">\n");
        html.append("        <div class=\"info-row\">\n");
        html.append("            <span class=\"info-label\">Employee Name:</span>\n");
        html.append("            <span>").append(employeeName).append("</span>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"info-row\">\n");
        html.append("            <span class=\"info-label\">Employee ID:</span>\n");
        html.append("            <span>").append(employee.getId()).append("</span>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"info-row\">\n");
        html.append("            <span class=\"info-label\">Pay Period:</span>\n");
        html.append("            <span>").append(payslip.getMonth()).append(" ").append(payslip.getYear()).append("</span>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"info-row\">\n");
        html.append("            <span class=\"info-label\">Generated On:</span>\n");
        html.append("            <span>").append(generatedDate).append("</span>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Salary Table
        html.append("    <table class=\"salary-table\">\n");
        html.append("        <thead>\n");
        html.append("            <tr>\n");
        html.append("                <th style=\"width: 25%;\">Earnings</th>\n");
        html.append("                <th style=\"width: 25%;\">Amount (₹)</th>\n");
        html.append("                <th style=\"width: 25%;\">Deductions</th>\n");
        html.append("                <th style=\"width: 25%;\">Amount (₹)</th>\n");
        html.append("            </tr>\n");
        html.append("        </thead>\n");
        html.append("        <tbody>\n");
        html.append("            <tr>\n");
        html.append("                <td>Basic Salary</td>\n");
        html.append("                <td class=\"amount\">").append(String.format("%.2f", basicSalary)).append("</td>\n");
        html.append("                <td>PF Contribution</td>\n");
        html.append("                <td class=\"amount\">").append(String.format("%.2f", pfContribution)).append("</td>\n");
        html.append("            </tr>\n");
        html.append("            <tr>\n");
        html.append("                <td>HRA</td>\n");
        html.append("                <td class=\"amount\">").append(String.format("%.2f", hra)).append("</td>\n");
        html.append("                <td>Income Tax</td>\n");
        html.append("                <td class=\"amount\">").append(String.format("%.2f", tax)).append("</td>\n");
        html.append("            </tr>\n");
        html.append("            <tr>\n");
        html.append("                <td>Allowances</td>\n");
        html.append("                <td class=\"amount\">").append(String.format("%.2f", allowances)).append("</td>\n");
        html.append("                <td>Other Deductions</td>\n");
        html.append("                <td class=\"amount\">0.00</td>\n");
        html.append("            </tr>\n");
        html.append("            <tr>\n");
        html.append("                <td>Bonuses</td>\n");
        html.append("                <td class=\"amount\">").append(String.format("%.2f", bonuses)).append("</td>\n");
        html.append("                <td></td>\n");
        html.append("                <td></td>\n");
        html.append("            </tr>\n");
        html.append("            <tr class=\"total-row\">\n");
        html.append("                <td><strong>Total Earnings</strong></td>\n");
        html.append("                <td class=\"amount\"><strong>").append(String.format("%.2f", grossSalary)).append("</strong></td>\n");
        html.append("                <td><strong>Total Deductions</strong></td>\n");
        html.append("                <td class=\"amount\"><strong>").append(String.format("%.2f", payslip.getDeductions())).append("</strong></td>\n");
        html.append("            </tr>\n");
        html.append("            <tr class=\"net-pay-row\">\n");
        html.append("                <td colspan=\"2\" style=\"text-align: center;\"><strong>NET SALARY</strong></td>\n");
        html.append("                <td colspan=\"2\" class=\"amount\"><strong>₹ ").append(String.format("%.2f", payslip.getNetPay())).append("</strong></td>\n");
        html.append("            </tr>\n");
        html.append("        </tbody>\n");
        html.append("    </table>\n");
        
        // Unpaid Leaves Section
        html.append("    <div class=\"info-section\" style=\"margin-top: 20px;\">\n");
        html.append("        <div class=\"info-row\">\n");
        html.append("            <span class=\"info-label\">Unpaid Leaves:</span>\n");
        html.append("            <span>" + payslip.getUnpaidLeaves() + "</span>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"info-row\">\n");
        html.append("            <span class=\"info-label\">Unpaid Leave Deduction:</span>\n");
        html.append("            <span>₹ " + String.format("%.2f", payslip.getUnpaidLeaveDeduction()) + "</span>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");

        // Footer
        html.append("    <div class=\"footer\">\n");
        html.append("        <p><strong>This is a computer-generated payslip and does not require a signature.</strong></p>\n");
        html.append("        <p>Generated by PayFlow System | ").append(generatedDate).append("</p>\n");
        html.append("        <p style=\"margin-top: 10px; font-size: 10px;\">\n");
        html.append("            For any queries regarding this payslip, please contact HR Department\n");
        html.append("        </p>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
}