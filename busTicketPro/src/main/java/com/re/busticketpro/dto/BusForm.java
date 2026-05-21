package com.re.busticketpro.dto;

import com.re.busticketpro.enums.BusStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusForm {
    @NotBlank(message = "Vui lòng nhập biển số xe")
    @Size(max = 30, message = "Biển số xe không được quá 30 ký tự")
    @Pattern(regexp = "^[0-9A-Z]{2,3}-?[0-9A-Z.]{4,10}$", message = "Biển số xe không hợp lệ")
    private String licensePlate;

    @NotBlank(message = "Vui lòng nhập loại xe")
    @Size(max = 60, message = "Loại xe không được quá 60 ký tự")
    private String busType;

    @NotNull(message = "Vui lòng nhập tổng số ghế")
    @Min(value = 1, message = "Tổng số ghế phải lớn hơn 0")
    @Max(value = 80, message = "Tổng số ghế không được quá 80")
    private Integer totalSeats;

    @NotBlank(message = "Vui lòng nhập hãng xe")
    @Size(max = 100, message = "Hãng xe không được quá 100 ký tự")
    private String companyName;

    private String driverName;

    @NotNull(message = "Vui lòng chọn tài xế")
    private Long driverId;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private BusStatus status = BusStatus.ACTIVE;
}
