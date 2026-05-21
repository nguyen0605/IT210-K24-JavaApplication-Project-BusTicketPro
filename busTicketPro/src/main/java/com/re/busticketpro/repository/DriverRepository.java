package com.re.busticketpro.repository;

import com.re.busticketpro.entity.Driver;
import com.re.busticketpro.enums.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByFullNameAndStatus(String fullName, DriverStatus status);

    List<Driver> findByStatusOrderByFullNameAsc(DriverStatus status);

    @Query("""
            select d from Driver d
            where (:fullName is null or lower(d.fullName) like lower(concat('%', :fullName, '%')))
              and (:phone is null or d.phone like concat('%', :phone, '%'))
              and (:licenseClass is null or lower(d.licenseClass) like lower(concat('%', :licenseClass, '%')))
              and (:status is null or d.status = :status)
            """)
    Page<Driver> search(@Param("fullName") String fullName,
                        @Param("phone") String phone,
                        @Param("licenseClass") String licenseClass,
                        @Param("status") DriverStatus status,
                        Pageable pageable);
}
