package com.re.busticketpro.service;

import com.re.busticketpro.enums.SeatStatus;
import com.re.busticketpro.enums.TicketStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PassengerTicketService {
    private final TicketRepository ticketRepository;

    public PassengerTicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void cancelByPassenger(String ticketCode, String phone) {
        var ticket = ticketRepository.findByTicketCodeAndPassengerPhone(ticketCode.trim(), phone.trim())
                .orElseThrow(() -> new BusinessException("Không tìm thấy vé"));
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BusinessException("Vé đã bị hủy");
        }
        if (Duration.between(LocalDateTime.now(), ticket.getTrip().getDepartureTime()).toHours() < 12) {
            throw new BusinessException("Chỉ có thể hủy vé trước giờ khởi hành tối thiểu 12 tiếng");
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancelledTime(LocalDateTime.now());
        ticket.setCancelReason("PASSENGER_CANCELLED");
        ticket.getSeat().setStatus(SeatStatus.AVAILABLE);
        ticket.getSeat().setLockedUntil(null);
    }
}
