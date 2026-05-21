package com.re.busticketpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {
    @NotBlank
    private String fullName;
    @NotBlank
    private String phone;
    @Email
    private String email;
    private String address;
}
