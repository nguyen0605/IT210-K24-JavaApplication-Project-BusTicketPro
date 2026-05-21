package com.re.busticketpro.service;

import com.re.busticketpro.dto.DriverForm;
import com.re.busticketpro.entity.Driver;
import com.re.busticketpro.enums.BusStatus;
import com.re.busticketpro.enums.DriverStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.BusRepository;
import com.re.busticketpro.repository.DriverRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;

    public DriverService(DriverRepository driverRepository, BusRepository busRepository) {
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
    }

    public Page<Driver> search(String fullName, String phone, String licenseClass, DriverStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        var pageable = PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "id"));
        return driverRepository.search(clean(fullName), clean(phone), clean(licenseClass), status, pageable);
    }

    public List<Driver> activeDrivers() {
        return driverRepository.findByStatusOrderByFullNameAsc(DriverStatus.ACTIVE);
    }

    public Driver get(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài xế"));
    }

    public boolean activeDriverExists(String fullName) {
        return driverRepository.existsByFullNameAndStatus(fullName, DriverStatus.ACTIVE);
    }

    @Transactional
    public Driver create(DriverForm form) {
        String phone = form.getPhone().trim();
        if (driverRepository.existsByPhone(phone)) {
            throw new BusinessException("Số điện thoại tài xế đã tồn tại");
        }
        Driver driver = new Driver();
        copy(form, driver);
        return driverRepository.save(driver);
    }

    @Transactional
    public void update(Long id, DriverForm form) {
        Driver driver = get(id);
        String phone = form.getPhone().trim();
        if (driverRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new BusinessException("Số điện thoại tài xế đã tồn tại");
        }
        DriverStatus oldStatus = driver.getStatus();
        copy(form, driver);
        syncAssignedBuses(driver, oldStatus);
    }

    @Transactional
    public void delete(Long id) {
        if (busRepository.existsByDriverId(id)) {
            throw new BusinessException("Không thể xóa tài xế đang được gán cho xe");
        }
        driverRepository.delete(get(id));
    }

    public DriverForm toForm(Driver driver) {
        DriverForm form = new DriverForm();
        form.setFullName(driver.getFullName());
        form.setPhone(driver.getPhone());
        form.setEmail(driver.getEmail());
        form.setLicenseClass(driver.getLicenseClass());
        form.setStatus(driver.getStatus());
        return form;
    }

    private void copy(DriverForm form, Driver driver) {
        driver.setFullName(form.getFullName().trim());
        driver.setPhone(form.getPhone().trim());
        driver.setEmail(clean(form.getEmail()));
        driver.setLicenseClass(form.getLicenseClass().trim());
        driver.setStatus(form.getStatus());
    }

    private void syncAssignedBuses(Driver driver, DriverStatus oldStatus) {
        var buses = busRepository.findByDriverId(driver.getId());
        for (var bus : buses) {
            bus.setDriverName(driver.getFullName());
            if (oldStatus == DriverStatus.ACTIVE && driver.getStatus() != DriverStatus.ACTIVE
                    && bus.getStatus() == BusStatus.ACTIVE) {
                bus.setStatus(BusStatus.MAINTENANCE);
            }
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
