package com.re.busticketpro.service;

import com.re.busticketpro.dto.BusForm;
import com.re.busticketpro.entity.Bus;
import com.re.busticketpro.entity.Driver;
import com.re.busticketpro.enums.BusStatus;
import com.re.busticketpro.enums.DriverStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.BusRepository;
import com.re.busticketpro.repository.DriverRepository;
import com.re.busticketpro.repository.TripRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusService {
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;

    public BusService(BusRepository busRepository, TripRepository tripRepository, DriverRepository driverRepository) {
        this.busRepository = busRepository;
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository;
    }

    public List<Bus> findAll() {
        return busRepository.findAll();
    }

    public Page<Bus> search(String licensePlate, String busType, BusStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        var pageable = PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "id"));
        return busRepository.search(clean(licensePlate), clean(busType), status, pageable);
    }

    public Bus get(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy xe"));
    }

    @Transactional
    public Bus create(BusForm form) {
        String licensePlate = form.getLicensePlate().trim();
        if (busRepository.existsByLicensePlate(licensePlate)) {
            throw new BusinessException("Biển số đã tồn tại");
        }
        Bus bus = new Bus();
        validateDriverAvailable(form.getDriverId(), null);
        copy(form, bus);
        return busRepository.save(bus);
    }

    @Transactional
    public void update(Long id, BusForm form) {
        Bus bus = get(id);
        String licensePlate = form.getLicensePlate().trim();
        if (busRepository.existsByLicensePlateAndIdNot(licensePlate, id)) {
            throw new BusinessException("Biển số đã tồn tại");
        }
        validateDriverAvailable(form.getDriverId(), id);
        copy(form, bus);
    }

    @Transactional
    public void delete(Long id) {
        try {
            busRepository.delete(get(id));
            busRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Không thể xóa xe đã được gán chuyến");
        }
    }

    public BusForm toForm(Bus bus) {
        BusForm form = new BusForm();
        form.setLicensePlate(bus.getLicensePlate());
        form.setBusType(bus.getBusType());
        form.setTotalSeats(bus.getTotalSeats());
        form.setCompanyName(bus.getCompanyName());
        form.setDriverName(bus.getDriverName());
        form.setDriverId(bus.getDriver() != null ? bus.getDriver().getId() : null);
        form.setStatus(bus.getStatus());
        return form;
    }

    private void copy(BusForm form, Bus bus) {
        Driver driver = driverRepository.findById(form.getDriverId())
                .orElseThrow(() -> new BusinessException("Vui lòng chọn tài xế trong hệ thống"));
        if (driver.getStatus() != DriverStatus.ACTIVE) {
            throw new BusinessException("Vui lòng chọn tài xế đang hoạt động trong hệ thống");
        }
        bus.setLicensePlate(form.getLicensePlate().trim());
        bus.setBusType(form.getBusType().trim());
        bus.setTotalSeats(form.getTotalSeats());
        bus.setCompanyName(form.getCompanyName().trim());
        bus.setDriver(driver);
        bus.setDriverName(driver.getFullName());
        bus.setStatus(form.getStatus());
    }

    private void validateDriverAvailable(Long driverId, Long currentBusId) {
        if (currentBusId == null) {
            if (busRepository.existsByDriverId(driverId)) {
                throw new BusinessException("Tài xế này đã được gán cho xe khác");
            }
            return;
        }
        if (busRepository.existsByDriverIdAndIdNot(driverId, currentBusId)) {
            throw new BusinessException("Tài xế này đã được gán cho xe khác");
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
