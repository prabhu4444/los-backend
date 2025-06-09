package com.loanapp.los.repository;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    // Find loans by status
    List<Loan> findByApplicationStatus(ApplicationStatus status);
    
    // Find loans by status with pagination
    Page<Loan> findByApplicationStatus(ApplicationStatus status, Pageable pageable);
    
    // Find loans by status ordered by creation date (newest first)
    List<Loan> findByApplicationStatusOrderByCreatedAtDesc(ApplicationStatus status);
    
    // Find loans by assigned agent and status
    List<Loan> findByAssignedAgentAndApplicationStatus(Agent agent, ApplicationStatus status);
    
    // Find recent approved/rejected loans
    List<Loan> findTop10ByApplicationStatusOrderByDecisionAtDesc(ApplicationStatus status);
    
    // Get count of loans by status
    @org.springframework.data.jpa.repository.Query("SELECT l.applicationStatus as status, COUNT(l) as count FROM Loan l GROUP BY l.applicationStatus")
    List<Map<String, Object>> countLoansGroupByStatus();
    
    // Count loans by status
    long countByApplicationStatus(ApplicationStatus status);
}
