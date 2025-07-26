package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "username")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    @Column(name = "total_experience")
    private String totalExperience; // Store as formatted string like "2 years 3 months"
    
	@OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Experience> experiences = new ArrayList<>();

    // 👇 This is where you add the setter method
    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
        // Link each experience to this employee
        if (experiences != null) {
            for (Experience exp : experiences) {
                exp.setEmployee(this); // foreign key link
            }
        }
    }

    // You should also include a getter if needed
    public List<Experience> getExperiences() {
        return experiences;
    }
    
    public String getTotalExperience() {
        return totalExperience;
    }
    
    public void setTotalExperience(String totalExperience) {
        this.totalExperience = totalExperience;
    }

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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}

