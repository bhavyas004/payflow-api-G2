package com.aptpath.payflowapi.repository;


import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aptpath.payflowapi.entity.User;


public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    List<User> findByRoleInIgnoreCase(List<String> roles);
    
    boolean existsByRole(String role); 
    boolean existsByUsername(String username); 
    boolean existsByEmail(String email);  
    boolean existsByContactNumber(String ContactNumber);
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE UPPER(u.role) = UPPER(:role)")
    long countByRoleIgnoreCase(@org.springframework.data.repository.query.Param("role") String role);

    // Count total users (all roles)
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

}

