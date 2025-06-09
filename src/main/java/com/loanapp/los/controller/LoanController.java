package com.loanapp.los.controller;

import com.loanapp.los.dto.LoanApplicationRequest;
import com.loanapp.los.dto.LoanApplicationResponse;
import com.loanapp.los.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> submitLoanApplication(
            @Valid @RequestBody LoanApplicationRequest request) {
        
        LoanApplicationResponse response = loanService.submitLoanApplication(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
