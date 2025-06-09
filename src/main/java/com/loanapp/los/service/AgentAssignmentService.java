package com.loanapp.los.service;

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
import java.util.List;

@Service
public class AgentAssignmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentAssignmentService.class);
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Assigns a loan to an available agent.
     * @param loan The loan to assign
     * @return true if assignment was successful, false if no agents available
     */
    @Transactional
    public boolean assignLoanToAgent(Loan loan) {
        // Find an available agent
        List<Agent> availableAgents = agentRepository.findByAvailable(true);
        
        // Log all available agents - keep this for debugging
        logger.info("Found {} available agents for loan {}", availableAgents.size(), loan.getId());
        
        // Log details of each available agent - only if there are any
        if (availableAgents.size() > 0) {
            logger.info("Available agents for loan {}:", loan.getId());
            for (Agent agent : availableAgents) {
                logger.info("  - ID={}, Name={}, Email={}", 
                        agent.getId(), agent.getName(), agent.getEmail());
            }
        }
        
        if (!availableAgents.isEmpty()) {
            // Choose an agent from the available agents
            Agent agent = availableAgents.get(0);
            
            logger.info("Selected agent {} for loan {}", agent.getName(), loan.getId());
            
            // Mark the agent as unavailable
            agent.setAvailable(false);
            agentRepository.save(agent);
            
            // Assign the loan to the agent
            loan.setAssignedAgent(agent);
            loan.setAssignedAt(LocalDateTime.now());
            
            // Only change status to UNDER_REVIEW after agent is assigned
            loan.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);
            
            loanRepository.save(loan);
            
            logger.info("Loan {} assigned to agent {}. Agent is now unavailable until decision.", 
                    loan.getId(), agent.getName());
            
            // Send notifications if needed
            try {
                notificationService.sendAgentAssignmentNotification(agent, loan);
        if (agent.getManager() != null) {
            notificationService.sendManagerNotification(agent.getManager(), agent, loan);
        }
            } catch (Exception e) {
                logger.error("Error sending notifications: {}", e.getMessage());
            }
            
            return true;
        } else {
            logger.warn("No available agents to assign loan {}", loan.getId());
            return false;
        }
    }
}