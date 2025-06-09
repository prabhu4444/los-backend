package com.loanapp.los.service;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Customer;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.model.enums.LoanType;
import com.loanapp.los.repository.AgentRepository;
import com.loanapp.los.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanProcessingServiceTest {

    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private AgentRepository agentRepository;
    
    @Mock
    private AgentAssignmentService agentAssignmentService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private LoanProcessingService loanProcessingService;
    
    private Loan testLoan;
    private Customer testCustomer;
    private Agent testAgent;
    
    @BeforeEach
    void setUp() {
        // Set up test data
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
        testLoan.setCreatedAt(LocalDateTime.now());
        
        testAgent = new Agent();
        testAgent.setId(1L);
        testAgent.setName("Test Agent");
        testAgent.setEmail("agent@example.com");
        testAgent.setAvailable(true);
    }

    @Test
    void shouldProcessNewLoans() {
        // Arrange
        when(loanRepository.findByApplicationStatus(ApplicationStatus.APPLIED))
                .thenReturn(List.of(testLoan));
        
        // Act
        loanProcessingService.processNewLoans();
        
        // Assert - verify the method was called
        verify(loanRepository).findByApplicationStatus(ApplicationStatus.APPLIED);
    }
    
    @Test
    void shouldProcessLoanAsyncForSmallPersonalLoan() throws Exception {
        // Arrange - small personal loan should be auto-approved
        testLoan.setLoanAmount(new BigDecimal("5000"));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // Act
        loanProcessingService.processLoanAsync(1L);
        
        // Allow for async processing
        Thread.sleep(100);
        
        // Assert
        verify(loanRepository).findById(1L);
    }
    
    @Test
    void shouldProcessLoanAsyncForLargePersonalLoan() throws Exception {
        // Arrange - large personal loan should be auto-rejected
        testLoan.setLoanAmount(new BigDecimal("15000"));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // Act
        loanProcessingService.processLoanAsync(1L);
        
        // Allow for async processing
        Thread.sleep(100);
        
        // Assert
        verify(loanRepository).findById(1L);
    }
    
    @Test
    void shouldProcessLoanAsyncForHomeLoan() throws Exception {
        // Arrange - home loans always need review
        testLoan.setLoanType(LoanType.HOME);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // Act
        loanProcessingService.processLoanAsync(1L);
        
        // Allow for async processing
        Thread.sleep(100);
        
        // Assert
        verify(loanRepository).findById(1L);
    }
    
    @Test
    void shouldAssignLoansToAgents() {
        // Arrange
        List<Loan> pendingLoans = List.of(testLoan);
        testLoan.setApplicationStatus(ApplicationStatus.PENDING_REVIEW);
        
        when(loanRepository.findByApplicationStatus(ApplicationStatus.PENDING_REVIEW))
                .thenReturn(pendingLoans);
        when(agentRepository.findByAvailable(true)).thenReturn(List.of(testAgent));
        
        // Act
        loanProcessingService.assignLoansToAgents();
        
        // Assert
        verify(loanRepository).findByApplicationStatus(ApplicationStatus.PENDING_REVIEW);
        verify(agentRepository).findByAvailable(true);
    }
}