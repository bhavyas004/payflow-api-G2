package com.aptpath.payflowapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.UserRepository;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEmail("admin@payflow.com");
            admin.setContactNumber("1234567890");
            admin.setFirstLogin(false);
            admin.setResetPasswordRequired(false);
            userRepository.save(admin);
            System.out.println("Created admin user: admin/admin123");
        }

        // Create HR user if not exists
        if (!userRepository.existsByUsername("hr")) {
            User hr = new User();
            hr.setUsername("hr");
            hr.setPassword(passwordEncoder.encode("hr123"));
            hr.setRole("HR");
            hr.setEmail("hr@payflow.com");
            hr.setContactNumber("1234567891");
            hr.setFirstLogin(false);
            hr.setResetPasswordRequired(false);
            userRepository.save(hr);
            System.out.println("Created HR user: hr/hr123");
        }

        // Create Manager user if not exists
        if (!userRepository.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole("MANAGER");
            manager.setEmail("manager@payflow.com");
            manager.setContactNumber("1234567892");
            manager.setFirstLogin(false);
            manager.setResetPasswordRequired(false);
            userRepository.save(manager);
            System.out.println("Created Manager user: manager/manager123");
        }
    }
}
