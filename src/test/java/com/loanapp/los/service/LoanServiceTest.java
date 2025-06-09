package com.loanapp.los.service;

import com.loanapp.los.dto.LoanApplicationRequest;
import com.loanapp.los.dto.LoanApplicationResponse;
import com.loanapp.los.model.Customer;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.model.enums.LoanType;
import com.loanapp.los.repository.CustomerRepository;
import com.loanapp.los.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private LoanService loanService;
    
    private LoanApplicationRequest testRequest;
    private Customer testCustomer;
    private Loan testLoan;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testRequest = new LoanApplicationRequest();
        testRequest.setCustomerName("Test Customer");
        testRequest.setCustomerPhone("1234567890");
        testRequest.setCustomerEmail("test@example.com");
        testRequest.setCustomerIdentityNumber("TEST123");
        testRequest.setLoanAmount(new BigDecimal("10000"));
        testRequest.setLoanType(LoanType.PERSONAL);
        testRequest.setPurpose("Vacation");
        testRequest.setTermMonths(36);
        
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhone("1234567890");
        testCustomer.setIdentityNumber("TEST123");
        
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setCustomer(testCustomer);
        testLoan.setLoanAmount(new BigDecimal("10000"));
        testLoan.setLoanType(LoanType.PERSONAL);
        testLoan.setApplicationStatus(ApplicationStatus.APPLIED);
        testLoan.setPurpose("Vacation");
        testLoan.setTermMonths(36);
    }
    
    @Test
    void shouldSubmitNewLoanApplicationForNewCustomer() {
        // Arrange
        when(customerRepository.findByPhone(testRequest.getCustomerPhone())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        
        // Act
        LoanApplicationResponse response = loanService.submitLoanApplication(testRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(testLoan.getId(), response.getLoanId());
        assertEquals(testCustomer.getName(), response.getCustomerName());
        assertEquals(testLoan.getLoanAmount(), response.getLoanAmount());
        assertEquals(testLoan.getLoanType(), response.getLoanType());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        
        verify(customerRepository).findByPhone(testRequest.getCustomerPhone());
        verify(customerRepository).save(any(Customer.class));
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    void shouldSubmitNewLoanApplicationForExistingCustomer() {
        // Arrange
        when(customerRepository.findByPhone(testRequest.getCustomerPhone())).thenReturn(Optional.of(testCustomer));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        
        // Act
        LoanApplicationResponse response = loanService.submitLoanApplication(testRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(testLoan.getId(), response.getLoanId());
        assertEquals(testCustomer.getName(), response.getCustomerName());
        
        verify(customerRepository).findByPhone(testRequest.getCustomerPhone());
        verify(customerRepository, never()).save(any(Customer.class)); // Should not save existing customer
        verify(loanRepository).save(any(Loan.class));
    }
}