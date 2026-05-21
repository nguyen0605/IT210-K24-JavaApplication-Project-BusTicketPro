package com.re.busticketpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {
    @NotNull
    private Long tripId;
    @NotNull
    private Long seatId;
    @NotBlank
    private String passengerName;
    @NotBlank
    private String passengerPhone;
    @NotBlank
    @Email
    private String passengerEmail;
}
