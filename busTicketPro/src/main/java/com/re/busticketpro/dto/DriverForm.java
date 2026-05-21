package com.re.busticketpro.dto;

import com.re.busticketpro.enums.DriverStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverForm {
    @NotBlank(message = "Vui lòng nhập họ tên tài xế")
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 120, message = "Email không được quá 120 ký tự")
    private String email;

    @NotBlank(message = "Vui lòng nhập hạng bằng lái")
    @Size(max = 20, message = "Hạng bằng lái không được quá 20 ký tự")
    private String licenseClass;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private DriverStatus status = DriverStatus.ACTIVE;
}
