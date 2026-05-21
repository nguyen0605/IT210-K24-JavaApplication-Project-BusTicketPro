package com.re.busticketpro.service;

import com.re.busticketpro.entity.Ticket;
import com.re.busticketpro.enums.SeatStatus;
import com.re.busticketpro.enums.TicketStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StaffTicketService {
    private final TicketRepository ticketRepository;

    public StaffTicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> pendingTickets() {
        return ticketRepository.findByStatusOrderByBookingTimeAsc(TicketStatus.PENDING);
    }

    public Ticket get(Long id) {
        return ticketRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("Kh\u00f4ng t\u00ecm th\u1ea5y v\u00e9"));
    }

    @Transactional
    public void confirmPayment(Long ticketId) {
        Ticket ticket = get(ticketId);
        if (ticket.getStatus() != TicketStatus.PENDING || ticket.getSeat().getStatus() != SeatStatus.PENDING) {
            throw new BusinessException("V\u00e9 kh\u00f4ng c\u00f2n \u1edf tr\u1ea1ng th\u00e1i ch\u1edd thanh to\u00e1n");
        }
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPaymentTime(LocalDateTime.now());
        ticket.getSeat().setStatus(SeatStatus.BOOKED);
        ticket.getSeat().setLockedUntil(null);
    }

    @Transactional
    public void cancelTicket(Long ticketId, String reason) {
        Ticket ticket = get(ticketId);
        cancel(ticket, reason == null || reason.isBlank() ? "STAFF_CANCELLED" : reason);
    }

    @Transactional
    public void cancelExpiredPendingTickets() {
        var expired = ticketRepository.findByStatusAndExpiredTimeBefore(TicketStatus.PENDING, LocalDateTime.now());
        for (Ticket ticket : expired) {
            cancel(ticket, "AUTO_CANCEL_EXPIRED_PAYMENT");
        }
    }

    private void cancel(Ticket ticket, String reason) {
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BusinessException("V\u00e9 \u0111\u00e3 b\u1ecb h\u1ee7y tr\u01b0\u1edbc \u0111\u00f3");
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancelledTime(LocalDateTime.now());
        ticket.setCancelReason(reason);
        ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
        ticket.getSeat().setLockedUntil(null);
    }
}
