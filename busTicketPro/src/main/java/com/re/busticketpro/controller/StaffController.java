package com.re.busticketpro.controller;

import com.re.busticketpro.dto.ProfileForm;
import com.re.busticketpro.service.ProfileService;
import com.re.busticketpro.service.StaffTicketService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffController {
    private final StaffTicketService staffTicketService;
    private final ProfileService profileService;

    public StaffController(StaffTicketService staffTicketService, ProfileService profileService) {
        this.staffTicketService = staffTicketService;
        this.profileService = profileService;
    }

    @GetMapping
    public String index(Model model) {
        var tickets = staffTicketService.pendingTickets();
        model.addAttribute("tickets", tickets);
        model.addAttribute("pendingCount", tickets.size());
        model.addAttribute("expiringCount", tickets.stream()
                .filter(ticket -> ticket.getExpiredTime() != null
                        && ticket.getExpiredTime().isBefore(java.time.LocalDateTime.now().plusMinutes(30)))
                .count());
        return "staff/dashboard";
    }

    @GetMapping("/tickets/pending")
    public String pending(Model model) {
        model.addAttribute("tickets", staffTicketService.pendingTickets());
        return "staff/pending";
    }

    @GetMapping("/tickets/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", staffTicketService.get(id));
        return "staff/detail";
    }

    @PostMapping("/tickets/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        staffTicketService.confirmPayment(id);
        redirectAttributes.addFlashAttribute("success", "\u0110\u00e3 x\u00e1c nh\u1eadn thanh to\u00e1n");
        return "redirect:/staff/tickets/pending";
    }

    @PostMapping("/tickets/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
                         RedirectAttributes redirectAttributes) {
        staffTicketService.cancelTicket(id, reason);
        redirectAttributes.addFlashAttribute("success", "\u0110\u00e3 h\u1ee7y v\u00e9");
        return "redirect:/staff/tickets/pending";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("form", profileService.getForm(authentication.getName()));
        return "staff/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("form") ProfileForm form, BindingResult bindingResult,
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "staff/profile";
        }
        profileService.update(authentication.getName(), form);
        redirectAttributes.addFlashAttribute("success", "\u0110\u00e3 c\u1eadp nh\u1eadt h\u1ed3 s\u01a1");
        return "redirect:/staff/profile";
    }
}
