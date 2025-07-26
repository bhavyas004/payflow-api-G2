package com.aptpath.payflowapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;

import com.aptpath.payflowapi.service.AuthService;

import io.swagger.v3.oas.annotations.media.Schema;
@Data
@Schema(description = "Manager Creation DTO")
public class ManagerDTO {
	
	@Schema(description = "Username must be unique")
    @NotBlank(message = "Username is required")
    private String username;
    
    @Schema(description = "Password (min 6 characters)")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$",
        message = "Password must contain letters and digits and be at least 6 characters"
    )
    private String password;

    @Schema(description = "Email address", example = "admin@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Contact number", example = "9876543210")
    @NotBlank(message = "Contact number is required")
    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Contact number must be a valid 10-digit Indian mobile number"
    )
    private String contactNumber;
    
    @Schema(description = "Role")
    @NotBlank(message = "Role is required")
    @Pattern(
        regexp = "HR|MANAGER",
        message = "Role must be HR or MANAGER"
    )
    private String role;
    
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
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
}