package com.re.busticketpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {
    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp t\u00ean \u0111\u0103ng nh\u1eadp")
    private String username;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp m\u1eadt kh\u1ea9u")
    private String password;

    private String loginScope = "PASSENGER";
}
