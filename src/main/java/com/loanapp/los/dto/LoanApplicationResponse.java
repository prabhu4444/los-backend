package com.loanapp.los.dto;

import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.model.enums.LoanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanApplicationResponse {
    
    private Long loanId;
    private String customerName;
    private String customerPhone;
    private BigDecimal loanAmount;
    private LoanType loanType;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private String message;
}