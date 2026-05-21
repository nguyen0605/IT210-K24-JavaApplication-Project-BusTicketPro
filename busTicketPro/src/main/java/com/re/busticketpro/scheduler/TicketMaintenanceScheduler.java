package com.re.busticketpro.scheduler;

import com.re.busticketpro.service.TicketExpiryService;
import com.re.busticketpro.service.TripStatusSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TicketMaintenanceScheduler {
    private static final Logger log = LoggerFactory.getLogger(TicketMaintenanceScheduler.class);

    private final TicketExpiryService ticketExpiryService;
    private final TripStatusSchedulerService tripStatusSchedulerService;

    public TicketMaintenanceScheduler(TicketExpiryService ticketExpiryService,
                                      TripStatusSchedulerService tripStatusSchedulerService) {
        this.ticketExpiryService = ticketExpiryService;
        this.tripStatusSchedulerService = tripStatusSchedulerService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.ticket-maintenance-delay-ms:600000}", initialDelayString = "${app.scheduler.ticket-maintenance-initial-delay-ms:30000}")
    public void maintainTicketsAndTrips() {
        LocalDateTime now = LocalDateTime.now();
        int expiredTickets = ticketExpiryService.cancelExpiredPendingTickets(now);
        int departedTickets = ticketExpiryService.cancelPendingTicketsForDepartedTrips(now);
        int departedTrips = tripStatusSchedulerService.markDepartedTrips(now);
        int completedTrips = tripStatusSchedulerService.markCompletedTrips(now);

        if (expiredTickets > 0 || departedTickets > 0 || departedTrips > 0 || completedTrips > 0) {
            log.info("Ticket maintenance completed: expiredTickets={}, departedPendingTickets={}, departedTrips={}, completedTrips={}",
                    expiredTickets, departedTickets, departedTrips, completedTrips);
        }
    }
}
