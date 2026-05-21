package com.re.busticketpro.service;

import com.re.busticketpro.enums.TripStatus;
import com.re.busticketpro.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TripStatusSchedulerService {
    private final TripRepository tripRepository;

    public TripStatusSchedulerService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional
    public int markDepartedTrips(LocalDateTime now) {
        var trips = tripRepository.findByStatusAndDepartureTimeBefore(TripStatus.SCHEDULED, now);
        trips.forEach(trip -> trip.setStatus(TripStatus.DEPARTED));
        return trips.size();
    }

    @Transactional
    public int markCompletedTrips(LocalDateTime now) {
        var trips = tripRepository.findByStatusAndArrivalTimeBefore(TripStatus.DEPARTED, now);
        trips.forEach(trip -> trip.setStatus(TripStatus.COMPLETED));
        return trips.size();
    }
}
