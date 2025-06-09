package com.loanapp.los.dto;

import com.loanapp.los.model.enums.LoanType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String customerPhone;
    
    private String customerEmail;
    
    private String customerIdentityNumber;
    
    @NotNull(message = "Loan amount is required")
    @Min(value = 1, message = "Loan amount must be positive")
    private BigDecimal loanAmount;
    
    @NotNull(message = "Loan type is required")
    private LoanType loanType;
    
    private String purpose;
    
    private Integer termMonths;
}