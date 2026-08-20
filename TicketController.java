package com.example.crm.controller;

import com.example.crm.dto.TicketRequest;
import com.example.crm.dto.TicketResponse;
import com.example.crm.entity.Ticket;
import com.example.crm.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/ingest")
    public ResponseEntity<Ticket> ingestTicket(@RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.ingestTicket(request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TicketResponse>> searchTickets(
            @RequestParam String query, 
            @RequestParam(defaultValue = "3") int topK) {
        return ResponseEntity.ok(ticketService.findSimilarTickets(query, topK));
    }
}