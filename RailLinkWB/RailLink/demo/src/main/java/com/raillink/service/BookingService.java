package com.raillink.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raillink.model.Booking;
import com.raillink.model.Schedule;
import com.raillink.model.User;
import com.raillink.repository.BookingRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ScheduleService scheduleService;

    public Booking createBooking(User user, Schedule schedule, String seatNumber) {
        if (!isSeatAvailable(schedule, seatNumber)) {
            throw new RuntimeException("Seat " + seatNumber + " is not available for this schedule");
        }

        if (isTrainFull(schedule)) {
            throw new RuntimeException("Train is full for this schedule");
        }

        Booking booking = new Booking();
        booking.setBookingDate(LocalDateTime.now());
        booking.setSeatNumber(seatNumber);
        booking.setStatus("CONFIRMED");
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setFare(calculateFare(schedule));

        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    public List<Booking> findBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Optional<Booking> findBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> findBookingsBySchedule(Long scheduleId) {
        return bookingRepository.findByScheduleId(scheduleId);
    }

    public List<Integer> getAvailableSeatsForSchedule(Schedule schedule) {
        int capacity = schedule.getTrain().getCapacity();
        List<Booking> existing = bookingRepository.findByScheduleId(schedule.getId());
        Set<String> taken = new HashSet<>();
        for (Booking b : existing) {
            if ("CONFIRMED".equals(b.getStatus())) {
                taken.add(b.getSeatNumber());
            }
        }
        List<Integer> available = new ArrayList<>();
        for (int i = 1; i <= capacity; i++) {
            if (!taken.contains(String.valueOf(i))) {
                available.add(i);
            }
        }
        return available;
    }

    private boolean isSeatAvailable(Schedule schedule, String seatNumber) {
        List<Booking> existingBookings = bookingRepository.findByScheduleId(schedule.getId());
        return existingBookings.stream()
                .noneMatch(booking -> booking.getSeatNumber().equals(seatNumber) &&
                                     "CONFIRMED".equals(booking.getStatus()));
    }

    private boolean isTrainFull(Schedule schedule) {
        List<Booking> confirmedBookings = bookingRepository.findByScheduleId(schedule.getId())
                .stream()
                .filter(booking -> "CONFIRMED".equals(booking.getStatus()))
                .toList();

        return confirmedBookings.size() >= schedule.getTrain().getCapacity();
    }

    private BigDecimal calculateFare(Schedule schedule) {
        // Simple fare calculator: fixed rate; can be extended to use distance/time
        return BigDecimal.valueOf(25.00);
    }
} 