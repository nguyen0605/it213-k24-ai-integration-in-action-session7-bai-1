package com.example.crm.dto;

import lombok.Data;

@Data
public class TicketRequest {
    private String customerId;
    private String issueDescription;
    private String resolutionSteps;
}