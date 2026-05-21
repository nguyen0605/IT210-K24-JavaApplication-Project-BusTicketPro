package com.re.busticketpro.service;

import com.re.busticketpro.entity.Seat;
import com.re.busticketpro.entity.Trip;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.SeatRepository;
import com.re.busticketpro.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;

    public SeatService(SeatRepository seatRepository, TripRepository tripRepository) {
        this.seatRepository = seatRepository;
        this.tripRepository = tripRepository;
    }

    public Trip getTrip(Long tripId) {
        return tripRepository.findDetailedById(tripId).orElseThrow(() -> new BusinessException("Không tìm thấy chuyến"));
    }

    public List<Seat> seatsForTrip(Long tripId) {
        return seatRepository.findByTripIdOrderBySeatFloorAscSeatRowAscSeatColAsc(tripId);
    }
}
