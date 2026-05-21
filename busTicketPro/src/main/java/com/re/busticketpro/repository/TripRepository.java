package com.re.busticketpro.repository;

import com.re.busticketpro.entity.Trip;
import com.re.busticketpro.enums.TripStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @EntityGraph(attributePaths = {"route", "route.departure", "route.arrival", "bus"})
    Optional<Trip> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"route", "route.departure", "route.arrival", "bus"})
    @Query("""
            select t from Trip t
            where t.route.departure.id = :departureId
              and t.route.arrival.id = :arrivalId
              and t.departureTime >= :start
              and t.departureTime < :end
              and t.status = :status
            order by t.departureTime
            """)
    List<Trip> searchTrips(@Param("departureId") Long departureId,
                           @Param("arrivalId") Long arrivalId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end,
                           @Param("status") TripStatus status);

    @EntityGraph(attributePaths = {"route", "route.departure", "route.arrival", "bus"})
    @Query("""
            select t from Trip t
            where t.route.departure.id = :departureId
              and t.route.arrival.id = :arrivalId
              and t.departureTime >= :now
              and t.status = :status
            order by t.departureTime
            """)
    List<Trip> searchUpcomingTrips(@Param("departureId") Long departureId,
                                   @Param("arrivalId") Long arrivalId,
                                   @Param("now") LocalDateTime now,
                                   @Param("status") TripStatus status);

    List<Trip> findByStatusAndDepartureTimeBefore(TripStatus status, LocalDateTime departureTime);

    List<Trip> findByStatusAndArrivalTimeBefore(TripStatus status, LocalDateTime arrivalTime);
}
