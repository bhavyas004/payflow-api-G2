package com.aptpath.payflowapi.dto;

import lombok.Data;
import java.util.List;
import java.util.Date;

@Data
public class EmployeeDTO {
	private Integer id;
	private String fullName;
	private int age;
	private String email;
	private String password;
	private String status;
	private List<ExperienceDTO> experiences;
	
	 public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ExperienceDTO> getExperiences() {
		return experiences;
	}
	public void setExperiences(List<ExperienceDTO> experiences) {
		this.experiences = experiences;
	}

	public static class ExperienceDTO {
		private String companyName;
		private Date startDate;
		private Date endDate;
		private String totalExperience;

		public String getCompanyName() {
			return companyName;
		}
		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}
		public Date getStartDate() {
			return startDate;
		}
		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}
		public Date getEndDate() {
			return endDate;
		}
		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}
		public String getTotalExperience() {
			return totalExperience;
		}
		public void setTotalExperience(String totalExperience) {
			this.totalExperience = totalExperience;
		}
	}
}

