package com.aptpath.payflowapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.aptpath.payflowapi.entity.User;
import com.aptpath.payflowapi.entity.Experience;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
	@OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Experience> experiences;

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

}

