package com.re.busticketpro.service;

import com.re.busticketpro.dto.BookingRequest;
import com.re.busticketpro.entity.Seat;
import com.re.busticketpro.entity.Ticket;
import com.re.busticketpro.enums.SeatStatus;
import com.re.busticketpro.enums.TicketStatus;
import com.re.busticketpro.enums.TripStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.SeatRepository;
import com.re.busticketpro.repository.TicketRepository;
import com.re.busticketpro.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class BookingService {
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public BookingService(SeatRepository seatRepository, TicketRepository ticketRepository, UserRepository userRepository,
                          EmailService emailService) {
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Ticket bookTicket(BookingRequest request, Long currentUserId) {
        // Row-level lock keeps the seat state and ticket insert in the same atomic transaction.
        Seat seat = seatRepository.findSeatForUpdate(request.getSeatId(), request.getTripId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy ghế"));
        if (seat.getTrip().getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("Chuyến xe không còn nhận đặt vé");
        }
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new BusinessException("Ghế vừa được người khác đặt. Vui lòng chọn ghế khác");
        }
        LocalDateTime now = LocalDateTime.now();
        seat.setStatus(SeatStatus.PENDING);
        seat.setLockedUntil(now.plusMinutes(15));

        Ticket ticket = new Ticket();
        ticket.setTicketCode(generateTicketCode());
        ticket.setTrip(seat.getTrip());
        ticket.setSeat(seat);
        if (currentUserId != null) {
            ticket.setUser(userRepository.findById(currentUserId).orElse(null));
        }
        ticket.setPassengerName(request.getPassengerName().trim());
        ticket.setPassengerPhone(request.getPassengerPhone().trim());
        ticket.setPassengerEmail(request.getPassengerEmail().trim());
        ticket.setTotalAmount(seat.getTrip().getPrice());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setBookingTime(now);
        ticket.setExpiredTime(now.plusMinutes(30));
        Ticket savedTicket = ticketRepository.save(ticket);
        sendConfirmationEmailAfterCommit(savedTicket.getTicketCode());
        return savedTicket;
    }

    private void sendConfirmationEmailAfterCommit(String ticketCode) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendBookingConfirmation(ticketCode);
            }
        });
    }

    private String generateTicketCode() {
        return "BT" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
    }
}
