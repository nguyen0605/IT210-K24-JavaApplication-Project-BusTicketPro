package com.re.busticketpro.controller;

import com.re.busticketpro.dto.BookingRequest;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.TicketRepository;
import com.re.busticketpro.repository.UserRepository;
import com.re.busticketpro.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/booking")
public class BookingController {
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository, TicketRepository ticketRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping
    public String book(@Valid @ModelAttribute("bookingRequest") BookingRequest request,
                       BindingResult bindingResult,
                       Authentication authentication,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin hành khách hợp lệ");
            return "redirect:/trips/" + request.getTripId() + "/seats";
        }

        Long userId = userRepository.findByUsername(authentication.getName())
                .map(user -> user.getId())
                .orElse(null);

        try {
            var ticket = bookingService.bookTicket(request, userId);
            return "redirect:/booking/success/" + ticket.getTicketCode();
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/trips/" + request.getTripId() + "/seats";
        }
    }

    @GetMapping("/success/{ticketCode}")
    public String success(@PathVariable String ticketCode, org.springframework.ui.Model model) {
        model.addAttribute("ticketCode", ticketCode);
        ticketRepository.findByTicketCode(ticketCode).ifPresent(ticket -> model.addAttribute("ticket", ticket));
        return "booking/success";
    }
}
