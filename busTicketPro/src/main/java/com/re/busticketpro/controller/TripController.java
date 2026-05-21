package com.re.busticketpro.controller;

import com.re.busticketpro.dto.BookingRequest;
import com.re.busticketpro.dto.TripSearchForm;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.service.SeatService;
import com.re.busticketpro.service.TripSearchService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/trips")
public class TripController {
    private final TripSearchService tripSearchService;
    private final SeatService seatService;

    public TripController(TripSearchService tripSearchService, SeatService seatService) {
        this.tripSearchService = tripSearchService;
        this.seatService = seatService;
    }

    @GetMapping("/search")
    public String search(Model model) {
        model.addAttribute("searchForm", new TripSearchForm());
        model.addAttribute("locations", tripSearchService.locations());
        return "trips/search";
    }

    @PostMapping("/search")
    public String doSearch(@Valid @ModelAttribute("searchForm") TripSearchForm form, BindingResult bindingResult, Model model) {
        model.addAttribute("locations", tripSearchService.locations());
        if (bindingResult.hasErrors()) {
            return "trips/search";
        }
        try {
            model.addAttribute("trips", tripSearchService.search(form));
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("trips", java.util.List.of());
        }
        return "trips/search";
    }

    @GetMapping("/{tripId}/seats")
    public String seats(@PathVariable Long tripId, Model model) {
        model.addAttribute("trip", seatService.getTrip(tripId));
        model.addAttribute("seats", seatService.seatsForTrip(tripId));
        BookingRequest request = new BookingRequest();
        request.setTripId(tripId);
        model.addAttribute("bookingRequest", request);
        return "trips/seats";
    }
}
