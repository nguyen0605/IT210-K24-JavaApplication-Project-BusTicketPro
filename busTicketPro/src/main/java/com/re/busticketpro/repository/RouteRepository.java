package com.re.busticketpro.repository;

import com.re.busticketpro.entity.Route;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    @EntityGraph(attributePaths = {"departure", "arrival"})
    List<Route> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"departure", "arrival"})
    @Query("""
            select r
            from Route r
            where (:departureId is null or r.departure.id = :departureId)
              and (:arrivalId is null or r.arrival.id = :arrivalId)
            order by r.id asc
            """)
    List<Route> search(@Param("departureId") Long departureId, @Param("arrivalId") Long arrivalId);
}
