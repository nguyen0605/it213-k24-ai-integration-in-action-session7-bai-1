package com.example.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String issueDescription;

    @Column(columnDefinition = "TEXT")
    private String resolutionSteps;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;
}