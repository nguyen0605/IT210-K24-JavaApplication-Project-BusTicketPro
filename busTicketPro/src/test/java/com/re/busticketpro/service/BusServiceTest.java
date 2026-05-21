package com.re.busticketpro.service;

import com.re.busticketpro.dto.BusForm;
import com.re.busticketpro.dto.DriverForm;
import com.re.busticketpro.entity.Driver;
import com.re.busticketpro.enums.BusStatus;
import com.re.busticketpro.enums.DriverStatus;
import com.re.busticketpro.repository.BusRepository;
import com.re.busticketpro.repository.DriverRepository;
import com.re.busticketpro.repository.SeatRepository;
import com.re.busticketpro.repository.TicketRepository;
import com.re.busticketpro.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BusServiceTest {
    @Autowired BusService busService;
    @Autowired DriverService driverService;
    @Autowired BusRepository busRepository;
    @Autowired TicketRepository ticketRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired TripRepository tripRepository;
    @Autowired DriverRepository driverRepository;

    private Driver driver;
    private Driver updatedDriver;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        seatRepository.deleteAll();
        tripRepository.deleteAll();
        busRepository.deleteAll();
        driverRepository.deleteAll();
        driver = driverRepository.save(driver("Driver", "0901111111"));
        updatedDriver = driverRepository.save(driver("Updated Driver", "0902222222"));
    }

    @Test
    void createUpdateDeleteBus() {
        var bus = busService.create(form("29B-11111"));
        assertThat(bus.getId()).isNotNull();

        BusForm update = form("29B-22222");
        update.setDriverId(updatedDriver.getId());
        busService.update(bus.getId(), update);
        assertThat(busRepository.findById(bus.getId()).orElseThrow().getDriverName()).isEqualTo("Updated Driver");

        busService.delete(bus.getId());
        assertThat(busRepository.existsById(bus.getId())).isFalse();
    }

    @Test
    void changingAssignedDriverToInactiveMovesActiveBusToMaintenance() {
        var bus = busService.create(form("29B-33333"));

        DriverForm form = driverService.toForm(driver);
        form.setStatus(DriverStatus.INACTIVE);
        driverService.update(driver.getId(), form);

        var updatedBus = busRepository.findById(bus.getId()).orElseThrow();
        assertThat(updatedBus.getStatus()).isEqualTo(BusStatus.MAINTENANCE);
        assertThat(updatedBus.getDriverName()).isEqualTo(driver.getFullName());
    }

    @Test
    void cannotAssignSameDriverToTwoBuses() {
        busService.create(form("29B-44444"));

        assertThatThrownBy(() -> busService.create(form("29B-55555")))
                .hasMessageContaining("Tài xế này đã được gán cho xe khác");
    }

    private BusForm form(String plate) {
        BusForm form = new BusForm();
        form.setLicensePlate(plate);
        form.setBusType("Limousine");
        form.setTotalSeats(16);
        form.setCompanyName("Test Bus");
        form.setDriverName("Driver");
        form.setDriverId(driver.getId());
        form.setStatus(BusStatus.ACTIVE);
        return form;
    }

    private Driver driver(String fullName, String phone) {
        Driver driver = new Driver();
        driver.setFullName(fullName);
        driver.setPhone(phone);
        driver.setEmail(fullName.toLowerCase().replace(" ", ".") + "@test.local");
        driver.setLicenseClass("E");
        driver.setStatus(DriverStatus.ACTIVE);
        return driver;
    }
}
