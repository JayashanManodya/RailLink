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
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class StaffApiController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private RefundService refundService;
    @Autowired
    private ScheduleUpdateBroadcaster scheduleBroadcaster;
    @GetMapping("/schedules")
    public ResponseEntity<List<java.util.Map<String, Object>>> listSchedules() {
        List<Schedule> list = scheduleService.findAllSchedules();
        List<java.util.Map<String, Object>> dto = list.stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("departureDate", s.getDepartureDate());
            m.put("arrivalDate", s.getArrivalDate());
            m.put("status", s.getStatus());
            m.put("trainName", s.getTrain().getName());
            m.put("routeName", s.getRoute().getName());
            return m;
        }).toList();
        return ResponseEntity.ok(dto);
    }
    @PutMapping("/schedules/{id}/status")
    public ResponseEntity<Schedule> updateScheduleStatus(@PathVariable Long id, @RequestParam String status) {
        Schedule s = scheduleService.findScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found"));
        s.setStatus(status);
        Schedule saved = scheduleService.saveSchedule(s);
        scheduleBroadcaster.broadcastScheduleUpdate(saved);
        return ResponseEntity.ok(saved);
    }
    @GetMapping("/bookings/by-schedule/{scheduleId}")
    public ResponseEntity<List<Booking>> bookingsBySchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(bookingService.findBookingsBySchedule(scheduleId));
    }
    @GetMapping("/bookings/{id}")
    public ResponseEntity<java.util.Map<String, Object>> bookingById(@PathVariable Long id) {
        return bookingService.findBookingById(id)
                .map(b -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", b.getId());
                    m.put("seatNumber", b.getSeatNumber());
                    m.put("status", b.getStatus());
                    m.put("fare", b.getFare());
                    m.put("scheduleId", b.getSchedule().getId());
                    m.put("trainName", b.getSchedule().getTrain().getName());
                    m.put("routeName", b.getSchedule().getRoute().getName());
                    m.put("departureDate", b.getSchedule().getDepartureDate());
                    m.put("arrivalDate", b.getSchedule().getArrivalDate());
                    m.put("username", b.getUser().getUsername());
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/refunds")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> listRefunds() {
        java.util.List<Refund> list = refundService.findAll();
        java.util.List<java.util.Map<String, Object>> dto = list.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("amount", r.getAmount());
            m.put("status", r.getStatus());
            m.put("requestedAt", r.getRequestedAt());
            m.put("processedAt", r.getProcessedAt());
            m.put("processedBy", r.getProcessedBy());
            try { m.put("bookingId", r.getBooking() != null ? r.getBooking().getId() : null); } catch (Exception ignore) {}
            return m;
        }).toList();
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/refunds/{id}")
    public ResponseEntity<java.util.Map<String, Object>> getRefund(@PathVariable Long id) {
        return refundService.findAll().stream().filter(r -> r.getId().equals(id)).findFirst()
                .map(r -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", r.getId());
                    m.put("amount", r.getAmount());
                    m.put("status", r.getStatus());
                    m.put("requestedAt", r.getRequestedAt());
                    m.put("processedAt", r.getProcessedAt());
                    m.put("processedBy", r.getProcessedBy());
                    try { m.put("bookingId", r.getBooking() != null ? r.getBooking().getId() : null); } catch (Exception ignore) {}
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/refunds/{id}/approve")
    public ResponseEntity<Refund> approveRefund(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.approveRefund(id, admin));
    }
    @PostMapping("/refunds/{id}/reject")
    public ResponseEntity<Refund> rejectRefund(@PathVariable Long id, @RequestParam String admin) {
        return ResponseEntity.ok(refundService.rejectRefund(id, admin));
    }
}
