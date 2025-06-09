package com.loanapp.los.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String recipient;
    private String message;
    private String channel; // SMS, PUSH, EMAIL
    private String type;    // CUSTOMER, AGENT, MANAGER
    private Long loanId;
    private LocalDateTime timestamp;
}