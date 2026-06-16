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
    private long totalTickets;
    private long ticketsOuverts;
    private long ticketsEnCours;
    private long ticketsResolus;
    private long ticketsClotures;
    private long slaDepassesCount;
}