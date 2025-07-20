package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

//    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String role;

    @Column(name = "is_first_login", nullable = false)
    private boolean isFirstLogin = true;

    @Column(length = 100)
    private String email;

    @Column(name = "contact_number", length = 15)
    private String contactNumber;

    @Column(name = "resetPasswordRequired", nullable = false)
    private boolean resetPasswordRequired = true;

    public enum Role {
        ADMIN, HR, MANAGER
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isFirstLogin() {
		return isFirstLogin;
	}

	public void setFirstLogin(boolean isFirstLogin) {
		this.isFirstLogin = isFirstLogin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public boolean getResetPasswordRequired() {
		return resetPasswordRequired;
	}

	public void setResetPasswordRequired(boolean resetPasswordRequired) {
		this.resetPasswordRequired = resetPasswordRequired;
	}
}
