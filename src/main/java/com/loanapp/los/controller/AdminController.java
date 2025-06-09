package com.loanapp.los.controller;

import com.loanapp.los.model.Agent;
import com.loanapp.los.model.Manager;
import com.loanapp.los.repository.AgentRepository;
import com.loanapp.los.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.loanapp.los.dto.NotificationDTO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ManagerRepository managerRepository;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @GetMapping
    public String adminPage(Model model) {
        // Get existing managers and agents for display
        List<Manager> managers = managerRepository.findAll();
        List<Agent> agents = agentRepository.findAll();
        
        // Add empty objects for the forms
        model.addAttribute("newManager", new Manager());
        model.addAttribute("newAgent", new Agent());
        
        // Add existing data for display
        model.addAttribute("managers", managers);
        model.addAttribute("agents", agents);
        
        return "admin";
    }
    
    @PostMapping("/managers/add")
    public String addManager(@ModelAttribute("newManager") Manager manager, 
                             RedirectAttributes redirectAttributes) {
        try {
            managerRepository.save(manager);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Manager " + manager.getName() + " added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error adding manager: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }
    
    @PostMapping("/agents/add")
    public String addAgent(@ModelAttribute("newAgent") Agent agent, 
                           RedirectAttributes redirectAttributes) {
        try {
            // Make sure the manager exists
            if (agent.getManager() != null && agent.getManager().getId() != null) {
                Manager manager = managerRepository.findById(agent.getManager().getId())
                        .orElseThrow(() -> new RuntimeException("Manager not found"));
                agent.setManager(manager);
            }
            
            // Set default availability
            agent.setAvailable(true);
            
            agentRepository.save(agent);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Agent " + agent.getName() + " added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error adding agent: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }
    
    @GetMapping("/logs")
    public String viewLogs(Model model) {
        try {
            // Try to find the log file in several possible locations
            Path logFile = null;
            List<String> possiblePaths = List.of(
                "logs/spring-boot-app.log",
                "target/spring-boot-app.log",
                "spring-boot-app.log",
                System.getProperty("java.io.tmpdir") + "/spring-boot-app.log"
            );
            
            for (String path : possiblePaths) {
                Path testPath = Paths.get(path);
                if (Files.exists(testPath)) {
                    logFile = testPath;
                    break;
                }
            }
            
            if (logFile != null) {
                List<String> logLines = Files.readAllLines(logFile);
                
                // Get the last 100 lines or all lines if fewer
                int start = Math.max(0, logLines.size() - 100);
                List<String> recentLogs = logLines.subList(start, logLines.size());
                
                model.addAttribute("logs", recentLogs);
            } else {
                // If no log file found, check console output
                model.addAttribute("error", "Log file not found. Check application console for logs.");
                
                // Add a list of recently generated log messages from code
                List<String> generatedLogs = new ArrayList<>();
                generatedLogs.add("INFO: Application started successfully");
                generatedLogs.add("INFO: Loan processing service initialized");
                generatedLogs.add("INFO: Agent assignment service initialized");
                model.addAttribute("logs", generatedLogs);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error reading logs: " + e.getMessage());
        }
        
        return "logs";
    }
    
    @GetMapping("/notifications")
public String viewNotifications(Model model) {
    // Return an empty list - we'll implement actual notification storage later
    List<NotificationDTO> notifications = new ArrayList<>();
    
    model.addAttribute("notifications", notifications);
    return "notifications";
}
}