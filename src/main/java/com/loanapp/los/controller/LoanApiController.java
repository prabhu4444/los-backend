package com.loanapp.los.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanApiController {

    @Autowired
    private LoanRepository loanRepository;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLoansByStatus(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);
        
        // Get loans by status with pagination
        Page<Loan> loanPage;
        if (status != null) {
            loanPage = loanRepository.findByApplicationStatus(status, pageable);
        } else {
            loanPage = loanRepository.findAll(pageable);
        }
        
        // Convert to DTOs to avoid circular references
        List<Map<String, Object>> simplifiedLoans = loanPage.getContent().stream()
            .map(loan -> {
                Map<String, Object> loanMap = new HashMap<>();
                loanMap.put("id", loan.getId());
                loanMap.put("loanAmount", loan.getLoanAmount());
                loanMap.put("loanType", loan.getLoanType());
                loanMap.put("applicationStatus", loan.getApplicationStatus());
                loanMap.put("createdAt", loan.getCreatedAt());
                loanMap.put("decisionAt", loan.getDecisionAt());
                
                // Only include essential customer info
                if (loan.getCustomer() != null) {
                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("id", loan.getCustomer().getId());
                    customerMap.put("name", loan.getCustomer().getName());
                    customerMap.put("email", loan.getCustomer().getEmail());
                    loanMap.put("customer", customerMap);
                }
                
                return loanMap;
            })
            .collect(Collectors.toList());
        
        // Build response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("loans", simplifiedLoans);
        response.put("currentPage", loanPage.getNumber());
        response.put("totalItems", loanPage.getTotalElements());
        response.put("totalPages", loanPage.getTotalPages());
        response.put("size", loanPage.getSize());
        
        return ResponseEntity.ok(response);
    }
}