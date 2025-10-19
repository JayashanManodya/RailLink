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
    
    public Booking createBooking(User user, Schedule schedule, String seatNumber) {
        return createBooking(user, schedule, seatNumber, null);
    }
    
    public Booking createBooking(User user, Schedule schedule, String seatNumber, String ticketClass) {
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
        booking.setTicketClass(ticketClass);
        booking.setFare(calculateFare(schedule, ticketClass));
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
    
    public List<Booking> findBookingsByUserWithDetails(Long userId) {
        return bookingRepository.findByUserIdWithDetails(userId);
    }
    
    public List<Booking> findBookingsByPassenger(String passenger) {
        return bookingRepository.findByPassengerNameOrEmail(passenger);
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
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
    public void deleteBooking(Long bookingId) {
        System.out.println("=== BOOKING SERVICE DELETE CALLED ===");
        System.out.println("Attempting to delete booking with ID: " + bookingId);
        boolean exists = bookingRepository.existsById(bookingId);
        System.out.println("Booking exists in repository: " + exists);
        if (!exists) {
            System.out.println("Booking not found in repository with ID: " + bookingId);
            throw new RuntimeException("Booking not found with ID: " + bookingId);
        }
        System.out.println("Proceeding to delete booking from repository...");
        bookingRepository.deleteById(bookingId);
        System.out.println("Booking successfully deleted from repository");
    }
    
    public void deleteBookingsByUser(User user) {
        List<Booking> userBookings = bookingRepository.findByUserId(user.getId());
        System.out.println("Deleting " + userBookings.size() + " bookings for user: " + user.getUsername());
        for (Booking booking : userBookings) {
            bookingRepository.deleteById(booking.getId());
        }
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
    
    public java.util.Map<String, Integer> getAvailableSeatsPerClass(Schedule schedule) {
        java.util.Map<String, Integer> availablePerClass = new java.util.HashMap<>();
        
        // Get train classes and their capacities
        java.util.Map<String, Integer> trainClasses = schedule.getTrain().getClasses();
        if (trainClasses == null || trainClasses.isEmpty()) {
            return availablePerClass;
        }
        
        // Get all confirmed bookings for this schedule
        List<Booking> confirmedBookings = bookingRepository.findByScheduleId(schedule.getId())
                .stream()
                .filter(booking -> "CONFIRMED".equals(booking.getStatus()))
                .toList();
        
        // Count bookings per class
        java.util.Map<String, Integer> bookedPerClass = new java.util.HashMap<>();
        for (Booking booking : confirmedBookings) {
            String ticketClass = booking.getTicketClass();
            if (ticketClass != null && !ticketClass.isEmpty()) {
                bookedPerClass.put(ticketClass, bookedPerClass.getOrDefault(ticketClass, 0) + 1);
            }
        }
        
        // Calculate available seats for each class
        for (java.util.Map.Entry<String, Integer> entry : trainClasses.entrySet()) {
            String className = entry.getKey();
            Integer totalCapacity = entry.getValue();
            Integer booked = bookedPerClass.getOrDefault(className, 0);
            Integer available = totalCapacity - booked;
            availablePerClass.put(className, Math.max(0, available));
        }
        
        return availablePerClass;
    }
    public boolean isSeatAvailable(Schedule schedule, String seatNumber) {
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
    private BigDecimal calculateFare(Schedule schedule, String ticketClass) {
        if (ticketClass == null || ticketClass.isEmpty()) {
            return BigDecimal.valueOf(25.00); // Default fare
        }
        
        // Get pricing from schedule
        java.util.Map<String, BigDecimal> pricing = schedule.getPricing();
        if (pricing != null && pricing.containsKey(ticketClass)) {
            return pricing.get(ticketClass);
        }
        
        // Fallback to default pricing based on class
        switch (ticketClass.toLowerCase()) {
            case "first class":
                return BigDecimal.valueOf(1500.00);
            case "second class":
                return BigDecimal.valueOf(1000.00);
            case "third class":
                return BigDecimal.valueOf(500.00);
            default:
                return BigDecimal.valueOf(25.00);
        }
    }
} 