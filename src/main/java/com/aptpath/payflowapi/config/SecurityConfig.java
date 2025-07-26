package com.aptpath.payflowapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                		"/user/login",
                		"/user/admin/register",
                		"/user/admin/create",
                		"/user/reset-password",
                		"/onboard-employee/add",
                        "/onboard-employee/employees",
                        "/onboard-employee/{id}/status",
                		"/v3/api-docs/**",
                		"/swagger-ui/**",
                		"/swagger-ui.html",
                        "/",
                		"/payflowapi/",
                		"/user/public",
                		"/payflowapi/public",
                		"/user/test-db",
                        "/user/counts",
                        // Add context-path-prefixed versions for Spring Security matching
                        "/payflowapi/user/login",
                        "/payflowapi/user/admin/register",
                        "/payflowapi/user/admin/create",
                        "/payflowapi/user/reset-password",
                        "/payflowapi/employee/create",
                        "/payflowapi/v3/api-docs/**",
                        "/payflowapi/swagger-ui/**",
                        "/payflowapi/swagger-ui.html",
                        "/payflowapi/user/public",
                        "/payflowapi/user/test-db",
                        "/payflowapi/user/counts"
                		).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling()
            .authenticationEntryPoint((request, response, authException) -> {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getOutputStream().println("{ \"error\": \"" + authException.getMessage() + "\" }");
            })
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
