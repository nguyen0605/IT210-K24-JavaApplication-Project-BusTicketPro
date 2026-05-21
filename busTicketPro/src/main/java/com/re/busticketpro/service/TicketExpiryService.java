package com.re.busticketpro.service;

import com.re.busticketpro.entity.Ticket;
import com.re.busticketpro.enums.SeatStatus;
import com.re.busticketpro.enums.TicketStatus;
import com.re.busticketpro.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketExpiryService {
    private final TicketRepository ticketRepository;

    public TicketExpiryService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public int cancelExpiredPendingTickets(LocalDateTime now) {
        List<Ticket> expiredTickets = ticketRepository.findByStatusAndExpiredTimeBefore(TicketStatus.PENDING, now);
        expiredTickets.forEach(ticket -> cancelPendingTicket(ticket, now, "Quá hạn thanh toán"));
        return expiredTickets.size();
    }

    @Transactional
    public int cancelPendingTicketsForDepartedTrips(LocalDateTime now) {
        List<Ticket> pendingTickets = ticketRepository.findByStatusAndTripDepartureTimeBefore(TicketStatus.PENDING, now);
        pendingTickets.forEach(ticket -> cancelPendingTicket(ticket, now, "Chuyến xe đã khởi hành"));
        return pendingTickets.size();
    }

    private void cancelPendingTicket(Ticket ticket, LocalDateTime now, String reason) {
        if (ticket.getStatus() != TicketStatus.PENDING) {
            return;
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancelledTime(now);
        ticket.setCancelReason(reason);
        ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
        ticket.getSeat().setLockedUntil(null);
    }
}
