package com.loanapp.los.service;

import com.loanapp.los.dto.LoanApplicationRequest;
import com.loanapp.los.dto.LoanApplicationResponse;
import com.loanapp.los.model.Customer;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.CustomerRepository;
import com.loanapp.los.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoanService {
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Transactional
    public LoanApplicationResponse submitLoanApplication(LoanApplicationRequest request) {
        // Find or create customer
        Customer customer = findOrCreateCustomer(request);
        
        // Create new loan
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(request.getLoanAmount());
        loan.setLoanType(request.getLoanType());
        loan.setApplicationStatus(ApplicationStatus.APPLIED);
        loan.setPurpose(request.getPurpose());
        loan.setTermMonths(request.getTermMonths());
        
        // Save the loan
        Loan savedLoan = loanRepository.save(loan);
        
        // Build response
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setLoanId(savedLoan.getId());
        response.setCustomerName(customer.getName());
        response.setCustomerPhone(customer.getPhone());
        response.setLoanAmount(savedLoan.getLoanAmount());
        response.setLoanType(savedLoan.getLoanType());
        response.setStatus(savedLoan.getApplicationStatus());
        response.setSubmittedAt(LocalDateTime.now());
        response.setMessage("Loan application submitted successfully.");
        
        return response;
    }
    
    private Customer findOrCreateCustomer(LoanApplicationRequest request) {
        // Try to find existing customer by phone
        Optional<Customer> existingCustomer = customerRepository.findByPhone(request.getCustomerPhone());
        
        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }
        
        // Create new customer
        Customer customer = new Customer();
        customer.setName(request.getCustomerName());
        customer.setPhone(request.getCustomerPhone());
        customer.setEmail(request.getCustomerEmail());
        customer.setIdentityNumber(request.getCustomerIdentityNumber());
        
        return customerRepository.save(customer);
    }
}
