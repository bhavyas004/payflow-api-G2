package com.aptpath.payflowapi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.UserRepository;
import com.aptpath.payflowapi.util.JwtUtil;

import jakarta.validation.Valid;

import com.aptpath.payflowapi.dto.AdminDTO;
import com.aptpath.payflowapi.dto.AuthResponse;
import com.aptpath.payflowapi.dto.LoginDTO;
import com.aptpath.payflowapi.dto.ManagerDTO;
import com.aptpath.payflowapi.dto.RegisterRequest;
import com.aptpath.payflowapi.dto.ResetPasswordDTO;
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    public void registerAdmin(AdminDTO adminDTO) {
    	if (userRepository.existsByRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Admin already exists");
        }
//        if (userRepository.existsByUsername(adminDTO.getUsername())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
//        }
//        if (userRepository.existsByEmail(adminDTO.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
//        }
//        if (userRepository.existsByContactNumber(adminDTO.getContactNumber())) {
//        	throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact number already in use");
//        }

        User user = new User();
        user.setUsername(adminDTO.getUsername());
        user.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        user.setEmail(adminDTO.getEmail());
        user.setRole("ADMIN");
        user.setContactNumber(adminDTO.getContactNumber());

        userRepository.save(user);
    }
    
    public void createManager(ManagerDTO managerDTO ,  String headerToken) {
    	
    	if (!jwtUtil.validateTokenForAction(headerToken, "ADMIN")) {
    	    throw new RuntimeException("Only admin can create HR or Manager");
    	}
        if (userRepository.existsByUsername(managerDTO.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(managerDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByContactNumber(managerDTO.getContactNumber())) {
        	throw new ResponseStatusException(HttpStatus.CONFLICT, "Contact number already in use");
        }

        User user = new User();
        user.setUsername(managerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(managerDTO.getPassword()));
        user.setEmail(managerDTO.getEmail());
        user.setRole(managerDTO.getRole());
        user.setContactNumber(managerDTO.getContactNumber());

        userRepository.save(user);
    }

    public AuthResponse login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        Map<String, Object> claims = new HashMap<>();
        if(!user.getRole().equals("ADMIN")) {
        	claims.put("isFirstLogin", user.isFirstLogin());
            claims.put("resetPasswordRequired", user.getResetPasswordRequired());
        }
        claims.put("username",user.getUsername());
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(user.getUsername(),claims);
        user.setFirstLogin(false);
        userRepository.save(user);
        return new AuthResponse(token);
    }

    public String resetPassword(ResetPasswordDTO dto, String token) {
    	if (!jwtUtil.validateUsername(token, dto.getUsername())) {
    	    throw new RuntimeException("Username mismatch - unauthorized password reset");
    	}
    	
        User user = userRepository.findByUsername(dto.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setResetPasswordRequired(false);
        user.setFirstLogin(false);
        userRepository.save(user);

        return "Password reset successfully";
    }

   
}