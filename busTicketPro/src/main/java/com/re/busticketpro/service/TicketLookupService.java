package com.re.busticketpro.service;

import com.re.busticketpro.entity.Ticket;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
public class TicketLookupService {
    private final TicketRepository ticketRepository;

    public TicketLookupService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket lookup(String ticketCode, String phone) {
        return ticketRepository.findByTicketCodeAndPassengerPhone(ticketCode.trim(), phone.trim())
                .orElseThrow(() -> new BusinessException("Không tìm thấy vé với mã và số điện thoại đã nhập"));
    }
}
