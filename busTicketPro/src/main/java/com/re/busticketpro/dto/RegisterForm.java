package com.re.busticketpro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {
    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp t\u00ean \u0111\u0103ng nh\u1eadp")
    @Size(min = 4, max = 50, message = "T\u00ean \u0111\u0103ng nh\u1eadp ph\u1ea3i t\u1eeb 4 \u0111\u1ebfn 50 k\u00fd t\u1ef1")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "T\u00ean \u0111\u0103ng nh\u1eadp ch\u1ec9 g\u1ed3m ch\u1eef, s\u1ed1 v\u00e0 d\u1ea5u g\u1ea1ch d\u01b0\u1edbi")
    private String username;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp m\u1eadt kh\u1ea9u")
    @Size(min = 6, message = "M\u1eadt kh\u1ea9u ph\u1ea3i c\u00f3 t\u1ed1i thi\u1ec3u 6 k\u00fd t\u1ef1")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$",
            message = "M\u1eadt kh\u1ea9u ph\u1ea3i c\u00f3 \u00edt nh\u1ea5t 1 ch\u1eef in hoa, 1 s\u1ed1, 1 k\u00fd t\u1ef1 \u0111\u1eb7c bi\u1ec7t v\u00e0 t\u1ed1i thi\u1ec3u 6 k\u00fd t\u1ef1"
    )
    private String password;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp l\u1ea1i m\u1eadt kh\u1ea9u")
    private String confirmPassword;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp h\u1ecd t\u00ean")
    @Size(max = 100, message = "H\u1ecd t\u00ean kh\u00f4ng \u0111\u01b0\u1ee3c qu\u00e1 100 k\u00fd t\u1ef1")
    private String fullName;

    @NotBlank(message = "Vui l\u00f2ng nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i kh\u00f4ng h\u1ee3p l\u1ec7")
    private String phone;

    @Email(message = "Email kh\u00f4ng h\u1ee3p l\u1ec7")
    private String email;

    @Size(max = 255, message = "\u0110\u1ecba ch\u1ec9 kh\u00f4ng \u0111\u01b0\u1ee3c qu\u00e1 255 k\u00fd t\u1ef1")
    private String address;

    @AssertTrue(message = "M\u1eadt kh\u1ea9u nh\u1eadp l\u1ea1i kh\u00f4ng kh\u1edbp")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
