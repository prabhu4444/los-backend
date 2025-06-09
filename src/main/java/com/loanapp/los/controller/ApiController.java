package com.loanapp.los.controller;

import com.loanapp.los.dto.TopCustomerDTO;
import com.loanapp.los.service.CustomerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private CustomerStatisticsService customerStatisticsService;
    
    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        List<TopCustomerDTO> topCustomers = customerStatisticsService.getTopCustomersByLoanAmount(limit);
        return ResponseEntity.ok(topCustomers);
    }
    
    @GetMapping("/top-customers/approved")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomersByApprovedLoans(
            @RequestParam(defaultValue = "3") int limit) {
        List<TopCustomerDTO> topCustomers = customerStatisticsService.getTopCustomersByApprovedLoans(limit);
        return ResponseEntity.ok(topCustomers);
    }
}