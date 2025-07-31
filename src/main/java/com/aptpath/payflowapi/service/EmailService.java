package com.aptpath.payflowapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendLeaveApprovalEmail(String toEmail, String employeeName, String startDate, String endDate, String remarks) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setSubject("Leave Request Approved - PayFlow");
            
            String body = String.format(
                "Dear %s,\n\n" +
                "Your leave request has been APPROVED.\n\n" +
                "Leave Details:\n" +
                "Start Date: %s\n" +
                "End Date: %s\n" +
                "%s" +
                "\nPlease ensure proper handover of your responsibilities before your leave begins.\n\n" +
                "Best regards,\n" +
                "PayFlow HR Team",
                employeeName,
                startDate,
                endDate,
                remarks != null && !remarks.isEmpty() ? "Manager Remarks: " + remarks + "\n" : ""
            );
            
            message.setText(body);
            mailSender.send(message);
            
            System.out.println("Leave approval email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send leave approval email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendLeaveRejectionEmail(String toEmail, String employeeName, String startDate, String endDate, String remarks) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setSubject("Leave Request Rejected - PayFlow");
            
            String body = String.format(
                "Dear %s,\n\n" +
                "We regret to inform you that your leave request has been REJECTED.\n\n" +
                "Leave Details:\n" +
                "Start Date: %s\n" +
                "End Date: %s\n" +
                "%s" +
                "\nIf you have any questions, please contact your manager or HR.\n\n" +
                "Best regards,\n" +
                "PayFlow HR Team",
                employeeName,
                startDate,
                endDate,
                remarks != null && !remarks.isEmpty() ? "Manager Remarks: " + remarks + "\n" : ""
            );
            
            message.setText(body);
            mailSender.send(message);
            
            System.out.println("Leave rejection email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send leave rejection email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendLeaveApplicationEmail(String toEmail, String managerName, String employeeName, String startDate, String endDate, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setSubject("New Leave Request - PayFlow");
            
            String body = String.format(
                "Dear %s,\n\n" +
                "A new leave request has been submitted by %s.\n\n" +
                "Leave Details:\n" +
                "Employee: %s\n" +
                "Start Date: %s\n" +
                "End Date: %s\n" +
                "Reason: %s\n" +
                "\nPlease review and take appropriate action in the PayFlow system.\n\n" +
                "Best regards,\n" +
                "PayFlow System",
                managerName,
                employeeName,
                employeeName,
                startDate,
                endDate,
                reason
            );
            
            message.setText(body);
            mailSender.send(message);
            
            System.out.println("Leave application notification sent successfully to manager: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send leave application email to manager: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
