package com.re.busticketpro.service;

import com.re.busticketpro.dto.DashboardStats;
import com.re.busticketpro.dto.RouteRevenueRow;
import com.re.busticketpro.enums.TicketStatus;
import com.re.busticketpro.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminDashboardService {
    private final TicketRepository ticketRepository;

    public AdminDashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public DashboardStats stats() {
        List<RouteRevenueRow> rows = ticketRepository.routeRevenueStats().stream()
                .map(row -> new RouteRevenueRow((String) row[0], ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .toList();
        long pending = ticketRepository.countByStatus(TicketStatus.PENDING);
        long paid = ticketRepository.countByStatus(TicketStatus.PAID);
        long cancelled = ticketRepository.countByStatus(TicketStatus.CANCELLED);
        return new DashboardStats(pending + paid + cancelled, pending, paid, cancelled,
                ticketRepository.sumRevenueByStatus(TicketStatus.PAID), rows);
    }
}
