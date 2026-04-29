package com.digitallending.application.entity;

import com.digitallending.application.model.LoanStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@NoArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    @Column(name = "self_reported_annual_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal selfReportedAnnualIncome;

    @Column(name = "self_reported_employer", nullable = false)
    private String selfReportedEmployer;

    @Column(name = "loan_amount_requested", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmountRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "loan_status")
    private LoanStatus status = LoanStatus.SUBMITTED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
