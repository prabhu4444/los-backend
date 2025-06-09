package com.loanapp.los.controller;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.AgentRepository;
import com.loanapp.los.repository.LoanRepository;
import com.loanapp.los.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private AgentRepository agentRepository;

    // Add this field if not already present
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/decision/{loanId}")
    public ResponseEntity<?> processLoanDecision(
            @PathVariable Long loanId,
            @RequestBody Map<String, String> decisionRequest) {
        
        String decision = decisionRequest.get("decision");
        if (!"APPROVE".equals(decision) && !"REJECT".equals(decision)) {
            return ResponseEntity.badRequest().body("Invalid decision: must be APPROVE or REJECT");
        }
        
        // Find the loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));
        
        // Check if it's in UNDER_REVIEW status
        if (loan.getApplicationStatus() != ApplicationStatus.UNDER_REVIEW) {
            return ResponseEntity.badRequest().body("Loan is not in UNDER_REVIEW status");
        }
        
        // Process the decision
        if ("APPROVE".equals(decision)) {
            loan.setApplicationStatus(ApplicationStatus.APPROVED_BY_AGENT);
        } else {
            loan.setApplicationStatus(ApplicationStatus.REJECTED_BY_AGENT);
        }
        
        loan.setDecisionAt(LocalDateTime.now());
        
        // If there's an assigned agent, make them available again
        if (loan.getAssignedAgent() != null) {
            Agent agent = loan.getAssignedAgent();
            agent.setAvailable(true);  // Make agent available again
            agentRepository.save(agent);
        }
        
        loanRepository.save(loan);
        
        // Send notification about the decision
        notificationService.sendAgentDecisionNotification(loan);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/reset-database")
    public ResponseEntity<String> resetDatabase() {
        try {
            // Delete all loans
            loanRepository.deleteAll();
            
            // Reset agent availability
            List<Agent> agents = agentRepository.findAll();
            for (Agent agent : agents) {
                agent.setAvailable(true);
                agentRepository.save(agent);
            }
            
            return ResponseEntity.ok("Database reset successful! All loan data has been cleared.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error resetting database: " + e.getMessage());
        }
    }
}
