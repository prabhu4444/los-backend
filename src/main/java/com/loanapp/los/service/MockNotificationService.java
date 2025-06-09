package com.loanapp.los.service;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockNotificationService implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockNotificationService.class);
    
    @Override
    public void sendLoanSubmissionNotification(Loan loan) {
        logger.info("NOTIFICATION: SMS sent to customer {} at {} about loan submission {}", 
                loan.getCustomer().getName(), 
                loan.getCustomer().getPhone(), 
                loan.getId());
    }
    
    @Override
    public void sendCustomerLoanApprovalNotification(Loan loan) {
        logger.info("NOTIFICATION: SMS sent to customer {} at {} about loan approval {}", 
                loan.getCustomer().getName(), 
                loan.getCustomer().getPhone(), 
                loan.getId());
    }
    
    @Override
    public void sendCustomerLoanRejectionNotification(Loan loan) {
        logger.info("NOTIFICATION: SMS sent to customer {} at {} about loan rejection {}", 
                loan.getCustomer().getName(), 
                loan.getCustomer().getPhone(), 
                loan.getId());
    }
    
    @Override
    public void sendAgentAssignmentNotification(Agent agent, Loan loan) {
        logger.info("NOTIFICATION: Push notification sent to agent {} for loan assignment {}", 
                agent.getName(), 
                loan.getId());
    }
    
    @Override
    public void sendManagerNotification(Manager manager, Agent agent, Loan loan) {
        logger.info("NOTIFICATION: Push notification sent to manager {} about loan {} assigned to agent {}", 
                manager.getName(), 
                loan.getId(), 
                agent.getName());
    }
    
    @Override
    public void sendAgentDecisionNotification(Loan loan) {
        logger.info("NOTIFICATION: SMS sent to customer {} at {} about agent decision on loan {}: {}", 
                loan.getCustomer().getName(), 
                loan.getCustomer().getPhone(), 
                loan.getId(),
                loan.getApplicationStatus());
    }
}