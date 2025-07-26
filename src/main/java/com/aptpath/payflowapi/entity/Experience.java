package com.aptpath.payflowapi.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Entity
@Table(name = "experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String companyName;
    private Date startDate;
    private Date endDate;
    private String totalExperience; // e.g. "2 years, 3 months"
}