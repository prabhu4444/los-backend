package com.loanapp.los.repository;

import com.loanapp.los.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    List<Agent> findByAvailable(boolean available);
    
}
