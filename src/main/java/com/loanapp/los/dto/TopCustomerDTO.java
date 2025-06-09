package com.loanapp.los.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDTO {
    private Long id;
    private String name;
    private String email;
    private BigDecimal totalLoanAmount;
    private int totalLoanCount;
    private int approvedLoanCount;
    
    public int getApprovalRate() {
        if (totalLoanCount == 0) return 0;
        return (int) Math.round((double) approvedLoanCount / totalLoanCount * 100);
    }
}