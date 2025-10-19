package com.raillink.service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.raillink.model.Booking;
@Service
public class ReminderService {
    @Autowired
    private BookingService bookingService;
    @Scheduled(cron = "0 0 * * * *")
    public void sendUpcomingBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.plusHours(24);
        List<Booking> all = bookingService.findAllBookings();
        all.stream()
                .filter(b -> b.getSchedule().getDepartureDate().isAfter(now)
                        && b.getSchedule().getDepartureDate().isBefore(cutoff)
                        && "CONFIRMED".equals(b.getStatus()))
                .forEach(b -> System.out.println("Reminder: Booking #" + b.getId() + " for user " + b.getUser().getUsername() + " is departing within 24 hours."));
    }
} 