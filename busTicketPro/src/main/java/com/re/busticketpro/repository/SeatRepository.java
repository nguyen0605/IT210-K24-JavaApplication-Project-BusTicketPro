package com.re.busticketpro.repository;

import com.re.busticketpro.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTripIdOrderBySeatFloorAscSeatRowAscSeatColAsc(Long tripId);

    Optional<Seat> findByIdAndTripId(Long id, Long tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"trip", "trip.route", "trip.route.departure", "trip.route.arrival", "trip.bus"})
    @Query("select s from Seat s where s.id = :seatId and s.trip.id = :tripId")
    Optional<Seat> findSeatForUpdate(@Param("seatId") Long seatId, @Param("tripId") Long tripId);
}
