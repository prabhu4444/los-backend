package com.loanapp.los.service;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.AgentRepository;
import com.loanapp.los.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class LoanProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoanProcessingService.class);
    private static final Random random = new Random();
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private AgentAssignmentService agentAssignmentService;
    
    // Add this field if not already present
    @Autowired
    private NotificationService notificationService;
    
    // Check for new loan applications every 30 seconds
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processNewLoans() {
        List<Loan> pendingLoans = loanRepository.findByApplicationStatus(ApplicationStatus.APPLIED);
        
        logger.info("Found {} pending loan applications to process", pendingLoans.size());
        
        for (Loan loan : pendingLoans) {
            processLoanAsync(loan.getId());
        }
    }
    
    @Async("loanProcessingExecutor")
    @Transactional
    public void processLoanAsync(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));
        
        // Skip if loan is not in APPLIED state (might have been picked up by another thread)
        if (loan.getApplicationStatus() != ApplicationStatus.APPLIED) {
            logger.info("Loan {} is no longer in APPLIED status, skipping processing", loanId);
            return;
        }
        
        // Mark as processing
        loan.setApplicationStatus(ApplicationStatus.PROCESSING);
        loanRepository.save(loan);
        
        // Simulate processing delay (random time between 2-7 seconds)
        int delaySeconds = 2 + random.nextInt(5);
        logger.info("Processing loan {} with a simulated delay of {} seconds", loanId, delaySeconds);
        
        try {
            TimeUnit.SECONDS.sleep(delaySeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Loan processing interrupted for loan {}", loanId);
            return;
        }
        
        // Apply business rules to determine loan outcome
        logger.info("About to apply loan rules for loan {} of type {} with amount {}", 
            loanId, loan.getLoanType(), loan.getLoanAmount());
        ApplicationStatus decision = applyLoanRules(loan);
        logger.info("Decision from loan rules for loan {}: {}", loanId, decision);
        
        // If automatic approval/rejection, update status directly
        if (decision == ApplicationStatus.APPROVED_BY_SYSTEM || 
            decision == ApplicationStatus.REJECTED_BY_SYSTEM) {
            
            loan.setApplicationStatus(decision);
            loan.setDecisionAt(LocalDateTime.now());
            loanRepository.save(loan);
            logger.info("Loan {} processed with decision: {}", loanId, decision);
            
            // Send notification based on decision
            if (decision == ApplicationStatus.APPROVED_BY_SYSTEM) {
                notificationService.sendCustomerLoanApprovalNotification(loan);
            } else {
                notificationService.sendCustomerLoanRejectionNotification(loan);
            }
            
        } else if (decision == ApplicationStatus.PENDING_REVIEW) {
            // For loans needing review, mark as PENDING_REVIEW
            // (they'll be picked up by the agent assignment scheduler)
            loan.setApplicationStatus(ApplicationStatus.PENDING_REVIEW);
            loanRepository.save(loan);
            logger.info("Loan {} marked as PENDING_REVIEW for agent assignment", loanId);
        }
    }
    
    private ApplicationStatus applyLoanRules(Loan loan) {
        // Simple rule-based decision logic
        
        // Rule 1: HOME loans always need review
        if (loan.getLoanType() == com.loanapp.los.model.enums.LoanType.HOME) {
            return ApplicationStatus.PENDING_REVIEW;
        }
        
        // Rule 2: BUSINESS loans always need review
        if (loan.getLoanType() == com.loanapp.los.model.enums.LoanType.BUSINESS) {
            return ApplicationStatus.PENDING_REVIEW;
        }
        
        // Rule 3: For PERSONAL and AUTO loans, amount > 12,000 are rejected by system
        if ((loan.getLoanType() == com.loanapp.los.model.enums.LoanType.PERSONAL || 
             loan.getLoanType() == com.loanapp.los.model.enums.LoanType.AUTO) && 
            loan.getLoanAmount().compareTo(new BigDecimal("12000")) > 0) {
            return ApplicationStatus.REJECTED_BY_SYSTEM;
        }
        
        // Rule 4: For PERSONAL and AUTO loans <= 12,000, automatically approve
        if (loan.getLoanType() == com.loanapp.los.model.enums.LoanType.PERSONAL || 
            loan.getLoanType() == com.loanapp.los.model.enums.LoanType.AUTO) {
            return ApplicationStatus.APPROVED_BY_SYSTEM;
        }
        
        // Fallback - anything else goes to review
        return ApplicationStatus.PENDING_REVIEW;
    }
    
    // Check for loans that need agent assignment every 10 seconds
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void assignLoansToAgents() {
        // Look for loans in PENDING_REVIEW status instead of APPLIED
        List<Loan> readyForReviewLoans = loanRepository.findByApplicationStatus(ApplicationStatus.PENDING_REVIEW);
        
        if (readyForReviewLoans.isEmpty()) {
            return; // No loans to process
        }
        
        logger.info("Found {} loans that need agent assignment", readyForReviewLoans.size());
        
        // Get all available agents
        List<Agent> availableAgents = agentRepository.findByAvailable(true);
        logger.info("Found {} available agents for assignment", availableAgents.size());
        
        if (availableAgents.isEmpty()) {
            logger.info("No agents available for assignment at this time");
            return;
        }
        
        // Log the available agents
        for (Agent agent : availableAgents) {
            logger.info("Available agent: ID={}, Name={}, Email={}", 
                    agent.getId(), agent.getName(), agent.getEmail());
        }
        
        // Simple round-robin assignment - assign loans to available agents
        int agentIndex = 0;
        for (Loan loan : readyForReviewLoans) {
            if (agentIndex >= availableAgents.size()) {
                logger.info("No more available agents for remaining {} loans", 
                        readyForReviewLoans.size() - agentIndex);
                break; // No more agents available
            }
            
            Agent agent = availableAgents.get(agentIndex);
            
            // Mark the agent as unavailable
            agent.setAvailable(false);
            agentRepository.save(agent);
            
            // Assign loan to agent
            loan.setAssignedAgent(agent);
            loan.setAssignedAt(LocalDateTime.now());
            loan.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);
            loanRepository.save(loan);
            
            logger.info("Loan {} assigned to agent {}. Agent is now unavailable until decision.", 
                    loan.getId(), agent.getName());
            
            // Move to next agent
            agentIndex++;
        }
    }
}
