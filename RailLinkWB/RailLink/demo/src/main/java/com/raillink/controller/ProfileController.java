package com.raillink.controller;
import com.raillink.model.Booking;
import com.raillink.model.User;
import com.raillink.service.BookingService;
import com.raillink.service.PdfService;
import com.raillink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private PdfService pdfService;
    @GetMapping("/my-bookings")
    public ResponseEntity<List<Booking>> getMyBookings(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Booking> bookings = bookingService.findBookingsByUser(user.getId());
        return ResponseEntity.ok(bookings);
    }
    @GetMapping("/my-bookings/{id}/ticket")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Booking booking = bookingService.findBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }
            byte[] ticketPdf = pdfService.generateTicketPdf(booking);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "ticket_" + id + ".txt");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(ticketPdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 