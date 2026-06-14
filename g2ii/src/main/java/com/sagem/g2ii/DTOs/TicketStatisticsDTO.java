package com.sagem.g2ii.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatisticsDTO {
    private Long totalTickets;
    private Long ticketsOuverts;
    private Long ticketsEnCours;
    private Long ticketsResolus;
    private Long ticketsClotures;
    private Long slaDepassesCount;
}