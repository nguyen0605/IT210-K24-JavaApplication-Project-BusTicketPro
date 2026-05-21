package com.re.busticketpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketLookupForm {
    @NotBlank
    private String ticketCode;
    @NotBlank
    private String passengerPhone;
}
