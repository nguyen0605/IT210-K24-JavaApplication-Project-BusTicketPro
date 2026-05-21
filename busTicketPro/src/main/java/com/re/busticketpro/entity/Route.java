package com.re.busticketpro.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routes", uniqueConstraints = @UniqueConstraint(name = "uk_routes_departure_arrival", columnNames = {"departure_location_id", "arrival_location_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departure_location_id")
    private Location departure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_location_id")
    private Location arrival;

    @Column(name = "distance_km")
    private Integer distanceKm;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
}
