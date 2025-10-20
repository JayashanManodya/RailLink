package com.raillink.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.raillink.model.Booking;
import com.raillink.model.Refund;
import com.raillink.model.Schedule;
import com.raillink.service.BookingService;
import com.raillink.service.RefundService;
import com.raillink.service.ScheduleService;
import com.raillink.service.ScheduleUpdateBroadcaster;

@RestController // Marks this class as a REST controller to handle API requests
@RequestMapping("/api/staff") // Base URL for all endpoints in this controller
@PreAuthorize("hasAnyRole('STAFF','ADMIN')") // Only STAFF and ADMIN roles can access these endpoints
public class StaffApiController {

    @Autowired // Automatically injects ScheduleService
    private ScheduleService scheduleService;

    @Autowired // Automatically injects BookingService
    private BookingService bookingService;

    @Autowired // Automatically injects RefundService
    private RefundService refundService;

    @Autowired // Automatically injects ScheduleUpdateBroadcaster for live updates
    private ScheduleUpdateBroadcaster scheduleBroadcaster;

    // Get all schedules with basic details
    @GetMapping("/schedules")
    public ResponseEntity<List<java.util.Map<String, Object>>> listSchedules() {
        List<Schedule> list = scheduleService.findAllSchedules(); // Get all schedules
        List<java.util.Map<String, Object>> dto = list.stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>(); // Create a map for each schedule
            m.put("id", s.getId()); // Schedule ID
            m.put("departureDate", s.getDepartureDate()); // Departure date
            m.put("arrivalDate", s.getArrivalDate()); // Arrival date
            m.put("status", s.getStatus()); // Schedule status
            m.put("trainName", s.getTrain().getName()); // Train name
            m.put("routeName", s.getRoute().getName()); // Route name
            return m; // Return map
        }).toList(); // Convert stream to list
        return ResponseEntity.ok(dto); // Return the list as HTTP 200 OK response
    }

    // Update the status of a specific schedule
    @PutMapping("/schedules/{id}/status")
    public ResponseEntity<Schedule> updateScheduleStatus(@PathVariable Long id, @RequestParam String status) {
        Schedule s = scheduleService.findScheduleById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found")); // Find schedule or throw error
        s.setStatus(status); // Update status
        Schedule saved = scheduleService.saveSchedule(s); // Save updated schedule
        scheduleBroadcaster.broadcastScheduleUpdate(saved); // Notify clients about update
        return ResponseEntity.ok(saved); // Return updated schedule
    }

    // Get all bookings related to a specific schedule
    @GetMapping("/bookings/by-schedule/{scheduleId}")
    public ResponseEntity<List<Booking>> bookingsBySchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(bookingService.findBookingsBySchedule(scheduleId)); // Return bookings list
    }

    // Get booking details by booking ID
    @GetMapping("/bookings/{id}")
    public ResponseEntity<java.util.Map<String, Object>> bookingById(@PathVariable Long id) {
        return bookingService.findBookingById(id)
                .map(b -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>(); // Map for booking details
                    m.put("id", b.getId()); // Booking ID
                    m.put("seatNumber", b.getSeatNumber()); // Seat number
                    m.put("status", b.getStatus()); // Booking status
                    m.put("fare", b.getFare()); // Fare amount
                    m.put("scheduleId", b.getSchedule().getId()); // Schedule ID
                    m.put("trainName", b.getSchedule().getTrain().getName()); // Train name
                    m.put("routeName", b.getSchedule().getRoute().getName()); // Route name
                    m.put("departureDate", b.getSchedule().getDepartureDate()); // Departure date
                    m.put("arrivalDate", b.getSchedule().getArrivalDate()); // Arrival date
                    m.put("username", b.getUser().getUsername()); // Username of the person who booked
                    return ResponseEntity.ok(m); // Return booking details
                })
                .orElseGet(() -> ResponseEntity.notFound().build()); // Return 404 if not found
    }

    // Get list of all refunds
    @GetMapping("/refunds")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> listRefunds() {
        java.util.List<Refund> list = refundService.findAll(); // Get all refunds
        java.util.List<java.util.Map<String, Object>> dto = list.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>(); // Map for refund details
            m.put("id", r.getId()); // Refund ID
            m.put("amount", r.getAmount()); // Refund amount
            m.put("status", r.getStatus()); // Refund status
            m.put("requestedAt", r.getRequestedAt()); // Requested date/time
            m.put("processedAt", r.getProcessedAt()); // Processed date/time
            m.put("processedBy", r.getProcessedBy()); // Processed by admin name
            try {
                m.put("bookingId", r.getBooking() != null ? r.getBooking().getId() : null); // Linked booking ID
            } catch (Exception ignore) {}
            return m; // Return map
        }).toList(); // Convert to list
        return ResponseEntity.ok(dto); // Return refund list
    }

    // Get refund details by refund ID
    @GetMapping("/refunds/{id}")
    public ResponseEntity<java.util.Map<String, Object>> getRefund(@PathVariable Long id) {
        return refundService.findAll().stream().filter(r -> r.getId().equals(id)).findFirst()
                .map(r -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>(); // Map for refund details
                    m.put("id", r.getId()); // Refund ID
                    m.put("amount", r.getAmount()); // Refund amount
                    m.put("status", r.getStatus()); // Refund status
                    m.put("requestedAt", r.getRequestedAt()); // Requested time
                    m.put("processedAt", r.getProcessedAt()); // Processed time
                    m.put("processedBy", r.getProcessedBy()); // Admin who processed refund
                    try {
                        m.put("bookingId", r.getBooking() != null ? r.getBooking().getId() : null); // Related booking ID
                    } catch (Exception ignore) {}
                    return ResponseEntity.ok(m); // Return refund details
                })
                .orElseGet(() -> ResponseEntity.notFound().build()); // Return 404 if refund not found
    }

    // Approve a refund by admin
    @PostMapping("/refunds/{id}/approve")
    public ResponseEntity<Refund> approveRefund(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.approveRefund(id, admin)); // Approve refund and return result
    }

    // Reject a refund by admin
    @PostMapping("/refunds/{id}/reject")
    public ResponseEntity<Refund> rejectRefund(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.rejectRefund(id, admin)); // Reject refund and return result
    }
}
