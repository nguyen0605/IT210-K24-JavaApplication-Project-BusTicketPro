package com.re.busticketpro.controller;

import com.re.busticketpro.dto.BusForm;
import com.re.busticketpro.dto.DriverForm;
import com.re.busticketpro.dto.ProfileForm;
import com.re.busticketpro.enums.BusStatus;
import com.re.busticketpro.enums.DriverStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.LocationRepository;
import com.re.busticketpro.repository.RouteRepository;
import com.re.busticketpro.service.AdminDashboardService;
import com.re.busticketpro.service.BusService;
import com.re.busticketpro.service.DriverService;
import com.re.busticketpro.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final BusService busService;
    private final DriverService driverService;
    private final AdminDashboardService dashboardService;
    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;
    private final ProfileService profileService;

    public AdminController(BusService busService, DriverService driverService, AdminDashboardService dashboardService,
                           LocationRepository locationRepository, RouteRepository routeRepository,
                           ProfileService profileService) {
        this.busService = busService;
        this.driverService = driverService;
        this.dashboardService = dashboardService;
        this.locationRepository = locationRepository;
        this.routeRepository = routeRepository;
        this.profileService = profileService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.stats());
        return "admin/dashboard";
    }

    @GetMapping("/drivers")
    public String drivers(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String fullName,
                          @RequestParam(required = false) String phone,
                          @RequestParam(required = false) String licenseClass,
                          @RequestParam(required = false) DriverStatus status,
                          Model model) {
        var driverPage = driverService.search(fullName, phone, licenseClass, status, page, 5);
        model.addAttribute("driverPage", driverPage);
        model.addAttribute("drivers", driverPage.getContent());
        model.addAttribute("fullName", fullName);
        model.addAttribute("phone", phone);
        model.addAttribute("licenseClass", licenseClass);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("driverStatuses", DriverStatus.values());
        return "admin/drivers";
    }

    @GetMapping("/drivers/create")
    public String createDriver(Model model) {
        model.addAttribute("form", new DriverForm());
        model.addAttribute("driverStatuses", DriverStatus.values());
        return "admin/driver-form";
    }

    @PostMapping("/drivers")
    public String storeDriver(@Valid @ModelAttribute("form") DriverForm form, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("driverStatuses", DriverStatus.values());
            return "admin/driver-form";
        }
        try {
            driverService.create(form);
        } catch (BusinessException ex) {
            bindingResult.rejectValue("phone", "phone.exists", ex.getMessage());
            model.addAttribute("driverStatuses", DriverStatus.values());
            return "admin/driver-form";
        }
        redirectAttributes.addFlashAttribute("success", "Đã thêm tài xế");
        return "redirect:/admin/drivers";
    }

    @GetMapping("/drivers/{id}/edit")
    public String editDriver(@PathVariable Long id, Model model) {
        model.addAttribute("driverId", id);
        model.addAttribute("form", driverService.toForm(driverService.get(id)));
        model.addAttribute("driverStatuses", DriverStatus.values());
        return "admin/driver-form";
    }

    @PostMapping("/drivers/{id}")
    public String updateDriver(@PathVariable Long id, @Valid @ModelAttribute("form") DriverForm form,
                               BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("driverId", id);
            model.addAttribute("driverStatuses", DriverStatus.values());
            return "admin/driver-form";
        }
        try {
            driverService.update(id, form);
        } catch (BusinessException ex) {
            bindingResult.rejectValue("phone", "phone.exists", ex.getMessage());
            model.addAttribute("driverId", id);
            model.addAttribute("driverStatuses", DriverStatus.values());
            return "admin/driver-form";
        }
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật tài xế");
        return "redirect:/admin/drivers";
    }

    @PostMapping("/drivers/{id}/delete")
    public String deleteDriver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            driverService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tài xế");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/drivers";
    }

    @GetMapping("/routes")
    public String routes(@RequestParam(required = false) Long departureId,
                         @RequestParam(required = false) Long arrivalId,
                         Model model) {
        model.addAttribute("locations", locationRepository.findAllByOrderByNameAsc());
        model.addAttribute("routes", routeRepository.search(departureId, arrivalId));
        model.addAttribute("departureId", departureId);
        model.addAttribute("arrivalId", arrivalId);
        return "admin/routes";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("form", profileService.getForm(authentication.getName()));
        return "admin/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("form") ProfileForm form, BindingResult bindingResult,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/profile";
        }
        profileService.update(authentication.getName(), form);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật hồ sơ");
        return "redirect:/admin/profile";
    }

    @GetMapping("/buses")
    public String buses(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) String licensePlate,
                        @RequestParam(required = false) String busType,
                        @RequestParam(required = false) BusStatus status,
                        Model model) {
        var busPage = busService.search(licensePlate, busType, status, page, 5);
        model.addAttribute("busPage", busPage);
        model.addAttribute("buses", busPage.getContent());
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("busType", busType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", BusStatus.values());
        return "admin/buses";
    }

    @GetMapping("/buses/create")
    public String create(Model model) {
        model.addAttribute("form", new BusForm());
        model.addAttribute("statuses", BusStatus.values());
        model.addAttribute("drivers", driverService.activeDrivers());
        return "admin/bus-form";
    }

    @PostMapping("/buses")
    public String store(@Valid @ModelAttribute("form") BusForm form, BindingResult bindingResult, Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addBusFormOptions(model);
            return "admin/bus-form";
        }
        try {
            busService.create(form);
        } catch (BusinessException ex) {
            rejectBusError(bindingResult, ex);
            addBusFormOptions(model);
            return "admin/bus-form";
        }
        redirectAttributes.addFlashAttribute("success", "Đã thêm xe");
        return "redirect:/admin/buses";
    }

    @GetMapping("/buses/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("busId", id);
        model.addAttribute("form", busService.toForm(busService.get(id)));
        addBusFormOptions(model);
        return "admin/bus-form";
    }

    @PostMapping("/buses/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") BusForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("busId", id);
            addBusFormOptions(model);
            return "admin/bus-form";
        }
        try {
            busService.update(id, form);
        } catch (BusinessException ex) {
            model.addAttribute("busId", id);
            rejectBusError(bindingResult, ex);
            addBusFormOptions(model);
            return "admin/bus-form";
        }
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật xe");
        return "redirect:/admin/buses";
    }

    @PostMapping("/buses/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        busService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Đã xóa xe");
        return "redirect:/admin/buses";
    }

    private void addBusFormOptions(Model model) {
        model.addAttribute("statuses", BusStatus.values());
        model.addAttribute("drivers", driverService.activeDrivers());
    }

    private void rejectBusError(BindingResult bindingResult, BusinessException ex) {
        String field = ex.getMessage().contains("tài xế") ? "driverId" : "licensePlate";
        bindingResult.rejectValue(field, field + ".invalid", ex.getMessage());
    }
}
