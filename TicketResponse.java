package com.example.crm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String customerId;
    private String issueDescription;
    private String resolutionSteps;
    private String status;
    private double similarityScore;
}