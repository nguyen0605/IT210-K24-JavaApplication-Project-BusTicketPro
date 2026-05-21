package com.re.busticketpro.dto;

import java.math.BigDecimal;

public record RouteRevenueRow(String routeName, long paidTickets, BigDecimal revenue) {
}
