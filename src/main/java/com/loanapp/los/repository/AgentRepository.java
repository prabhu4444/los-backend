package com.loanapp.los.repository;

import com.loanapp.los.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    // Simple method to find available agents
    List<Agent> findByAvailable(boolean available);
    
    // If we need to find the agent with least workload in the future, we can add that method
}
