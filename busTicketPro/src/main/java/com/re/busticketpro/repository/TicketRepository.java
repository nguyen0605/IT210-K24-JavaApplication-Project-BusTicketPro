package com.re.busticketpro.repository;

import com.re.busticketpro.entity.Ticket;
import com.re.busticketpro.enums.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat", "user"})
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat", "user"})
    Optional<Ticket> findByTicketCode(String ticketCode);

    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat", "user"})
    Optional<Ticket> findByTicketCodeAndPassengerPhone(String ticketCode, String passengerPhone);

    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat", "user"})
    List<Ticket> findByUserIdOrderByBookingTimeDesc(Long userId);

    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat", "user"})
    @Query("""
            select distinct t
            from Ticket t
            where t.user.id = :userId
               or (:phone is not null and t.passengerPhone = :phone)
            order by t.bookingTime desc
            """)
    List<Ticket> findMyTickets(@Param("userId") Long userId, @Param("phone") String phone);

    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat", "user"})
    @Query("""
            select distinct t
            from Ticket t
            where (t.user.id = :userId
                   or (:phone is not null and t.passengerPhone = :phone))
              and (:departureId is null or t.trip.route.departure.id = :departureId)
              and (:arrivalId is null or t.trip.route.arrival.id = :arrivalId)
            order by t.bookingTime desc
            """)
    List<Ticket> findMyTickets(@Param("userId") Long userId,
                               @Param("phone") String phone,
                               @Param("departureId") Long departureId,
                               @Param("arrivalId") Long arrivalId);

    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus", "seat"})
    List<Ticket> findByStatusOrderByBookingTimeAsc(TicketStatus status);

    @EntityGraph(attributePaths = {"trip", "seat"})
    List<Ticket> findByStatusAndExpiredTimeBefore(TicketStatus status, LocalDateTime expiredTime);

    @EntityGraph(attributePaths = {"trip", "seat"})
    List<Ticket> findByStatusAndTripDepartureTimeBefore(TicketStatus status, LocalDateTime departureTime);

    long countByStatus(TicketStatus status);

    @Query("select coalesce(sum(t.totalAmount), 0) from Ticket t where t.status = :status")
    BigDecimal sumRevenueByStatus(@Param("status") TicketStatus status);

    @Query("""
            select concat(t.trip.route.departure.name, ' -> ', t.trip.route.arrival.name), count(t), coalesce(sum(t.totalAmount), 0)
            from Ticket t
            where t.status = com.re.busticketpro.enums.TicketStatus.PAID
            group by t.trip.route.departure.name, t.trip.route.arrival.name
            order by count(t) desc
            """)
    List<Object[]> routeRevenueStats();
}
