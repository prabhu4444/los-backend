package com.loanapp.los.service;

import com.loanapp.los.dto.AgentDecisionRequest;
import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.AgentRepository;
import com.loanapp.los.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AgentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<Loan> getLoansAssignedToAgent(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));
        
        return loanRepository.findByAssignedAgentAndApplicationStatus(agent, ApplicationStatus.UNDER_REVIEW);
    }
    
    @Transactional
    public Loan processAgentDecision(Long agentId, Long loanId, AgentDecisionRequest decisionRequest) {
        // Verify agent exists
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));
        
        // Find the loan and verify it's assigned to this agent
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));
        
        if (loan.getAssignedAgent() == null || !loan.getAssignedAgent().getId().equals(agentId)) {
            throw new RuntimeException("Loan " + loanId + " is not assigned to agent " + agentId);
        }
        
        if (loan.getApplicationStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new RuntimeException("Loan is not in UNDER_REVIEW status");
        }
        
        // Check if time limit has expired (1 minute)
        if (loan.getAssignedAt() != null && 
            ChronoUnit.MINUTES.between(loan.getAssignedAt(), LocalDateTime.now()) >= 1) {
            throw new RuntimeException("Decision time has expired for this loan");
        }
        
        // Process the decision
        if ("APPROVE".equalsIgnoreCase(decisionRequest.getDecision())) {
            loan.setApplicationStatus(ApplicationStatus.APPROVED_BY_AGENT);
            logger.info("Loan {} approved by agent {}", loanId, agentId);
            
            // Send notification to customer
            notificationService.sendCustomerLoanApprovalNotification(loan);
        } else if ("REJECT".equalsIgnoreCase(decisionRequest.getDecision())) {
            loan.setApplicationStatus(ApplicationStatus.REJECTED_BY_AGENT);
            logger.info("Loan {} rejected by agent {}", loanId, agentId);
        } else {
            throw new IllegalArgumentException("Invalid decision: must be either APPROVE or REJECT");
        }
        
        // Update decision timestamp
        loan.setDecisionAt(LocalDateTime.now());
        
        // Save and return updated loan
        return loanRepository.save(loan);
    }
}
