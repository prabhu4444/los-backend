package com.loanapp.los.service;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.Manager;

public interface NotificationService {
    
    /**
     * Send notification to agent when loan is assigned for review
     */
    void sendAgentAssignmentNotification(Agent agent, Loan loan);
    
    /**
     * Send notification to manager about agent assignment
     */
    void sendManagerNotification(Manager manager, Agent agent, Loan loan);
    
    /**
     * Send notification to customer when loan is automatically approved
     */
    void sendCustomerLoanApprovalNotification(Loan loan);
    
    /**
     * Send notification to customer when loan is submitted
     */
    void sendLoanSubmissionNotification(Loan loan);
    
    /**
     * Send notification to customer when loan is automatically rejected
     */
    void sendCustomerLoanRejectionNotification(Loan loan);
    
    /**
     * Send notification to customer about agent decision
     */
    void sendAgentDecisionNotification(Loan loan);
}
