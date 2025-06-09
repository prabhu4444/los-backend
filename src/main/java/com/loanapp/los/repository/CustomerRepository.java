package com.loanapp.los.repository;

import com.loanapp.los.model.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByPhone(String phone);
    
    Optional<Customer> findByIdentityNumber(String identityNumber);
    
    @Query("SELECT c FROM Customer c JOIN c.loans l WHERE l.applicationStatus = 'APPROVED_BY_SYSTEM' OR l.applicationStatus = 'APPROVED_BY_AGENT' GROUP BY c ORDER BY COUNT(l) DESC")
    List<Customer> findTopCustomersByApprovedLoans(Pageable pageable);
}
