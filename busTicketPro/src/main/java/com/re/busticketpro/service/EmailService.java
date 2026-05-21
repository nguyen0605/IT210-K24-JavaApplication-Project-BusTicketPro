package com.re.busticketpro.service;

import com.re.busticketpro.entity.Ticket;
import com.re.busticketpro.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TicketRepository ticketRepository;
    private final String from;

    public EmailService(JavaMailSender mailSender, TicketRepository ticketRepository,
                        @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.ticketRepository = ticketRepository;
        this.from = from;
    }

    @Async
    @Transactional(readOnly = true)
    public void sendBookingConfirmation(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode).orElse(null);
        if (ticket == null) {
            log.warn("Skip booking confirmation email because ticket {} was not found", ticketCode);
            return;
        }
        if (ticket.getPassengerEmail() == null || ticket.getPassengerEmail().isBlank()) {
            log.info("Skip booking confirmation email for ticket {} because passenger email is blank", ticketCode);
            return;
        }
        if (from == null || from.isBlank()) {
            log.warn("Skip booking confirmation email for ticket {} because MAIL_USERNAME/MAIL_FROM is blank", ticketCode);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(ticket.getPassengerEmail().trim());
        message.setSubject("Xác nhận đặt vé - " + ticket.getTicketCode());
        message.setText(buildBookingConfirmationText(ticket));

        try {
            mailSender.send(message);
            log.info("Booking confirmation email sent for ticket {}", ticketCode);
        } catch (MailException ex) {
            log.warn("Could not send booking confirmation email for ticket {}: {}", ticketCode, ex.getMessage());
        }
    }

    private String buildBookingConfirmationText(Ticket ticket) {
        String route = ticket.getTrip().getRoute().getDeparture().getName()
                + " -> "
                + ticket.getTrip().getRoute().getArrival().getName();
        return """
                Bus Ticket Pro xác nhận đặt vé thành công.

                Mã vé: %s
                Hành khách: %s
                Số điện thoại: %s
                Tuyến: %s
                Khởi hành: %s
                Ghế: %s
                Biển số xe: %s
                Loại xe: %s
                Trạng thái: Chờ thanh toán

                Vui lòng thanh toán trong 30 phút để giữ chỗ.
                Bạn có thể tra cứu vé bằng mã vé và số điện thoại đã đặt.
                """.formatted(
                ticket.getTicketCode(),
                ticket.getPassengerName(),
                ticket.getPassengerPhone(),
                route,
                ticket.getTrip().getDepartureTime(),
                ticket.getSeat().getSeatNumber(),
                ticket.getTrip().getBus().getLicensePlate(),
                ticket.getTrip().getBus().getBusType()
        );
    }
}
