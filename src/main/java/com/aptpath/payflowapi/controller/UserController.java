package com.aptpath.payflowapi.controller;

import com.aptpath.payflowapi.service.AuthService;
import com.aptpath.payflowapi.service.EmployeeService;
import com.aptpath.payflowapi.dto.AuthResponse;
import com.aptpath.payflowapi.dto.EmployeeDTO;
import com.aptpath.payflowapi.dto.LoginDTO;
import com.aptpath.payflowapi.dto.ManagerDTO;
import com.aptpath.payflowapi.dto.ResetPasswordDTO;
import com.aptpath.payflowapi.entity.Employee;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.UserRepository;
import com.aptpath.payflowapi.dto.AdminDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AuthService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/admin/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody AdminDTO adminDTO) {
        userService.registerAdmin(adminDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Admin created successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponse message = userService.login(loginDTO);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/admin/create")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ManagerDTO managerDTO,@RequestHeader("Authorization") String token) {
    	token = token.substring(7);
        userService.createManager(managerDTO,token);
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "User created successfully");

        return ResponseEntity.ok(response);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordDTO dto, @RequestHeader("Authorization") String token) {
    	token = token.substring(7);
    	String message = userService.resetPassword(dto,token);
    	Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Reset Password successfully");

        return ResponseEntity.ok(response);
    }

       // Add this method to make /payflowapi/ public
    @GetMapping("/public")
    public ResponseEntity<String> publicRoot() {
        return ResponseEntity.ok("Welcome to PayFlow API!");
    }

    @GetMapping("/test-db")
    public String testDb() {
        long count = userService.getUserRepository().count();
        return "User count: " + count;
    }

    @CrossOrigin
    @GetMapping("/counts")
    public Map<String, Long> getUserCounts() {
        long hrCount = userService.getUserRepository().countByRoleIgnoreCase("HR");
        long managerCount = userService.getUserRepository().countByRoleIgnoreCase("MANAGER");
        Map<String, Long> result = new HashMap<>();
        result.put("HR", hrCount);
        result.put("MANAGER", managerCount);
        return result;
    }
}
