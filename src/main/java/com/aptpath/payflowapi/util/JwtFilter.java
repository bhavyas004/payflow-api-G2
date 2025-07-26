package com.aptpath.payflowapi.util;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {
    private JwtUtil jwtUtil;
    private UserRepository userRepo;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenExpired(token)) {
                String username = jwtUtil.extractUsername(token);
                User user = userRepo.findByUsername(username).orElse(null);
                if (user != null) {
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(user.getRole())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}