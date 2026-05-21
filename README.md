# Bus Ticket Pro

Bus Ticket Pro là ứng dụng Java Web monolithic cho hệ thống đặt vé xe khách liên tỉnh. Ứng dụng gồm giao diện Thymeleaf, Spring MVC controller, service nghiệp vụ, Spring Data JPA repository và database MySQL trong cùng một project.


```bash
$env:MAIL_USERNAME="gmail cua ban@gmail.com"
$env:MAIL_PASSWORD=""
$env:MAIL_FROM="gmail cua ban@gmail.com"
.\gradlew.bat bootRun
```

Sau khi chạy, mở:

```text
http://localhost:8080
```

## Tài khoản demo

- Admin: `admin` / `admin123`
- Staff: `staff01` / `staff123`
- Passenger: `passenger01` / `passenger123`

Nếu database đang có password demo, ứng dụng sẽ tự thay bằng BCrypt thật cho các tài khoản demo khi khởi động.

## Luồng test thủ công

1. Đăng nhập `admin`, vào `/admin/buses`, thêm/sửa/xóa xe.
2. Vào `/trips/search`, tìm chuyến Hà Nội -> Hải Phòng ngày `2026-06-01`.
3. Chọn chuyến, chọn ghế còn trống, nhập thông tin hành khách và đặt vé.
4. Lưu mã vé, vào `/tickets/lookup` tra cứu bằng mã vé và số điện thoại.
5. Đăng nhập `staff01`, vào `/staff/tickets/pending`, xác nhận thanh toán hoặc hủy vé.
6. Thử hủy vé từ trang tra cứu bằng passenger trước mốc 12 tiếng so với giờ khởi hành.

## Transaction chống trùng ghế

`BookingService.bookTicket(...)` chạy trong `@Transactional`. Service gọi `SeatRepository.findSeatForUpdate(...)` với:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

Ghế được khóa theo `seat_id` và `trip_id` trước khi kiểm tra trạng thái. Nếu ghế không còn `AVAILABLE`, transaction ném lỗi nghiệp vụ và rollback, không tạo vé mồ côi và không giữ sai ghế.

## Scheduler hủy vé quá hạn

`TicketCleanupScheduler` chạy mỗi 5 phút. Scheduler gọi service transaction để tìm vé `PENDING` có `expired_time < now`, chuyển vé sang `CANCELLED`, giải phóng ghế về `AVAILABLE`, xóa `locked_until` và ghi `cancel_reason = AUTO_CANCEL_EXPIRED_PAYMENT`.

## Lệnh kiểm tra đã chạy

```bash
.\gradlew.bat test
```

Kết quả: build và 5 test pass.
