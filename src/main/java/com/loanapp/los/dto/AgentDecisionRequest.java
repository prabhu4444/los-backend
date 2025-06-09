package com.loanapp.los.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgentDecisionRequest {
    
    @NotNull(message = "Decision is required")
    private String decision;  // "APPROVE" or "REJECT"
    
    private String comments;  
}