package com.re.busticketpro.service;

import com.re.busticketpro.dto.BookingRequest;
import com.re.busticketpro.entity.*;
import com.re.busticketpro.enums.*;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceTest {
    @Autowired BookingService bookingService;
    @Autowired PassengerTicketService passengerTicketService;
    @Autowired LocationRepository locationRepository;
    @Autowired RouteRepository routeRepository;
    @Autowired BusRepository busRepository;
    @Autowired TripRepository tripRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired TicketRepository ticketRepository;

    private Trip trip;
    private Seat seat;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        seatRepository.deleteAll();
        tripRepository.deleteAll();
        routeRepository.deleteAll();
        busRepository.deleteAll();
        locationRepository.deleteAll();

        Location departure = new Location();
        departure.setName("Hà Nội");
        Location arrival = new Location();
        arrival.setName("Hải Phòng");
        locationRepository.save(departure);
        locationRepository.save(arrival);

        com.re.busticketpro.entity.Route route = new com.re.busticketpro.entity.Route();
        route.setDeparture(departure);
        route.setArrival(arrival);
        route.setDistanceKm(120);
        route.setEstimatedDurationMinutes(150);
        routeRepository.save(route);

        Bus bus = new Bus();
        bus.setLicensePlate("29B-TEST");
        bus.setBusType("Limousine");
        bus.setTotalSeats(16);
        bus.setCompanyName("Test Bus");
        bus.setDriverName("Driver");
        bus.setStatus(BusStatus.ACTIVE);
        busRepository.save(bus);

        trip = new Trip();
        trip.setRoute(route);
        trip.setBus(bus);
        trip.setDepartureTime(LocalDateTime.now().plusDays(2));
        trip.setArrivalTime(LocalDateTime.now().plusDays(2).plusHours(2));
        trip.setPrice(BigDecimal.valueOf(120000));
        trip.setStatus(TripStatus.SCHEDULED);
        tripRepository.save(trip);

        seat = new Seat();
        seat.setTrip(trip);
        seat.setSeatNumber("A1");
        seat.setSeatRow(1);
        seat.setSeatCol(1);
        seat.setSeatFloor(SeatFloor.NORMAL);
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }

    @Test
    void bookTicketSuccess() {
        var ticket = bookingService.bookTicket(request(), null);

        assertThat(ticket.getTicketCode()).startsWith("BT");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PENDING);
        assertThat(seatRepository.findById(seat.getId()).orElseThrow().getStatus()).isEqualTo(SeatStatus.PENDING);
    }

    @Test
    void cannotBookPendingOrBookedSeat() {
        bookingService.bookTicket(request(), null);

        assertThatThrownBy(() -> bookingService.bookTicket(request(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ghế");
    }

    @Test
    void passengerCancelReleasesSeat() {
        var ticket = bookingService.bookTicket(request(), null);

        passengerTicketService.cancelByPassenger(ticket.getTicketCode(), "0909000000");

        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    private BookingRequest request() {
        BookingRequest request = new BookingRequest();
        request.setTripId(trip.getId());
        request.setSeatId(seat.getId());
        request.setPassengerName("Nguyen Van A");
        request.setPassengerPhone("0909000000");
        request.setPassengerEmail("a@example.com");
        return request;
    }
}
