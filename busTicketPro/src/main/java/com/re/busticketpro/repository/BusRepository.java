package com.re.busticketpro.repository;

import com.re.busticketpro.entity.Bus;
import com.re.busticketpro.enums.BusStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusRepository extends JpaRepository<Bus, Long> {
    @Override
    @EntityGraph(attributePaths = "driver")
    java.util.Optional<Bus> findById(Long id);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    boolean existsByDriverId(Long driverId);

    boolean existsByDriverIdAndIdNot(Long driverId, Long id);

    java.util.List<Bus> findByDriverId(Long driverId);

    @Query("""
            select b from Bus b
            where (:licensePlate is null or lower(b.licensePlate) like lower(concat('%', :licensePlate, '%')))
              and (:busType is null or lower(b.busType) like lower(concat('%', :busType, '%')))
              and (:status is null or b.status = :status)
            """)
    Page<Bus> search(@Param("licensePlate") String licensePlate,
                     @Param("busType") String busType,
                     @Param("status") BusStatus status,
                     Pageable pageable);
}
