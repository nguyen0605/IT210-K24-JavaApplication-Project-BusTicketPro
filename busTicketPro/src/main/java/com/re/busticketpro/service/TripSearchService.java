package com.re.busticketpro.service;

import com.re.busticketpro.dto.TripSearchForm;
import com.re.busticketpro.entity.Location;
import com.re.busticketpro.entity.Trip;
import com.re.busticketpro.enums.TripStatus;
import com.re.busticketpro.exception.BusinessException;
import com.re.busticketpro.repository.LocationRepository;
import com.re.busticketpro.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripSearchService {
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;

    public TripSearchService(TripRepository tripRepository, LocationRepository locationRepository) {
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
    }

    public List<Location> locations() {
        return locationRepository.findAllByOrderByNameAsc();
    }

    public List<Trip> search(TripSearchForm form) {
        if (form.getDepartureId().equals(form.getArrivalId())) {
            throw new BusinessException("Điểm đi không được trùng điểm đến");
        }
        if (form.getDepartureDate() == null) {
            return tripRepository.searchUpcomingTrips(
                    form.getDepartureId(),
                    form.getArrivalId(),
                    LocalDateTime.now(),
                    TripStatus.SCHEDULED
            );
        }
        var start = form.getDepartureDate().atStartOfDay();
        return tripRepository.searchTrips(form.getDepartureId(), form.getArrivalId(), start, start.plusDays(1), TripStatus.SCHEDULED);
    }
}
