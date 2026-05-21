package com.re.busticketpro.controller;

import com.re.busticketpro.dto.TicketLookupForm;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.LocationRepository;
import com.re.busticketpro.repository.TicketRepository;
import com.re.busticketpro.repository.UserProfileRepository;
import com.re.busticketpro.repository.UserRepository;
import com.re.busticketpro.service.PassengerTicketService;
import com.re.busticketpro.service.TicketLookupService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tickets")
public class TicketController {
    private final TicketLookupService ticketLookupService;
    private final PassengerTicketService passengerTicketService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LocationRepository locationRepository;

    public TicketController(TicketLookupService ticketLookupService, PassengerTicketService passengerTicketService,
                            TicketRepository ticketRepository, UserRepository userRepository,
                            UserProfileRepository userProfileRepository, LocationRepository locationRepository) {
        this.ticketLookupService = ticketLookupService;
        this.passengerTicketService = passengerTicketService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/lookup")
    public String lookup(Model model) {
        model.addAttribute("form", new TicketLookupForm());
        return "tickets/lookup";
    }

    @PostMapping("/lookup")
    public String doLookup(@Valid @ModelAttribute("form") TicketLookupForm form, BindingResult bindingResult, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                model.addAttribute("ticket", ticketLookupService.lookup(form.getTicketCode(), form.getPassengerPhone()));
            } catch (BusinessException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        return "tickets/lookup";
    }

    @GetMapping("/my")
    public String myTickets(@RequestParam(required = false) Long departureId,
                            @RequestParam(required = false) Long arrivalId,
                            Authentication authentication,
                            Model model) {
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        String phone = userProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.getPhone())
                .orElse(null);
        model.addAttribute("locations", locationRepository.findAllByOrderByNameAsc());
        model.addAttribute("departureId", departureId);
        model.addAttribute("arrivalId", arrivalId);
        model.addAttribute("tickets", ticketRepository.findMyTickets(user.getId(), phone, departureId, arrivalId));
        return "tickets/my";
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam String ticketCode, @RequestParam String passengerPhone, RedirectAttributes redirectAttributes) {
        passengerTicketService.cancelByPassenger(ticketCode, passengerPhone);
        redirectAttributes.addFlashAttribute("success", "Hủy vé thành công");
        return "redirect:/tickets/lookup";
    }
}
