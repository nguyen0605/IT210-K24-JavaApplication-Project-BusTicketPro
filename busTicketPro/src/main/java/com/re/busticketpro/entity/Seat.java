package com.re.busticketpro.entity;

import com.re.busticketpro.enums.SeatFloor;
import com.re.busticketpro.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(name = "uk_seats_trip_number", columnNames = {"trip_id", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Column(name = "seat_row", nullable = false)
    private Integer seatRow;

    @Column(name = "seat_col", nullable = false)
    private Integer seatCol;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_floor", nullable = false, length = 20)
    private SeatFloor seatFloor = SeatFloor.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Version
    private Integer version;
}
