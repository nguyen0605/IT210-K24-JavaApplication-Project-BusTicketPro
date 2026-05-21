package com.re.busticketpro.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStats(long totalTickets, long pendingTickets, long paidTickets, long cancelledTickets,
                             BigDecimal revenue, List<RouteRevenueRow> routeRows) {
}
