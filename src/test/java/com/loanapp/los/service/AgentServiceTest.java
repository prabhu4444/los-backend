package com.loanapp.los.service;

import com.loanapp.los.dto.AgentDecisionRequest;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {

    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private AgentRepository agentRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private AgentService agentService;
    
    private Loan testLoan;
    private Customer testCustomer;
    private Agent testAgent;
    private AgentDecisionRequest approveRequest;
    private AgentDecisionRequest rejectRequest;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhone("1234567890");
        
        testAgent = new Agent();
        testAgent.setId(1L);
        testAgent.setName("Test Agent");
        testAgent.setEmail("agent@example.com");
        
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setCustomer(testCustomer);
        testLoan.setLoanAmount(new BigDecimal("10000"));
        testLoan.setLoanType(LoanType.PERSONAL);
        testLoan.setApplicationStatus(ApplicationStatus.UNDER_REVIEW);
        testLoan.setAssignedAgent(testAgent);
        testLoan.setAssignedAt(LocalDateTime.now());
        
        approveRequest = new AgentDecisionRequest();
        approveRequest.setDecision("APPROVE");
        
        rejectRequest = new AgentDecisionRequest();
        rejectRequest.setDecision("REJECT");
    }
    
    @Test
    void shouldGetLoansAssignedToAgent() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(loanRepository.findByAssignedAgentAndApplicationStatus(testAgent, ApplicationStatus.UNDER_REVIEW))
                .thenReturn(List.of(testLoan));
        
        // Act
        List<Loan> result = agentService.getLoansAssignedToAgent(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLoan.getId(), result.get(0).getId());
        
        verify(agentRepository).findById(1L);
        verify(loanRepository).findByAssignedAgentAndApplicationStatus(testAgent, ApplicationStatus.UNDER_REVIEW);
    }
    
    @Test
    void shouldApproveLoan() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan savedLoan = invocation.getArgument(0);
            assertEquals(ApplicationStatus.APPROVED_BY_AGENT, savedLoan.getApplicationStatus());
            assertNotNull(savedLoan.getDecisionAt());
            return savedLoan;
        });
        
        // Act
        Loan result = agentService.processAgentDecision(1L, 1L, approveRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.APPROVED_BY_AGENT, result.getApplicationStatus());
        assertNotNull(result.getDecisionAt());
        
        verify(agentRepository).findById(1L);
        verify(loanRepository).findById(1L);
        verify(loanRepository).save(any(Loan.class));
        verify(notificationService).sendCustomerLoanApprovalNotification(any(Loan.class));
    }
    
    @Test
    void shouldRejectLoan() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan savedLoan = invocation.getArgument(0);
            assertEquals(ApplicationStatus.REJECTED_BY_AGENT, savedLoan.getApplicationStatus());
            assertNotNull(savedLoan.getDecisionAt());
            return savedLoan;
        });
        
        // Act
        Loan result = agentService.processAgentDecision(1L, 1L, rejectRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.REJECTED_BY_AGENT, result.getApplicationStatus());
        assertNotNull(result.getDecisionAt());
        
        verify(agentRepository).findById(1L);
        verify(loanRepository).findById(1L);
        verify(loanRepository).save(any(Loan.class));
    }
    
    @Test
    void shouldThrowExceptionForNonExistentAgent() {
        // Arrange
        when(agentRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            agentService.processAgentDecision(99L, 1L, approveRequest);
        });
        
        assertTrue(exception.getMessage().contains("Agent not found"));
    }
    
    @Test
    void shouldThrowExceptionForNonExistentLoan() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            agentService.processAgentDecision(1L, 99L, approveRequest);
        });
        
        assertTrue(exception.getMessage().contains("Loan not found"));
    }
    
    @Test
    void shouldThrowExceptionForLoanNotAssignedToAgent() {
        // Arrange
        Agent otherAgent = new Agent();
        otherAgent.setId(2L);
        
        testLoan.setAssignedAgent(otherAgent);
        
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            agentService.processAgentDecision(1L, 1L, approveRequest);
        });
        
        assertTrue(exception.getMessage().contains("not assigned to agent"));
    }
}