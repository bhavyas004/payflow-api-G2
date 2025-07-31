package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.entity.Experience;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;

    private int age;
    private String email;
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "username")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Experience> experiences;
    
    @Column(name = "manager")
    private String manager;

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public User getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public List<Experience> getExperiences() {
		return experiences;
	}
	
	public void setExperiences(List<Experience> experiences) {
		this.experiences = experiences;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getManager() {
		return manager;
	}
	
	public void setManager(String manager) {
		this.manager = manager;
	}

}

