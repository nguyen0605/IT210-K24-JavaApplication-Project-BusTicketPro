-- =========================================================
-- BUS TICKET PRO - FULL DATABASE INIT SCRIPT
-- Project: Hệ thống Đặt vé Xe khách Liên tỉnh
-- Database: MySQL 8+
-- =========================================================

CREATE DATABASE IF NOT EXISTS bus_ticket_pro
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE bus_ticket_pro;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS buses;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. USERS
-- =========================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('PASSENGER', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'PASSENGER',
    status ENUM('ACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================================================
-- 2. LOCATIONS
-- Seed cứng tỉnh/thành
-- =========================================================

CREATE TABLE locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    province_code VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE'
);

-- TÀI XẾ --
CREATE TABLE drivers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(120),
    license_class VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_drivers_phone UNIQUE (phone)
);

-- =========================================================
-- 3. ROUTES
-- Seed cứng tuyến đường
-- =========================================================

CREATE TABLE routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    departure_location_id BIGINT NOT NULL,
    arrival_location_id BIGINT NOT NULL,
    distance_km INT NOT NULL,
    estimated_duration_minutes INT,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_routes_departure
        FOREIGN KEY (departure_location_id) REFERENCES locations(id),

    CONSTRAINT fk_routes_arrival
        FOREIGN KEY (arrival_location_id) REFERENCES locations(id),

    CONSTRAINT uq_route UNIQUE (departure_location_id, arrival_location_id),

    CONSTRAINT ck_route_different_location
        CHECK (departure_location_id <> arrival_location_id)
);

-- =========================================================
-- 4. BUSES
-- Admin CRUD bảng này
-- =========================================================

CREATE TABLE buses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(30) NOT NULL UNIQUE,
    bus_type VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    brand VARCHAR(100),
    driver_name VARCHAR(150),
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

-- =========================================================
-- 5. TRIPS
-- Mỗi chuyến xe = route + bus + ngày giờ khởi hành
-- =========================================================

CREATE TABLE trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NULL,
    price DECIMAL(12,2) NOT NULL,
    status ENUM('SCHEDULED', 'DEPARTED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trips_route
        FOREIGN KEY (route_id) REFERENCES routes(id),

    CONSTRAINT fk_trips_bus
        FOREIGN KEY (bus_id) REFERENCES buses(id),

    INDEX idx_trips_search (route_id, departure_time, status)
);

-- =========================================================
-- 6. SEATS
-- Mỗi ghế thuộc về một chuyến xe cụ thể
-- status:
-- AVAILABLE = còn trống
-- PENDING   = đang giữ/chờ thanh toán
-- BOOKED    = đã thanh toán/đã đặt chắc
-- =========================================================

CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    seat_floor ENUM('LOWER', 'UPPER', 'NORMAL') NOT NULL DEFAULT 'NORMAL',
    seat_row INT,
    seat_col INT,
    status ENUM('AVAILABLE', 'PENDING', 'BOOKED') NOT NULL DEFAULT 'AVAILABLE',
    locked_until DATETIME NULL,
    version INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_seats_trip
        FOREIGN KEY (trip_id) REFERENCES trips(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_trip_seat UNIQUE (trip_id, seat_number),

    INDEX idx_seats_trip_status (trip_id, status),
    INDEX idx_seats_locked_until (locked_until)
);

-- =========================================================
-- 7. TICKETS
-- Vé đặt
-- Có generated column active_seat_id để chống 2 vé active cùng 1 ghế
-- =========================================================

CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,

    user_id BIGINT NULL,
    trip_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,

    passenger_name VARCHAR(150) NOT NULL,
    passenger_phone VARCHAR(20) NOT NULL,
    passenger_email VARCHAR(150),

    total_amount DECIMAL(12,2) NOT NULL,

    status ENUM('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',

    booking_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_time DATETIME NULL,
    cancelled_time DATETIME NULL,
    expired_time DATETIME NULL,

    cancel_reason VARCHAR(255),

    active_seat_id BIGINT GENERATED ALWAYS AS (
        CASE
            WHEN status IN ('PENDING', 'PAID') THEN seat_id
            ELSE NULL
        END
    ) STORED,

    CONSTRAINT fk_tickets_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_tickets_trip
        FOREIGN KEY (trip_id) REFERENCES trips(id),

    CONSTRAINT fk_tickets_seat
        FOREIGN KEY (seat_id) REFERENCES seats(id),

    CONSTRAINT uq_active_ticket_per_seat UNIQUE (active_seat_id),

    INDEX idx_tickets_lookup (ticket_code, passenger_phone),
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_trip (trip_id),
    INDEX idx_tickets_expired_time (expired_time)
);

-- =========================================================
-- 8. PAYMENTS
-- Optional cho mở rộng VNPay/Momo
-- =========================================================

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    provider VARCHAR(50),
    amount DECIMAL(12,2) NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_code VARCHAR(100),
    raw_response TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);

-- =========================================================
-- 9. SEED USERS
-- Password dưới đây chỉ là demo hash giả.
-- Khi code Java phải dùng BCrypt thật.
-- Ví dụ BCrypt hash sẽ có dạng $2a$10$...
-- =========================================================

INSERT INTO users (username, password_hash, role, status) VALUES
('admin', '$2a$10$demo_admin_hash_replace_in_java', 'ADMIN', 'ACTIVE'),
('staff01', '$2a$10$demo_staff_hash_replace_in_java', 'STAFF', 'ACTIVE'),
('passenger01', '$2a$10$demo_passenger_hash_replace_in_java', 'PASSENGER', 'ACTIVE');

INSERT INTO user_profiles (user_id, full_name, phone, email, address) VALUES
(1, 'Quản trị viên', '0900000001', 'admin@busticketpro.vn', 'Hà Nội'),
(2, 'Nhân viên bán vé', '0900000002', 'staff01@busticketpro.vn', 'Hà Nội'),
(3, 'Nguyễn Văn Khách', '0900000003', 'passenger01@gmail.com', 'Nam Định');

-- =========================================================
-- 10. SEED LOCATIONS
-- =========================================================

INSERT INTO locations (name, province_code) VALUES
('Hà Nội', 'HN'),
('Hải Phòng', 'HP'),
('Nam Định', 'ND'),
('Ninh Bình', 'NB'),
('Thanh Hóa', 'TH'),
('Nghệ An', 'NA'),
('Đà Nẵng', 'DN'),
('Huế', 'HUE');

-- =========================================================
-- 11. SEED ROUTES
-- =========================================================

INSERT INTO routes (
    departure_location_id,
    arrival_location_id,
    distance_km,
    estimated_duration_minutes
)
VALUES
(1, 2, 120, 150),
(1, 3, 90, 120),
(1, 4, 95, 120),
(1, 5, 160, 210),
(1, 6, 300, 420),
(2, 1, 120, 150),
(3, 1, 90, 120),
(4, 1, 95, 120),
(5, 1, 160, 210),
(6, 1, 300, 420),
(1, 7, 760, 900),
(7, 1, 760, 900),
(1, 8, 670, 780),
(8, 1, 670, 780);

-- =========================================================
-- 12. SEED BUSES
-- =========================================================

INSERT INTO buses (
    license_plate,
    bus_type,
    total_seats,
    brand,
    driver_name
)
VALUES
('29B-12345', 'Limousine 22 phòng', 22, 'Thaco', 'Nguyễn Văn Bình'),
('29B-67890', 'Giường nằm 40 chỗ', 40, 'Hyundai', 'Trần Văn Nam'),
('30F-24680', 'Xe khách 45 chỗ', 45, 'Universe', 'Phạm Văn Hùng');

-- =========================================================
-- 13. SEED TRIPS
-- =========================================================

INSERT INTO trips (
    route_id,
    bus_id,
    departure_time,
    arrival_time,
    price
)
VALUES
(1, 1, '2026-06-01 08:00:00', '2026-06-01 10:30:00', 180000),
(2, 2, '2026-06-01 09:00:00', '2026-06-01 11:00:00', 150000),
(5, 2, '2026-06-01 20:00:00', '2026-06-02 03:00:00', 350000),
(11, 3, '2026-06-02 18:00:00', '2026-06-03 09:00:00', 650000);

-- =========================================================
-- 14. SEED SEATS
-- Demo ghế cho từng trip
-- =========================================================

-- Trip 1 - Limousine 22 phòng
INSERT INTO seats (trip_id, seat_number, seat_floor, seat_row, seat_col)
VALUES
(1, 'A01', 'LOWER', 1, 1),
(1, 'A02', 'LOWER', 1, 2),
(1, 'A03', 'LOWER', 2, 1),
(1, 'A04', 'LOWER', 2, 2),
(1, 'A05', 'LOWER', 3, 1),
(1, 'A06', 'LOWER', 3, 2),
(1, 'A07', 'LOWER', 4, 1),
(1, 'A08', 'LOWER', 4, 2),
(1, 'A09', 'LOWER', 5, 1),
(1, 'A10', 'LOWER', 5, 2),
(1, 'A11', 'LOWER', 6, 1),

(1, 'B01', 'UPPER', 1, 1),
(1, 'B02', 'UPPER', 1, 2),
(1, 'B03', 'UPPER', 2, 1),
(1, 'B04', 'UPPER', 2, 2),
(1, 'B05', 'UPPER', 3, 1),
(1, 'B06', 'UPPER', 3, 2),
(1, 'B07', 'UPPER', 4, 1),
(1, 'B08', 'UPPER', 4, 2),
(1, 'B09', 'UPPER', 5, 1),
(1, 'B10', 'UPPER', 5, 2),
(1, 'B11', 'UPPER', 6, 1);

-- Trip 2 - Giường nằm 40 chỗ
INSERT INTO seats (trip_id, seat_number, seat_floor, seat_row, seat_col)
VALUES
(2, 'A01', 'LOWER', 1, 1),
(2, 'A02', 'LOWER', 1, 2),
(2, 'A03', 'LOWER', 2, 1),
(2, 'A04', 'LOWER', 2, 2),
(2, 'A05', 'LOWER', 3, 1),
(2, 'A06', 'LOWER', 3, 2),
(2, 'A07', 'LOWER', 4, 1),
(2, 'A08', 'LOWER', 4, 2),
(2, 'A09', 'LOWER', 5, 1),
(2, 'A10', 'LOWER', 5, 2),
(2, 'A11', 'LOWER', 6, 1),
(2, 'A12', 'LOWER', 6, 2),
(2, 'A13', 'LOWER', 7, 1),
(2, 'A14', 'LOWER', 7, 2),
(2, 'A15', 'LOWER', 8, 1),
(2, 'A16', 'LOWER', 8, 2),
(2, 'A17', 'LOWER', 9, 1),
(2, 'A18', 'LOWER', 9, 2),
(2, 'A19', 'LOWER', 10, 1),
(2, 'A20', 'LOWER', 10, 2),

(2, 'B01', 'UPPER', 1, 1),
(2, 'B02', 'UPPER', 1, 2),
(2, 'B03', 'UPPER', 2, 1),
(2, 'B04', 'UPPER', 2, 2),
(2, 'B05', 'UPPER', 3, 1),
(2, 'B06', 'UPPER', 3, 2),
(2, 'B07', 'UPPER', 4, 1),
(2, 'B08', 'UPPER', 4, 2),
(2, 'B09', 'UPPER', 5, 1),
(2, 'B10', 'UPPER', 5, 2),
(2, 'B11', 'UPPER', 6, 1),
(2, 'B12', 'UPPER', 6, 2),
(2, 'B13', 'UPPER', 7, 1),
(2, 'B14', 'UPPER', 7, 2),
(2, 'B15', 'UPPER', 8, 1),
(2, 'B16', 'UPPER', 8, 2),
(2, 'B17', 'UPPER', 9, 1),
(2, 'B18', 'UPPER', 9, 2),
(2, 'B19', 'UPPER', 10, 1),
(2, 'B20', 'UPPER', 10, 2);

-- Trip 3 - Giường nằm 40 chỗ
INSERT INTO seats (trip_id, seat_number, seat_floor, seat_row, seat_col)
SELECT 3, seat_number, seat_floor, seat_row, seat_col
FROM seats
WHERE trip_id = 2;

-- Trip 4 - Xe khách 45 chỗ
INSERT INTO seats (trip_id, seat_number, seat_floor, seat_row, seat_col)
VALUES
(4, 'S01', 'NORMAL', 1, 1),
(4, 'S02', 'NORMAL', 1, 2),
(4, 'S03', 'NORMAL', 1, 3),
(4, 'S04', 'NORMAL', 1, 4),
(4, 'S05', 'NORMAL', 2, 1),
(4, 'S06', 'NORMAL', 2, 2),
(4, 'S07', 'NORMAL', 2, 3),
(4, 'S08', 'NORMAL', 2, 4),
(4, 'S09', 'NORMAL', 3, 1),
(4, 'S10', 'NORMAL', 3, 2),
(4, 'S11', 'NORMAL', 3, 3),
(4, 'S12', 'NORMAL', 3, 4),
(4, 'S13', 'NORMAL', 4, 1),
(4, 'S14', 'NORMAL', 4, 2),
(4, 'S15', 'NORMAL', 4, 3),
(4, 'S16', 'NORMAL', 4, 4),
(4, 'S17', 'NORMAL', 5, 1),
(4, 'S18', 'NORMAL', 5, 2),
(4, 'S19', 'NORMAL', 5, 3),
(4, 'S20', 'NORMAL', 5, 4),
(4, 'S21', 'NORMAL', 6, 1),
(4, 'S22', 'NORMAL', 6, 2),
(4, 'S23', 'NORMAL', 6, 3),
(4, 'S24', 'NORMAL', 6, 4),
(4, 'S25', 'NORMAL', 7, 1),
(4, 'S26', 'NORMAL', 7, 2),
(4, 'S27', 'NORMAL', 7, 3),
(4, 'S28', 'NORMAL', 7, 4),
(4, 'S29', 'NORMAL', 8, 1),
(4, 'S30', 'NORMAL', 8, 2),
(4, 'S31', 'NORMAL', 8, 3),
(4, 'S32', 'NORMAL', 8, 4),
(4, 'S33', 'NORMAL', 9, 1),
(4, 'S34', 'NORMAL', 9, 2),
(4, 'S35', 'NORMAL', 9, 3),
(4, 'S36', 'NORMAL', 9, 4),
(4, 'S37', 'NORMAL', 10, 1),
(4, 'S38', 'NORMAL', 10, 2),
(4, 'S39', 'NORMAL', 10, 3),
(4, 'S40', 'NORMAL', 10, 4),
(4, 'S41', 'NORMAL', 11, 1),
(4, 'S42', 'NORMAL', 11, 2),
(4, 'S43', 'NORMAL', 11, 3),
(4, 'S44', 'NORMAL', 11, 4),
(4, 'S45', 'NORMAL', 12, 1);

-- SEED TAI XE --
INSERT INTO drivers (full_name, phone, email, license_class, status)
VALUES
('Nguyễn Văn Lộc', '0901234567', 'loc.nguyen@buspro.vn', 'E', 'ACTIVE'),
('Trần Minh Hoàng', '0902345678', 'hoang.tran@buspro.vn', 'D', 'ACTIVE'),
('Phạm Tuấn Anh', '0903456789', 'anh.pham@buspro.vn', 'FC', 'ON_LEAVE');

-- =========================================================
-- 15. VIEW: TRA CỨU VÉ CHI TIẾT
-- Dùng cho CORE-07
-- =========================================================

CREATE OR REPLACE VIEW v_ticket_detail AS
SELECT 
    tk.id AS ticket_id,
    tk.ticket_code,
    tk.passenger_name,
    tk.passenger_phone,
    tk.passenger_email,
    tk.total_amount,
    tk.status AS ticket_status,
    tk.booking_time,
    tk.payment_time,
    tk.cancelled_time,
    tk.expired_time,

    s.id AS seat_id,
    s.seat_number,
    s.seat_floor,
    s.status AS seat_status,

    t.id AS trip_id,
    t.departure_time,
    t.arrival_time,
    t.price,

    b.id AS bus_id,
    b.license_plate,
    b.bus_type,
    b.total_seats,
    b.brand,
    b.driver_name,

    r.id AS route_id,
    dl.name AS departure_location,
    al.name AS arrival_location,
    r.distance_km,
    r.estimated_duration_minutes
FROM tickets tk
JOIN trips t ON tk.trip_id = t.id
JOIN routes r ON t.route_id = r.id
JOIN locations dl ON r.departure_location_id = dl.id
JOIN locations al ON r.arrival_location_id = al.id
JOIN buses b ON t.bus_id = b.id
JOIN seats s ON tk.seat_id = s.id;

-- =========================================================
-- 16. QUERY MẪU: TÌM CHUYẾN XE
-- Dùng trong Java với PreparedStatement / Repository
-- =========================================================

-- SELECT 
--     t.id AS trip_id,
--     dl.name AS departure_location,
--     al.name AS arrival_location,
--     t.departure_time,
--     t.arrival_time,
--     t.price,
--     b.license_plate,
--     b.bus_type,
--     b.total_seats,
--     b.driver_name
-- FROM trips t
-- JOIN routes r ON t.route_id = r.id
-- JOIN locations dl ON r.departure_location_id = dl.id
-- JOIN locations al ON r.arrival_location_id = al.id
-- JOIN buses b ON t.bus_id = b.id
-- WHERE r.departure_location_id = ?
--   AND r.arrival_location_id = ?
--   AND DATE(t.departure_time) = ?
--   AND t.status = 'SCHEDULED';

-- =========================================================
-- 17. QUERY MẪU: LẤY SƠ ĐỒ GHẾ
-- =========================================================

-- SELECT 
--     id,
--     seat_number,
--     seat_floor,
--     seat_row,
--     seat_col,
--     status,
--     locked_until
-- FROM seats
-- WHERE trip_id = ?
-- ORDER BY seat_floor, seat_row, seat_col;

-- =========================================================
-- 18. TRANSACTION MẪU: ĐẶT VÉ CHỐNG TRÙNG GHẾ
-- Logic này nên đặt trong Java Service.
-- Không chạy trực tiếp nếu chưa thay biến.
-- =========================================================

-- START TRANSACTION;
--
-- SELECT id, status
-- FROM seats
-- WHERE id = ?
--   AND trip_id = ?
-- FOR UPDATE;
--
-- UPDATE seats
-- SET status = 'PENDING',
--     locked_until = DATE_ADD(NOW(), INTERVAL 15 MINUTE),
--     version = version + 1
-- WHERE id = ?
--   AND trip_id = ?
--   AND status = 'AVAILABLE';
--
-- INSERT INTO tickets (
--     ticket_code,
--     user_id,
--     trip_id,
--     seat_id,
--     passenger_name,
--     passenger_phone,
--     passenger_email,
--     total_amount,
--     status,
--     expired_time
-- )
-- VALUES (
--     ?,
--     ?,
--     ?,
--     ?,
--     ?,
--     ?,
--     ?,
--     ?,
--     'PENDING',
--     DATE_ADD(NOW(), INTERVAL 30 MINUTE)
-- );
--
-- COMMIT;

-- =========================================================
-- 19. TRANSACTION MẪU: NHÂN VIÊN XÁC NHẬN THANH TOÁN
-- Ticket PENDING -> PAID
-- Seat PENDING -> BOOKED
-- =========================================================

-- START TRANSACTION;
--
-- SELECT id, seat_id, status
-- FROM tickets
-- WHERE id = ?
-- FOR UPDATE;
--
-- UPDATE tickets
-- SET status = 'PAID',
--     payment_time = NOW()
-- WHERE id = ?
--   AND status = 'PENDING';
--
-- UPDATE seats
-- SET status = 'BOOKED',
--     locked_until = NULL,
--     version = version + 1
-- WHERE id = ?
--   AND status = 'PENDING';
--
-- COMMIT;

-- =========================================================
-- 20. TRANSACTION MẪU: HỦY VÉ VÀ GIẢI PHÓNG GHẾ
-- =========================================================

-- START TRANSACTION;
--
-- SELECT id, seat_id, status
-- FROM tickets
-- WHERE id = ?
-- FOR UPDATE;
--
-- UPDATE tickets
-- SET status = 'CANCELLED',
--     cancelled_time = NOW(),
--     cancel_reason = ?
-- WHERE id = ?
--   AND status IN ('PENDING', 'PAID');
--
-- UPDATE seats
-- SET status = 'AVAILABLE',
--     locked_until = NULL,
--     version = version + 1
-- WHERE id = ?;
--
-- COMMIT;

-- =========================================================
-- 21. QUERY MẪU: TRA CỨU VÉ BẰNG MÃ VÉ + SĐT
-- =========================================================

-- SELECT *
-- FROM v_ticket_detail
-- WHERE ticket_code = ?
--   AND passenger_phone = ?;

-- =========================================================
-- 22. QUERY MẪU: DANH SÁCH VÉ CHỜ THANH TOÁN CHO STAFF
-- =========================================================

-- SELECT *
-- FROM v_ticket_detail
-- WHERE ticket_status = 'PENDING'
-- ORDER BY booking_time ASC;

-- =========================================================
-- 23. QUERY MẪU: HỦY VÉ PENDING QUÁ 30 PHÚT
-- Dùng nếu làm extension cron job
-- =========================================================

-- START TRANSACTION;
--
-- UPDATE seats s
-- JOIN tickets tk ON tk.seat_id = s.id
-- SET s.status = 'AVAILABLE',
--     s.locked_until = NULL,
--     s.version = s.version + 1
-- WHERE tk.status = 'PENDING'
--   AND tk.expired_time < NOW();
--
-- UPDATE tickets
-- SET status = 'CANCELLED',
--     cancelled_time = NOW(),
--     cancel_reason = 'AUTO_CANCEL_EXPIRED_PAYMENT'
-- WHERE status = 'PENDING'
--   AND expired_time < NOW();
--
-- COMMIT;

-- =========================================================
-- 24. TEST NHANH SAU KHI IMPORT
-- =========================================================

SELECT 'DATABASE INIT SUCCESSFULLY' AS message;

SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_locations FROM locations;
SELECT COUNT(*) AS total_routes FROM routes;
SELECT COUNT(*) AS total_buses FROM buses;
SELECT COUNT(*) AS total_trips FROM trips;
SELECT COUNT(*) AS total_seats FROM seats;

ALTER TABLE user_profiles
ADD CONSTRAINT uk_user_profiles_phone UNIQUE (phone);

-- thêm quan hệ thật giữa Bus và Driver--
ALTER TABLE buses
ADD COLUMN driver_id BIGINT NULL;

ALTER TABLE buses
ADD CONSTRAINT fk_buses_driver
FOREIGN KEY (driver_id) REFERENCES drivers(id)
ON DELETE SET NULL;

UPDATE buses b
JOIN drivers d ON d.full_name = b.driver_name
SET b.driver_id = d.id;

ALTER TABLE buses
ADD CONSTRAINT uk_buses_driver UNIQUE (driver_id);