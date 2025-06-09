package com.loanapp.los.controller;

import com.loanapp.los.dto.TopCustomerDTO;
import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Loan;
import com.loanapp.los.model.enums.ApplicationStatus;
import com.loanapp.los.repository.AgentRepository;
import com.loanapp.los.repository.LoanRepository;
import com.loanapp.los.service.CustomerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class WebController {
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private CustomerStatisticsService customerStatisticsService;
    
    @GetMapping("/apply")
    public String loanApplicationForm() {
        return "loan-application";
    }

    @GetMapping("/admin-redirect")
    public String adminRedirect() {
        return "redirect:/admin";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) ApplicationStatus status,
            Model model) {
        
        // Get loans filtered by status
        List<Loan> loans;
        if (status != null) {
            loans = loanRepository.findByApplicationStatusOrderByCreatedAtDesc(status);
        } else {
            loans = loanRepository.findAll();
        }
        
        // Add loans to model
        model.addAttribute("loans", loans);
        
        // For maintaining filter state in form
        model.addAttribute("status", status);
        
        // Count loans by status for the status summary section
        Map<ApplicationStatus, Long> statusCounts = Arrays.stream(ApplicationStatus.values())
                .collect(Collectors.toMap(
                        status1 -> status1,
                        status1 -> loanRepository.countByApplicationStatus(status1)
                ));
        model.addAttribute("statusCounts", statusCounts);
        
        // Calculate total count and add to model
        long totalLoanCount = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        model.addAttribute("totalLoanCount", totalLoanCount);
        
        // Get top 5 customers by loan amount
        List<TopCustomerDTO> topCustomersByAmount = customerStatisticsService.getTopCustomersByLoanAmount(5);
        model.addAttribute("topCustomers", topCustomersByAmount);
        
        // Get top 3 customers by approved loans
        List<TopCustomerDTO> topCustomersByApproved = customerStatisticsService.getTopCustomersByApprovedLoans(3);
        model.addAttribute("topCustomersByApproved", topCustomersByApproved);
        
        return "dashboard";
    }
    
    @GetMapping("/agent-dashboard")
    public String agentDashboard(Model model) {
        // Get all loans under review
        List<Loan> loansUnderReview = loanRepository.findByApplicationStatus(ApplicationStatus.UNDER_REVIEW);
        model.addAttribute("loansUnderReview", loansUnderReview);
        
        // Get recent agent decisions (last 10)
        List<Loan> recentApproved = loanRepository.findTop10ByApplicationStatusOrderByDecisionAtDesc(
                ApplicationStatus.APPROVED_BY_AGENT);
        List<Loan> recentRejected = loanRepository.findTop10ByApplicationStatusOrderByDecisionAtDesc(
                ApplicationStatus.REJECTED_BY_AGENT);
        
        // Combine and sort by decision time (most recent first)
        List<Loan> recentDecisions = new ArrayList<>();
        recentDecisions.addAll(recentApproved);
        recentDecisions.addAll(recentRejected);
        recentDecisions.sort((a, b) -> b.getDecisionAt().compareTo(a.getDecisionAt()));
        
        // Limit to 10 most recent
        if (recentDecisions.size() > 10) {
            recentDecisions = recentDecisions.subList(0, 10);
        }
        
        model.addAttribute("recentDecisions", recentDecisions);
        
        return "agent-dashboard";
    }
}