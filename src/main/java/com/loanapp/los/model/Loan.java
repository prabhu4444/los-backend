package com.loanapp.los.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.model.enums.LoanType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;
    
    // We'll keep these fields for convenience in queries, but they're references to the Customer entity
    @Column(name = "customer_name", insertable = false, updatable = false)
    private String customerName;
    
    @Column(name = "customer_phone", insertable = false, updatable = false)
    private String customerPhone;
    
    private BigDecimal loanAmount;
    
    @Enumerated(EnumType.STRING)
    private LoanType loanType;
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent assignedAgent;
    
    private LocalDateTime assignedAt;
    
    private LocalDateTime decisionAt;
    
    // Optional: Additional loan information
    private String purpose;
    private Integer termMonths;
    private BigDecimal interestRate;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (applicationStatus == null) {
            applicationStatus = ApplicationStatus.APPLIED;
        }
        if (customer != null) {
            customerName = customer.getName();
            customerPhone = customer.getPhone();
        }
    }
}
