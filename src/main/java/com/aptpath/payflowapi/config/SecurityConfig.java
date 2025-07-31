package com.aptpath.payflowapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                		"/user/login",
                		"/user/admin/register",
                		"/user/admin/create",
                		"/user/reset-password",
                		"/onboard-employee/add",
                        "/onboard-employee/employees",
                        "/onboard-employee/{id}/status",
                        "/stats/**",
                		"/v3/api-docs/**",
                		"/swagger-ui/**",
                		"/swagger-ui.html",
                        "/",
                		"/payflowapi/",
                		"/user/public",
                		"/payflowapi/public",
                		"/user/test-db",
                        "/user/counts",
                        "/user/hr-managers",
                        "/onboard-employee/login",
                        "/payflowapi/onboard-employee/login",
                        "/payflowapi/onboard-employee/set-password",
                        "/leave-requests/apply" ,
                        "/leave-requests/**",
                        "/managers/available",
                        "/managers/assign",
                        "/managers/assigned",
                        "/payflowapi/leave-requests/**",
                        "/payflowapi/managers/available",
                        "/payflowapi/managers/assign",
                        "/payflowapi/managers/assigned",

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
                        "/payflowapi/leave-requests/**" ,
                        "/payflowapi/user/test-db",
                        "/payflowapi/user/counts",
                        "/payflowapi/user/hr-managers",
                        "/payflowapi/onboard-employee/**",
                        "/payflowapi/stats/**"
                		).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getOutputStream().println("{ \"error\": \"" + authException.getMessage() + "\" }");
                })
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}
