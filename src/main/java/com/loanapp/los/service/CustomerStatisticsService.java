package com.loanapp.los.service;

import com.loanapp.los.dto.TopCustomerDTO;
import com.loanapp.los.model.Customer;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.CustomerRepository;
import com.loanapp.los.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerStatisticsService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Get top customers by total approved loan count
     * @param limit Number of top customers to return
     * @return List of top customers
     */
    public List<TopCustomerDTO> getTopCustomersByApprovedLoans(int limit) {
        List<Loan> allLoans = loanRepository.findAll();
        
        // Group loans by customer
        Map<Customer, Integer> customerApprovedCountMap = new HashMap<>();
        Map<Customer, Integer> customerTotalCountMap = new HashMap<>();
        Map<Customer, BigDecimal> customerTotalAmountMap = new HashMap<>();
        
        for (Loan loan : allLoans) {
            Customer customer = loan.getCustomer();
            
            // Update total loan count
            int currentCount = customerTotalCountMap.getOrDefault(customer, 0);
            customerTotalCountMap.put(customer, currentCount + 1);
            
            // Update total loan amount
            BigDecimal currentAmount = customerTotalAmountMap.getOrDefault(customer, BigDecimal.ZERO);
            customerTotalAmountMap.put(customer, currentAmount.add(loan.getLoanAmount()));
            
            // Update approved loan count
            if (loan.getApplicationStatus() == ApplicationStatus.APPROVED_BY_SYSTEM || 
                loan.getApplicationStatus() == ApplicationStatus.APPROVED_BY_AGENT) {
                int approvedCount = customerApprovedCountMap.getOrDefault(customer, 0);
                customerApprovedCountMap.put(customer, approvedCount + 1);
            }
        }
        
        // Convert to DTOs and sort by approved loan count
        List<TopCustomerDTO> topCustomers = customerApprovedCountMap.entrySet().stream()
            .map(entry -> {
                Customer customer = entry.getKey();
                return new TopCustomerDTO(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customerTotalAmountMap.getOrDefault(customer, BigDecimal.ZERO),
                    customerTotalCountMap.getOrDefault(customer, 0),
                    entry.getValue()
                );
            })
            .sorted(Comparator.comparing(TopCustomerDTO::getApprovedLoanCount).reversed())
            .limit(limit)
            .collect(Collectors.toList());
        
        return topCustomers;
    }
    
    /**
     * Get top customers by total loan amount
     * @param limit Number of top customers to return
     * @return List of top customers
     */
    public List<TopCustomerDTO> getTopCustomersByLoanAmount(int limit) {
        List<Loan> allLoans = loanRepository.findAll();
        
        // Group loans by customer and calculate total loan amount
        Map<Customer, BigDecimal> customerTotalMap = new HashMap<>();
        Map<Customer, Integer> customerLoanCountMap = new HashMap<>();
        Map<Customer, Integer> customerApprovedCountMap = new HashMap<>();
        
        for (Loan loan : allLoans) {
            Customer customer = loan.getCustomer();
            
            // Update total loan amount
            BigDecimal currentAmount = customerTotalMap.getOrDefault(customer, BigDecimal.ZERO);
            customerTotalMap.put(customer, currentAmount.add(loan.getLoanAmount()));
            
            // Update total loan count
            int currentCount = customerLoanCountMap.getOrDefault(customer, 0);
            customerLoanCountMap.put(customer, currentCount + 1);
            
            // Update approved loan count
            if (loan.getApplicationStatus() == ApplicationStatus.APPROVED_BY_SYSTEM || 
                loan.getApplicationStatus() == ApplicationStatus.APPROVED_BY_AGENT) {
                int approvedCount = customerApprovedCountMap.getOrDefault(customer, 0);
                customerApprovedCountMap.put(customer, approvedCount + 1);
            }
        }
        
        // Convert to DTOs and sort by total loan amount
        List<TopCustomerDTO> topCustomers = customerTotalMap.entrySet().stream()
            .map(entry -> {
                Customer customer = entry.getKey();
                return new TopCustomerDTO(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    entry.getValue(),
                    customerLoanCountMap.getOrDefault(customer, 0),
                    customerApprovedCountMap.getOrDefault(customer, 0)
                );
            })
            .sorted(Comparator.comparing(TopCustomerDTO::getTotalLoanAmount).reversed())
            .limit(limit)
            .collect(Collectors.toList());
        
        return topCustomers;
    }
}