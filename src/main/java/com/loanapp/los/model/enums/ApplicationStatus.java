package com.loanapp.los.model.enums;

public enum ApplicationStatus {
    APPLIED,               // Initial state when loan is first submitted
    PROCESSING,            // Being processed by the system
    PENDING_REVIEW,        // Processed by rules and needs human review, waiting for agent
    APPROVED_BY_SYSTEM,    // Automatically approved
    REJECTED_BY_SYSTEM,    // Automatically rejected
    UNDER_REVIEW,          // Assigned to an agent for review
    APPROVED_BY_AGENT,     // Approved after human review
    REJECTED_BY_AGENT      // Rejected after human review
}