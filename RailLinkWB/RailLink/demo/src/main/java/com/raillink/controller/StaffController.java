package com.raillink.controller;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.raillink.model.Schedule;
import com.raillink.model.Train;
import com.raillink.model.User;
import com.raillink.service.ScheduleService;
import com.raillink.service.TrainService;
import com.raillink.service.BookingService;
import com.raillink.service.UserService;
import com.raillink.model.Booking;
@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class StaffController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private TrainService trainService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    // @Autowired
    // private FinanceReportService financeReportService;  // Temporarily disabled - causing 500 error
    @GetMapping("/schedules")
    public String viewSchedules(Model model) {
        List<Schedule> schedules = scheduleService.findAllSchedules();
        model.addAttribute("schedules", schedules);
        model.addAttribute("isStaffView", true); // Flag to indicate this is staff view
        return "admin/schedules"; // Reuse admin template but with read-only mode
    }
    @GetMapping("/trains")
    public String viewTrains(Model model) {
        List<Train> trains = trainService.findAllTrains();
        model.addAttribute("trains", trains);
        model.addAttribute("isStaffView", true); // Flag to indicate this is staff view
        return "admin/trains"; // Reuse admin template but with read-only mode
    }
    @GetMapping("/bookings")
    public String viewBookings(@RequestParam(required = false) String passenger,
                              @RequestParam(required = false) String bookingId,
                              Model model) {
        System.out.println("=== STAFF BOOKINGS PAGE LOADED ===");
        System.out.println("Search parameters - Passenger: " + passenger + ", Booking ID: " + bookingId);
        
        List<Booking> bookings;
        
        // Apply search filters
        if (bookingId != null && !bookingId.trim().isEmpty()) {
            // Search by booking ID
            try {
                Long id = Long.parseLong(bookingId.trim());
                Optional<Booking> booking = bookingService.findBookingById(id);
                bookings = booking.map(List::of).orElse(List.of());
                System.out.println("Searching by booking ID: " + id + ", Found: " + bookings.size());
            } catch (NumberFormatException e) {
                System.out.println("Invalid booking ID format: " + bookingId);
                bookings = List.of();
            }
        } else if (passenger != null && !passenger.trim().isEmpty()) {
            // Search by passenger name or email
            bookings = bookingService.findBookingsByPassenger(passenger.trim());
            System.out.println("Searching by passenger: " + passenger + ", Found: " + bookings.size());
        } else {
            // No search criteria, return all bookings
            bookings = bookingService.findAllBookings();
            System.out.println("No search criteria, returning all bookings: " + bookings.size());
        }
        
        // Add users and schedules for create booking form
        List<User> users = userService.findAllUsers();
        List<Schedule> schedules = scheduleService.findAllSchedules();
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("users", users);
        model.addAttribute("schedules", schedules);
        model.addAttribute("isStaffView", true); // Flag to indicate this is staff view
        return "admin/bookings"; // Reuse admin template but with read-only mode
    }
    
}
