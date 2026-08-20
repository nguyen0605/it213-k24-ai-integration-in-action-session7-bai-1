package com.example.crm.service;

import com.example.crm.dto.TicketRequest;
import com.example.crm.dto.TicketResponse;
import com.example.crm.entity.Ticket;
import com.example.crm.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final VectorStore vectorStore;

    @Transactional
    public Ticket ingestTicket(TicketRequest request) {
        Ticket ticket = Ticket.builder()
                .customerId(request.getCustomerId())
                .issueDescription(request.getIssueDescription())
                .resolutionSteps(request.getResolutionSteps())
                .status("RESOLVED")
                .createdAt(LocalDateTime.now())
                .build();

        Ticket saved = ticketRepository.save(ticket);

        // Create Document for vector store representation
        Document document = new Document(
                String.format("Customer: %s. Issue: %s. Resolution: %s", 
                        saved.getCustomerId(), saved.getIssueDescription(), saved.getResolutionSteps()),
                Map.of("ticketId", saved.getId())
        );

        vectorStore.add(List.of(document));
        return saved;
    }

    public List<TicketResponse> findSimilarTickets(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(topK);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        return documents.stream().map(doc -> {
            Long ticketId = ((Number) doc.getMetadata().get("ticketId")).longValue();
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
            
            // Extract mock/calculated similarity score or default safely if API doesn't expose it
            double score = doc.getMetadata().get("distance") != null ? 
                Double.parseDouble(doc.getMetadata().get("distance").toString()) : 1.0;

            return TicketResponse.builder()
                    .id(ticket.getId())
                    .customerId(ticket.getCustomerId())
                    .issueDescription(ticket.getIssueDescription())
                    .resolutionSteps(ticket.getResolutionSteps())
                    .status(ticket.getStatus())
                    .similarityScore(score)
                    .build();
        }).collect(Collectors.toList());
    }
}