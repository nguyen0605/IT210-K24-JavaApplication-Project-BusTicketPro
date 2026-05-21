package com.re.busticketpro.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TripSearchForm {
    @NotNull
    private Long departureId;
    @NotNull
    private Long arrivalId;

    @FutureOrPresent
    private LocalDate departureDate;
}
