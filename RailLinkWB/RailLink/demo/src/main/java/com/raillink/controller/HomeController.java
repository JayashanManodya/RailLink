package com.raillink.controller;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.raillink.service.AnnouncementService;
import com.raillink.service.StationService;
import com.raillink.service.BookingService;
import com.raillink.service.UserService;
import com.raillink.model.User;
import com.raillink.model.Booking;
@Controller
public class HomeController {
    @Autowired
    private StationService stationService;
    @Autowired
    private AnnouncementService announcementService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home(Model model) {
        List<com.raillink.model.Station> stations = stationService.findAllStations();
        model.addAttribute("stations", stations);
        model.addAttribute("activeAnnouncements", announcementService.findActiveAnnouncements());
        return "index";
    }
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        List<com.raillink.model.Announcement> activeAnnouncements = announcementService.findActiveAnnouncements();
        System.out.println("DEBUG: Found " + activeAnnouncements.size() + " active announcements");
        for (com.raillink.model.Announcement a : activeAnnouncements) {
            System.out.println("DEBUG: Announcement - " + a.getTitle() + " (Start: " + a.getStartDate() + ", End: " + a.getEndDate() + ")");
        }
        model.addAttribute("allAnnouncements", activeAnnouncements);
        
        // Add user-specific data
        if (userDetails != null) {
            java.util.Optional<User> userOptional = userService.findByUsername(userDetails.getUsername());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("currentUser", user);
                
                // Get all user bookings with details
                List<Booking> allBookings = bookingService.findBookingsByUserWithDetails(user.getId());
                
                // Calculate statistics
                long totalBookings = allBookings.size();
                long activeBookings = allBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .count();
                long completedBookings = allBookings.stream()
                    .filter(b -> "COMPLETED".equals(b.getStatus()))
                    .count();
                long cancelledBookings = allBookings.stream()
                    .filter(b -> "CANCELLED".equals(b.getStatus()))
                    .count();
                
                // Get upcoming trips (confirmed bookings with future departure dates)
                List<Booking> upcomingTrips = allBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .filter(b -> b.getSchedule() != null && b.getSchedule().getDepartureDate() != null)
                    .filter(b -> b.getSchedule().getDepartureDate().isAfter(LocalDateTime.now()))
                    .sorted((b1, b2) -> b1.getSchedule().getDepartureDate().compareTo(b2.getSchedule().getDepartureDate()))
                    .limit(5)
                    .collect(Collectors.toList());
                
                // Get recent bookings (last 5)
                List<Booking> recentBookings = allBookings.stream()
                    .sorted((b1, b2) -> b2.getBookingDate().compareTo(b1.getBookingDate()))
                    .limit(5)
                    .collect(Collectors.toList());
                
                model.addAttribute("totalBookings", totalBookings);
                model.addAttribute("activeBookings", activeBookings);
                model.addAttribute("completedBookings", completedBookings);
                model.addAttribute("cancelledBookings", cancelledBookings);
                model.addAttribute("upcomingTrips", upcomingTrips);
                model.addAttribute("recentBookings", recentBookings);
            }
        }
        
        List<com.raillink.model.Station> stations = stationService.findAllStations();
        model.addAttribute("stations", stations);
        return "dashboard";
    }
    @GetMapping("/debug")
    public String debug() {
        return "debug";
    }
    @GetMapping("/help")
    public String help() {
        return "help";
    }
    @GetMapping("/debug/create-announcement")
    public String createTestAnnouncement() {
        try {
            com.raillink.model.Announcement testAnnouncement = new com.raillink.model.Announcement();
            testAnnouncement.setTitle("Test Announcement");
            testAnnouncement.setMessage("This is a test announcement to verify the system is working.");
            testAnnouncement.setStartDate(java.time.LocalDateTime.now().minusHours(1));
            testAnnouncement.setEndDate(java.time.LocalDateTime.now().plusDays(7));
            testAnnouncement.setAuthor("system");
            announcementService.save(testAnnouncement);
            System.out.println("DEBUG: Test announcement created successfully");
        } catch (Exception e) {
            System.out.println("DEBUG: Error creating test announcement: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
} 