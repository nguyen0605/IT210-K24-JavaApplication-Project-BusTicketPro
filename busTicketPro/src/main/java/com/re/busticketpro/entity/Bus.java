package com.re.busticketpro.entity;

import com.re.busticketpro.enums.BusStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 30)
    private String licensePlate;

    @Column(name = "bus_type", nullable = false, length = 60)
    private String busType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "brand", length = 100)
    private String companyName;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BusStatus status = BusStatus.ACTIVE;
}
