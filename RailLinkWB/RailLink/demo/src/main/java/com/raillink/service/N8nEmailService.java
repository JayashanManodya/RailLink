package com.raillink.service;

import com.raillink.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to send booking confirmation emails via n8n webhook
 */
@Service
public class N8nEmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${n8n.webhook.url:}")
    private String n8nWebhookUrl;

    @Value("${n8n.enabled:true}")
    private boolean n8nEnabled;

    /**
     * Send booking confirmation email via n8n webhook
     * This method is async so it doesn't block the booking process
     */
    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        if (!n8nEnabled || n8nWebhookUrl == null || n8nWebhookUrl.trim().isEmpty()) {
            System.out.println("N8n email service is disabled or webhook URL is not configured");
            return;
        }

        try {
            Map<String, Object> payload = buildEmailPayload(booking);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                n8nWebhookUrl, 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Booking confirmation email sent successfully for booking ID: " + booking.getId());
            } else {
                System.err.println("Failed to send email. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Log error but don't throw - we don't want to break the booking flow
            System.err.println("Error sending booking confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send booking confirmation email for multiple bookings (group booking)
     */
    @Async
    public void sendBookingConfirmationEmail(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return;
        }
        
        // Send email for the first booking (main booking) with details of all bookings
        Booking mainBooking = bookings.get(0);
        
        if (!n8nEnabled || n8nWebhookUrl == null || n8nWebhookUrl.trim().isEmpty()) {
            System.out.println("N8n email service is disabled or webhook URL is not configured");
            return;
        }

        try {
            Map<String, Object> payload = buildGroupEmailPayload(bookings);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                n8nWebhookUrl, 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Group booking confirmation email sent successfully for booking ID: " + mainBooking.getId());
            } else {
                System.err.println("Failed to send email. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error sending group booking confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build email payload for single booking
     */
    private Map<String, Object> buildEmailPayload(Booking booking) {
        Map<String, Object> payload = new HashMap<>();
        
        // Passenger information
        Map<String, Object> passenger = new HashMap<>();
        passenger.put("name", booking.getUser().getUsername());
        passenger.put("email", booking.getUser().getEmail());
        payload.put("passenger", passenger);
        
        // Booking information
        Map<String, Object> bookingInfo = new HashMap<>();
        bookingInfo.put("bookingId", booking.getId());
        bookingInfo.put("bookingDate", booking.getBookingDate().toString());
        bookingInfo.put("seatNumber", booking.getSeatNumber());
        bookingInfo.put("ticketClass", booking.getTicketClass() != null ? booking.getTicketClass() : "Standard");
        bookingInfo.put("fare", booking.getFare() != null ? booking.getFare().toString() : "0.00");
        bookingInfo.put("status", booking.getStatus());
        payload.put("booking", bookingInfo);
        
        // Train and schedule information
        Map<String, Object> scheduleInfo = new HashMap<>();
        scheduleInfo.put("scheduleId", booking.getSchedule().getId());
        scheduleInfo.put("trainName", booking.getSchedule().getTrain().getName());
        scheduleInfo.put("routeName", booking.getSchedule().getRoute().getName());
        scheduleInfo.put("departureDate", booking.getSchedule().getDepartureDate().toString());
        scheduleInfo.put("arrivalDate", booking.getSchedule().getArrivalDate().toString());
        // Route name typically contains station information (e.g., "Colombo - Kandy")
        scheduleInfo.put("departureStation", extractFromStation(booking.getSchedule().getRoute().getName()));
        scheduleInfo.put("arrivalStation", extractToStation(booking.getSchedule().getRoute().getName()));
        scheduleInfo.put("duration", booking.getSchedule().getDuration());
        payload.put("schedule", scheduleInfo);
        
        // Email metadata
        payload.put("emailType", "booking_confirmation");
        payload.put("subject", "RailLink - Booking Confirmation #" + booking.getId());
        
        return payload;
    }

    /**
     * Build email payload for group booking (multiple tickets)
     */
    private Map<String, Object> buildGroupEmailPayload(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return new HashMap<>();
        }
        
        Booking mainBooking = bookings.get(0);
        Map<String, Object> payload = buildEmailPayload(mainBooking);
        
        // Add information about all tickets
        List<Map<String, Object>> tickets = new java.util.ArrayList<>();
        java.math.BigDecimal totalFare = java.math.BigDecimal.ZERO;
        
        for (Booking booking : bookings) {
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("bookingId", booking.getId());
            ticket.put("seatNumber", booking.getSeatNumber());
            ticket.put("ticketClass", booking.getTicketClass() != null ? booking.getTicketClass() : "Standard");
            ticket.put("fare", booking.getFare() != null ? booking.getFare().toString() : "0.00");
            tickets.add(ticket);
            
            if (booking.getFare() != null) {
                totalFare = totalFare.add(booking.getFare());
            }
        }
        
        payload.put("tickets", tickets);
        payload.put("ticketCount", bookings.size());
        payload.put("totalFare", totalFare.toString());
        payload.put("subject", "RailLink - Booking Confirmation #" + mainBooking.getId() + " (" + bookings.size() + " tickets)");
        
        return payload;
    }
    
    /**
     * Extract departure station from route name (format: "From - To")
     */
    private String extractFromStation(String routeName) {
        if (routeName == null || routeName.trim().isEmpty()) {
            return "N/A";
        }
        String[] parts = routeName.split("-");
        return parts.length > 0 ? parts[0].trim() : routeName;
    }
    
    /**
     * Extract arrival station from route name (format: "From - To")
     */
    private String extractToStation(String routeName) {
        if (routeName == null || routeName.trim().isEmpty()) {
            return "N/A";
        }
        String[] parts = routeName.split("-");
        return parts.length > 1 ? parts[parts.length - 1].trim() : routeName;
    }
}

